package com.rosan.dhizuku.data.settings.model.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity

import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {
    @Query("select * from app")
    fun flowAll(): Flow<List<AppEntity>>

    @Query("select * from app where id = :id limit 1")
    fun flowFind(id: Long): Flow<AppEntity?>

    @Query("select * from app where uid = :uid limit 1")
    fun flowFindByUID(uid: Int): Flow<AppEntity?>

    @Query("select * from app")
    suspend fun all(): List<AppEntity>

    @Query("select * from app where id = :id limit 1")
    suspend fun find(id: Long): AppEntity?

    @Query("select * from app where uid = :uid limit 1")
    suspend fun findByUID(uid: Int): AppEntity?

    @Update
    suspend fun update(entity: AppEntity)

    @Insert
    suspend fun insert(entity: AppEntity)

    @Delete
    suspend fun delete(entity: AppEntity)
}