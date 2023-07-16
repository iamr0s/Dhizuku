package com.rosan.dhizuku.ui.page.settings.config

import android.content.Context
import android.os.Build
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.data.common.util.clearDelegatedScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ConfigViewModel(
    private var repo: AppRepo
) : ViewModel(), KoinComponent {
    private val context by inject<Context>()

    var state by mutableStateOf(ConfigViewState())
        private set

    fun dispatch(action: ConfigViewAction) {
        when (action) {
            ConfigViewAction.Init -> init()
            is ConfigViewAction.UpdateConfigByUID -> updateConfigByUID(action.uid, action.allowApi)
        }
    }

    private fun init() {
        collectRepo(repo)
    }

    private var collectRepoJob: Job? = null

    private fun collectRepo(repo: AppRepo) {
        this.repo = repo
        collectRepoJob?.cancel()
        collectRepoJob = viewModelScope.launch(Dispatchers.IO) {
            repo.flowAll().collect { apps ->
                val packageManager = context.packageManager
                val map = mutableMapOf<Int, ConfigViewState.Data>()
                apps.forEach {
                    val packageName = packageManager.getPackagesForUid(it.uid)?.first()
                    val packageInfo = kotlin.runCatching {
                        packageName?.let {
                            packageManager.getPackageInfo(
                                it,
                                0
                            )
                        }
                    }.getOrNull()
                    map[it.uid] = if (packageInfo != null) {
                        val applicationInfo = packageInfo.applicationInfo
                        val uid = applicationInfo.uid
                        ConfigViewState.Data(
                            uid = uid,
                            packageName = packageInfo.packageName,
                            label = applicationInfo.loadLabel(packageManager).toString(),
                            icon = applicationInfo.loadIcon(packageManager),
                            allowApi = it.allowApi
                        )
                    } else {
                        ConfigViewState.Data(
                            uid = it.uid,
                            packageName = it.uid.toString(),
                            label = it.uid.toString(),
                            icon = ContextCompat.getDrawable(
                                context, android.R.drawable.sym_def_app_icon
                            )!!,
                            allowApi = it.allowApi
                        )
                    }
                }
                state = ConfigViewState(
                    initialized = true,
                    data = map.values.sortedBy {
                        it.uid
                    })
            }
        }
    }

    private fun updateConfigByUID(uid: Int, allowApi: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val entity = repo.findByUID(uid)
            if (entity == null) {
                repo.insert(
                    AppEntity(
                        uid = uid,
                        allowApi = allowApi
                    )
                )
                return@launch
            }
            entity.allowApi = allowApi
            if (!allowApi && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.clearDelegatedScopes(entity.uid)
            }
            repo.update(entity)
        }
    }
}