package com.rosan.dhizuku.data.settings.repo

import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import kotlinx.coroutines.flow.Flow

interface AppRepo {
    fun all(): List<AppEntity>

    fun flowAll(): Flow<List<AppEntity>>

    fun find(id: Long): AppEntity?

    fun flowFind(id: Long): Flow<AppEntity?>

    fun findByUID(uid: Int): AppEntity?

    fun flowFindByUID(uid: Int): Flow<AppEntity?>

    suspend fun update(entity: AppEntity)

    suspend fun insert(entity: AppEntity)

    suspend fun delete(entity: AppEntity)
}