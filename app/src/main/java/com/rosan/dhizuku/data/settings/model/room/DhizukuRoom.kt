package com.rosan.dhizuku.data.settings.model.room

import android.content.Context
import android.os.Build

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

import com.rosan.dhizuku.data.settings.model.room.dao.AppDao
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity

import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Database(
    entities = [AppEntity::class],
    version = 3,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
        AutoMigration(from = 2, to = 3)
    ]
)
abstract class DhizukuRoom : RoomDatabase() {
    companion object : KoinComponent {
        fun createInstance(): DhizukuRoom {
            var context: Context = get()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                && !context.isDeviceProtectedStorage
            ) {
                context = context.createDeviceProtectedStorageContext()
            }
            return Room.databaseBuilder(
                context,
                DhizukuRoom::class.java,
                "dhizuku.db",
            ).build()
        }
    }

    abstract val appDao: AppDao
}