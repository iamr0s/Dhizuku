package com.rosan.dhizuku.data.settings.repo

import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import kotlinx.coroutines.flow.Flow

interface AppRepo {
    fun flowAll(): Flow<List<AppEntity>>

    fun flowFind(id: Long): Flow<AppEntity?>

    fun flowFindByUID(uid: Int): Flow<AppEntity?>

    suspend fun all(): List<AppEntity>

    suspend fun find(id: Long): AppEntity?

    suspend fun findByUID(uid: Int): AppEntity?

    suspend fun update(entity: AppEntity)

    suspend fun insert(entity: AppEntity)

    suspend fun delete(entity: AppEntity)
}