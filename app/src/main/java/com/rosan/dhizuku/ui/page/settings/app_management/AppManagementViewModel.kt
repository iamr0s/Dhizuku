package com.rosan.dhizuku.ui.page.settings.app_management

import android.content.Context
import android.content.pm.PackageManager

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.shared.DhizukuVariables

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.component.inject

class AppManagementViewModel : ViewModel(), KoinComponent {
    val context by lazy {
        get<Context>()
    }

    private val packageManager: PackageManager by lazy {
        context.packageManager
    }

    private val repo by inject<AppRepo>()

    var state by mutableStateOf(AppManagementViewState())
        private set

    fun collect() {
        collectRepo()
    }

    private var collectRepoJob: Job? = null

    fun collectRepo() {
        state = state.copy(loading = true)
        collectRepoJob?.cancel()
        collectRepoJob = viewModelScope.launch(Dispatchers.IO) {
            repo.flowAll().collect {
                val flags = PackageManager.GET_META_DATA or PackageManager.GET_PERMISSIONS
                val data = packageManager
                    .getInstalledPackages(flags)
                    .mapNotNull { packageInfo ->
                        if (packageInfo.packageName == context.packageName) return@mapNotNull null
                        val applicationInfo =
                            packageInfo.applicationInfo ?: return@mapNotNull null

                        val uid = applicationInfo.uid
                        val requested = packageInfo.requestedPermissions
                            ?.contains(DhizukuVariables.PERMISSION_API) ?: false
                        val entity = it.find { it.uid == uid }
                        val allowApi = entity?.allowApi ?: false

                        if (!requested && entity == null)
                            return@mapNotNull null

                        AppManagementViewData(applicationInfo = applicationInfo, enabled = allowApi)
                    }
                state = state.copy(
                    data = data.distinctBy { it.applicationInfo.packageName }
                        .sortedBy { it.applicationInfo.packageName },
                    loading = false
                )
            }
        }
    }

    fun setEnabled(uid: Int, bool: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val signature = packageManager.getPackageInfoForUid(uid)?.signature ?: return@launch
            val entity = repo.findByUID(uid)
            if (entity == null)
                repo.insert(AppEntity(uid = uid, signature = signature, allowApi = bool))
            else
                repo.update(entity.copy(signature = signature, allowApi = bool))
        }
    }
}