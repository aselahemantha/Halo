package com.exoticstech.halo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [AlarmEntity::class, AlarmHistoryEntity::class],
    version = 7,
    exportSchema = true
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun alarmHistoryDao(): AlarmHistoryDao

    companion object {
        /**
         * Migrations registry. Add new migrations here as the schema evolves.
         * 
         * Example for a future version 7 -> 8 migration:
         * val MIGRATION_7_8 = object : Migration(7, 8) {
         *     override fun migrate(db: SupportSQLiteDatabase) {
         *         db.execSQL("ALTER TABLE alarms ADD COLUMN newColumn TEXT DEFAULT ''")
         *     }
         * }
         *
         * Then add it to the database builder in AppModule.kt:
         * .addMigrations(AlarmDatabase.MIGRATION_7_8)
         */
        val ALL_MIGRATIONS: Array<Migration> = arrayOf(
            // Add migrations here as they're created
        )
    }
}
