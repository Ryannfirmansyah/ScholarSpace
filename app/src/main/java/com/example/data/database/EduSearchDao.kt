package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface EduSearchDao {
    
    // --- Scholarships ---
    @Query("SELECT * FROM saved_scholarships ORDER BY savedAt DESC")
    fun getSavedScholarshipsFlow(): Flow<List<SavedScholarshipEntity>>
    
    @Query("SELECT * FROM saved_scholarships WHERE id = :id LIMIT 1")
    suspend fun getScholarshipById(id: String): SavedScholarshipEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScholarship(scholarship: SavedScholarshipEntity)
    
    @Query("DELETE FROM saved_scholarships WHERE id = :id")
    suspend fun deleteScholarshipById(id: String)
    
    @Query("DELETE FROM saved_scholarships")
    suspend fun clearAllScholarships()

    // --- Courses ---
    @Query("SELECT * FROM saved_courses ORDER BY savedAt DESC")
    fun getSavedCoursesFlow(): Flow<List<SavedCourseEntity>>
    
    @Query("SELECT * FROM saved_courses WHERE id = :id LIMIT 1")
    suspend fun getCourseById(id: String): SavedCourseEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: SavedCourseEntity)
    
    @Query("DELETE FROM saved_courses WHERE id = :id")
    suspend fun deleteCourseById(id: String)
    
    @Query("DELETE FROM saved_courses")
    suspend fun clearAllCourses()
}
