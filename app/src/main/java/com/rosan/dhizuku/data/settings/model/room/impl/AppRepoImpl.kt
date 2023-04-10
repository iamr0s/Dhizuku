package com.rosan.dhizuku.data.settings.model.room.impl

import com.rosan.dhizuku.data.settings.model.room.dao.AppDao
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import com.rosan.dhizuku.data.settings.repo.AppRepo
import kotlinx.coroutines.flow.Flow

class AppRepoImpl(
    private val dao: AppDao
) : AppRepo {
    override fun all(): List<AppEntity> = dao.all()

    override fun flowAll(): Flow<List<AppEntity>> = dao.flowAll()

    override fun find(id: Long): AppEntity? = dao.find(id)

    override fun flowFind(id: Long): Flow<AppEntity?> = dao.flowFind(id)

    override fun findByUID(uid: Int): AppEntity? = dao.findByUID(uid)

    override fun flowFindByUID(uid: Int): Flow<AppEntity?> = dao.flowFindByUID(uid)

    override suspend fun update(entity: AppEntity) {
        entity.modifiedAt = System.currentTimeMillis()
        dao.update(entity)
    }

    override suspend fun insert(entity: AppEntity) {
        entity.createdAt = System.currentTimeMillis()
        entity.modifiedAt = System.currentTimeMillis()
        dao.insert(entity)
    }

    override suspend fun delete(entity: AppEntity) = dao.delete(entity)
}