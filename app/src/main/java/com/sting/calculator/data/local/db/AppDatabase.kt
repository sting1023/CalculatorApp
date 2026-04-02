package com.sting.calculator.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.sting.calculator.data.local.db.dao.HistoryDao
import com.sting.calculator.data.local.db.dao.SettingsDao
import com.sting.calculator.data.local.db.entity.HistoryEntity
import com.sting.calculator.data.local.db.entity.SettingsEntity

@Database(
    entities = [HistoryEntity::class, SettingsEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): HistoryDao
    abstract fun settingsDao(): SettingsDao
}
