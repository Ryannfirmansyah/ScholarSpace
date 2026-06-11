package com.ryan.scholarspace.data.database

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
abstract class ScholarSpaceDatabase : RoomDatabase() {
    
    abstract fun ScholarSpaceDao(): ScholarSpaceDao

    companion object {
        @Volatile
        private var INSTANCE: ScholarSpaceDatabase? = null

        fun getDatabase(context: Context): ScholarSpaceDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ScholarSpaceDatabase::class.java,
                    "scholarspace_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
