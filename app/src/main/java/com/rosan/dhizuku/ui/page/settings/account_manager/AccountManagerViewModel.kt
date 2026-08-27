package com.rosan.dhizuku.ui.page.settings.account_manager

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.dhizuku.data.common.util.replace
import com.rosan.dhizuku.data.common.util.toast
import com.rosan.dhizuku.data.account.repo.UserService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AccountManagerViewModel(
    private val userId: Int
) : ViewModel(), KoinComponent {
    private val jobs = mutableMapOf<String, Job>()

    private val context by inject<Context>()
    private val userService by inject<UserService>()

    // SharedPreferences to persist frozen packages across app restarts
    private val prefs: SharedPreferences by lazy {
        context.getSharedPreferences("frozen_packages_user_$userId", Context.MODE_PRIVATE)
    }

    data class FrozenInfo(
        val packageName: String,
        val type: String,
        val label: String
    )

    private fun getFrozenPackages(): List<FrozenInfo> = synchronized(prefs) {
        val set = prefs.getStringSet("frozen_info", emptySet()) ?: emptySet()
        set.mapNotNull {
            val parts = it.split("|", limit = 3)
            if (parts.size >= 3) {
                FrozenInfo(packageName = parts[0], type = parts[1], label = parts[2])
            } else null
        }
    }

    private fun saveFrozenPackage(packageName: String, type: String, label: String) = synchronized(prefs) {
        val current = prefs.getStringSet("frozen_info", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.removeAll { it.startsWith("$packageName|") }
        current.add("$packageName|$type|$label")
        prefs.edit().putStringSet("frozen_info", current).commit()
    }

    private fun removeFrozenPackage(packageName: String) = synchronized(prefs) {
        val current = prefs.getStringSet("frozen_info", emptySet())?.toMutableSet() ?: mutableSetOf()
        current.removeAll { it.startsWith("$packageName|") }
        prefs.edit().putStringSet("frozen_info", current).commit()
    }

    var state by mutableStateOf(AccountManagerViewState())
        private set

    var togglingPackages by mutableStateOf(emptySet<String>())
        private set

    fun dispatch(action: AccountManagerViewAction) {
        when (action) {
            AccountManagerViewAction.Load -> load()
            is AccountManagerViewAction.FreezeToggle -> freezeToggle(action.packageName, action.currentFrozen)
            AccountManagerViewAction.FreezeAll -> freezeAll()
            AccountManagerViewAction.UnfreezeAll -> unfreezeAll()
        }
    }

    private fun freezeAll() {
        val toFreeze = state.authenticators.filter { !it.isFrozen }
        if (toFreeze.isEmpty()) return

        val pkgNames = toFreeze.map { it.auth.packageName }
        togglingPackages = togglingPackages + pkgNames

        val updated = state.authenticators.map {
            if (pkgNames.contains(it.auth.packageName)) it.copy(isFrozen = true) else it
        }
        state = state.copy(authenticators = updated)

        jobs.replace("freeze_all") { old ->
            old?.cancel()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    toFreeze.forEach { item ->
                        val pkg = item.auth.packageName
                        val type = item.auth.type
                        val label = item.auth.label
                        try {
                            userService.setPackageEnabled(pkg, false, userId)
                            saveFrozenPackage(pkg, type, label)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    delay(800)
                    load(showLoading = false)
                } finally {
                    withContext(Dispatchers.Main) {
                        togglingPackages = togglingPackages - pkgNames.toSet()
                    }
                }
            }
        }
    }

    private fun unfreezeAll() {
        val toThaw = state.authenticators.filter { it.isFrozen }
        if (toThaw.isEmpty()) return

        val pkgNames = toThaw.map { it.auth.packageName }
        togglingPackages = togglingPackages + pkgNames

        val updated = state.authenticators.map {
            if (pkgNames.contains(it.auth.packageName)) it.copy(isFrozen = false) else it
        }
        state = state.copy(authenticators = updated)

        jobs.replace("unfreeze_all") { old ->
            old?.cancel()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    toThaw.forEach { item ->
                        val pkg = item.auth.packageName
                        try {
                            userService.setPackageEnabled(pkg, true, userId)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    delay(800)
                    load(showLoading = false)
                } finally {
                    withContext(Dispatchers.Main) {
                        togglingPackages = togglingPackages - pkgNames.toSet()
                    }
                }
            }
        }
    }

    private fun freezeToggle(packageName: String, currentFrozen: Boolean) {
        if (togglingPackages.contains(packageName)) return

        togglingPackages = togglingPackages + packageName

        // Optimistic UI update
        val updatedAuthenticators = state.authenticators.map {
            if (it.auth.packageName == packageName) it.copy(isFrozen = !currentFrozen) else it
        }
        state = state.copy(authenticators = updatedAuthenticators)

        val existing = state.authenticators.find { it.auth.packageName == packageName }
        val type = existing?.auth?.type ?: ""
        val label = existing?.auth?.label ?: ""

        jobs.replace("toggle_$packageName") { oldJob ->
            oldJob?.cancel()
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    userService.setPackageEnabled(packageName, currentFrozen, userId)
                    // Persist new frozen state
                    if (!currentFrozen) saveFrozenPackage(packageName, type, label)
                    delay(800)
                    load(showLoading = false)
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        context.toast("操作失败: ${e.localizedMessage ?: e.message}")
                    }
                    load(showLoading = false)
                } finally {
                    withContext(Dispatchers.Main) {
                        togglingPackages = togglingPackages - packageName
                    }
                }
            }
        }
    }

    private fun load(showLoading: Boolean = true) {
        jobs.replace("load") {
            it?.cancel()
            if (showLoading) {
                state = state.copy(loading = true)
            }
            viewModelScope.launch(Dispatchers.IO) {
                val frozenPackages = getFrozenPackages()
                val auths = userService.getAccountAuthenticators(userId)
                val accounts = userService.getAccounts(userId)

                // Build authenticator list: active ones + any frozen ones not returned by system
                val activeAuthMap = auths.associateBy { it.packageName }

                // Merge: active authenticators + frozen ones that disappeared from system list
                val allAuthenticators = mutableListOf<AccountManagerViewState.Authenticator>()

                // Build a set of package names that we think are frozen based on SharedPreferences
                val frozenPkgNames = frozenPackages.map { it.packageName }.toSet()

                // Add all active authenticators (only those with accounts, or that are frozen)
                auths.forEach { auth ->
                    val matchingAccounts = accounts.filter { auth.type == it.type }
                    
                    var isActuallyFrozen = frozenPkgNames.contains(auth.packageName)
                    if (togglingPackages.contains(auth.packageName)) {
                        val existing = state.authenticators.find { it.auth.packageName == auth.packageName }
                        if (existing != null) {
                            isActuallyFrozen = existing.isFrozen
                        }
                    } else if (isActuallyFrozen) {
                        try {
                            val isSystemFrozen = !userService.isPackageEnabled(auth.packageName, userId)
                            if (!isSystemFrozen) {
                                isActuallyFrozen = false
                                removeFrozenPackage(auth.packageName)
                            }
                        } catch (e: Exception) {
                            isActuallyFrozen = false
                            removeFrozenPackage(auth.packageName)
                        }
                    }

                    // Only show if it has accounts OR is frozen (frozen apps lose their accounts)
                    if (matchingAccounts.isNotEmpty() || isActuallyFrozen) {
                        allAuthenticators.add(
                            AccountManagerViewState.Authenticator(
                                auth = auth,
                                accounts = matchingAccounts,
                                isFrozen = isActuallyFrozen
                            )
                        )
                    }
                }

                // Add frozen packages not currently returned by getAccountAuthenticators
                // (because system hides disabled app's authenticators)
                frozenPackages
                    .filter { activeAuthMap[it.packageName] == null }
                    .forEach { info ->
                        try {
                            var isStillFrozen = !userService.isPackageEnabled(info.packageName, userId)
                            if (togglingPackages.contains(info.packageName)) {
                                val existing = state.authenticators.find { it.auth.packageName == info.packageName }
                                if (existing != null) {
                                    isStillFrozen = existing.isFrozen
                                }
                            }

                            if (isStillFrozen) {
                                val existing = state.authenticators.find { it.auth.packageName == info.packageName }
                                if (existing != null) {
                                    allAuthenticators.add(existing.copy(isFrozen = true, accounts = emptyList()))
                                } else {
                                    // Load the icon dynamically since the package is disabled
                                    val icon = try {
                                        context.packageManager.getApplicationIcon(info.packageName)
                                    } catch (e: Exception) {
                                        androidx.core.content.ContextCompat.getDrawable(
                                            context,
                                            android.R.drawable.sym_def_app_icon
                                        )!!
                                    }
                                    val placeholderAuth = com.rosan.dhizuku.data.account.entity.AccountAuthenticatorEntity(
                                        userId = userId,
                                        type = info.type,
                                        packageName = info.packageName,
                                        label = info.label,
                                        icon = icon
                                    )
                                    allAuthenticators.add(
                                        AccountManagerViewState.Authenticator(
                                            auth = placeholderAuth,
                                            accounts = emptyList(),
                                            isFrozen = true
                                        )
                                    )
                                }
                            } else {
                                // Thawed but not in auths yet. Display it as active placeholder so it doesn't disappear!
                                val existing = state.authenticators.find { it.auth.packageName == info.packageName }
                                val icon = existing?.auth?.icon ?: try {
                                    context.packageManager.getApplicationIcon(info.packageName)
                                } catch (e: Exception) {
                                    androidx.core.content.ContextCompat.getDrawable(
                                        context,
                                        android.R.drawable.sym_def_app_icon
                                    )!!
                                }
                                val placeholderAuth = com.rosan.dhizuku.data.account.entity.AccountAuthenticatorEntity(
                                    userId = userId,
                                    type = info.type,
                                    packageName = info.packageName,
                                    label = info.label,
                                    icon = icon
                                )
                                allAuthenticators.add(
                                    AccountManagerViewState.Authenticator(
                                        auth = placeholderAuth,
                                        accounts = emptyList(),
                                        isFrozen = false // Show as active (unfrozen)
                                    )
                                )
                            }
                        } catch (e: Exception) {
                            if (!togglingPackages.contains(info.packageName)) {
                                removeFrozenPackage(info.packageName)
                            }
                        }
                    }

                state = state.copy(authenticators = allAuthenticators.sortedBy { it.auth.label }, loading = false)
            }
        }
    }
}