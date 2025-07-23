package com.rosan.dhizuku.data.settings.model.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "app",
    indices = [
        Index(value = ["uid"], unique = true),
        Index(value = ["allow_api"])
    ]
)
data class AppEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    var id: Long = 0L,
    @ColumnInfo(name = "uid") var uid: Int,
    @ColumnInfo(name = "signature", defaultValue = "") var signature: String,
    @ColumnInfo(name = "allow_api") var allowApi: Boolean,
    @ColumnInfo(name = "blocked", defaultValue = "0") var blocked: Boolean = false,
    @ColumnInfo(name = "created_at") var createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "modified_at") var modifiedAt: Long = System.currentTimeMillis(),
)