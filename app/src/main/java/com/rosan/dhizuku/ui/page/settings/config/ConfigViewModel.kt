package com.rosan.dhizuku.ui.page.settings.config

import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.dhizuku.data.common.util.clearDelegatedScopes
import com.rosan.dhizuku.data.common.util.getPackageInfoForUid
import com.rosan.dhizuku.data.common.util.signature
import com.rosan.dhizuku.data.settings.repo.AppRepo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfigViewModel(private val repo: AppRepo) : ViewModel(), KoinComponent {
    private val context by inject<Context>()

    var state by mutableStateOf(ConfigViewState())
        private set

    fun dispatch(action: ConfigViewAction) {
        when (action) {
            ConfigViewAction.Init -> init()
            is ConfigViewAction.UpdateConfigByUID -> updateConfigByUID(
                action.uid,
                action.allowApi
            )
        }
    }

    private fun init() {
        collectRepo()
    }

    private var collectRepoJob: Job? = null

    private fun collectRepo() {
        collectRepoJob?.cancel()
        collectRepoJob = viewModelScope.launch(Dispatchers.IO) {
            repo.flowAll().collect { apps ->
                val packageManager = context.packageManager
                state = ConfigViewState(
                    initialized = true,
                    data = apps.map {
                        val packageInfo =
                            packageManager.getPackageInfoForUid(it.uid) ?: return@map null
                        val applicationInfo = packageInfo.applicationInfo
                        if (it.signature != packageInfo.signature) return@map null
                        ConfigViewState.Data(
                            packageName = packageInfo.packageName,
                            label = applicationInfo.loadLabel(packageManager).toString(),
                            icon = applicationInfo.loadIcon(packageManager),
                            appEntity = it
                        )
                    }.filterNotNull()
                )
            }
        }
    }

    private fun updateConfigByUID(uid: Int, allowApi: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = repo.findByUID(uid) ?: return@launch
            entity.allowApi = allowApi
            if (!allowApi && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.clearDelegatedScopes(entity.uid)
            }
            repo.update(entity)
        }
    }
}