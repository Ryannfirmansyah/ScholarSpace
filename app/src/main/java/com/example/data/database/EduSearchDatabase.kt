package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        SavedScholarshipEntity::class,
        SavedCourseEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class EduSearchDatabase : RoomDatabase() {
    
    abstract fun eduSearchDao(): EduSearchDao

    companion object {
        @Volatile
        private var INSTANCE: EduSearchDatabase? = null

        fun getDatabase(context: Context): EduSearchDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EduSearchDatabase::class.java,
                    "edusearch_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
