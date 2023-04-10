package com.rosan.dhizuku.data.settings.model.room

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.rosan.dhizuku.data.settings.model.room.dao.AppDao
import com.rosan.dhizuku.data.settings.model.room.entity.AppEntity
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

@Database(
    entities = [AppEntity::class],
    version = 1,
    exportSchema = false
)
abstract class DhizukuRoom : RoomDatabase() {
    companion object : KoinComponent {
        fun createInstance(): DhizukuRoom {
            return Room.databaseBuilder(
                get(),
                DhizukuRoom::class.java,
                "dhizuku.db",
            ).build()
        }
    }

    abstract val appDao: AppDao
}