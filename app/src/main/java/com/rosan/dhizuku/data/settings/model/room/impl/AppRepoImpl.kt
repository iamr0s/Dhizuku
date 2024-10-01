package com.rosan.dhizuku.data.settings.model.room.impl

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.os.Build
import com.rosan.dhizuku.data.settings.model.room.dao.AppDao
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import com.rosan.dhizuku.server.DhizukuState
import kotlinx.coroutines.flow.Flow
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AppRepoImpl(
    private val dao: AppDao
) : AppRepo, KoinComponent {
    private val context by inject<Context>()

    private val packageManager by lazy { context.packageManager }

    private val devicePolicyManager by lazy {
        context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
    }

    override fun flowAll(): Flow<List<AppEntity>> = dao.flowAll()

    override fun flowFind(id: Long): Flow<AppEntity?> = dao.flowFind(id)

    override fun flowFindByUID(uid: Int): Flow<AppEntity?> = dao.flowFindByUID(uid)

    override suspend fun all(): List<AppEntity> = dao.all()

    override suspend fun find(id: Long): AppEntity? = dao.find(id)

    override suspend fun findByUID(uid: Int): AppEntity? = dao.findByUID(uid)

    override suspend fun update(entity: AppEntity) {
        if (!entity.allowApi) {
            clearDelegatedScopes(entity)
        }
        entity.modifiedAt = System.currentTimeMillis()
        dao.update(entity)
    }

    override suspend fun insert(entity: AppEntity) {
        entity.createdAt = System.currentTimeMillis()
        entity.modifiedAt = System.currentTimeMillis()
        dao.insert(entity)
    }

    override suspend fun delete(entity: AppEntity) {
        clearDelegatedScopes(entity)
        dao.delete(entity)
    }

    private fun clearDelegatedScopes(entity: AppEntity) {
        if (!DhizukuState.state.owner) return
        val packageNames = packageManager.getPackagesForUid(entity.uid) ?: emptyArray()
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        packageNames.forEach {
            kotlin.runCatching {
                devicePolicyManager.setDelegatedScopes(
                    DhizukuState.component,
                    it,
                    emptyList()
                )
            }
        }
    }
}