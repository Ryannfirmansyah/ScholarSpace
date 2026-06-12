package com.ryan.scholarspace.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val fullName: String,
    val email: String,
    val username: String,
    val password: String,
    val university: String = "",
    val major: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_scholarships")
data class SavedScholarshipEntity(
    @PrimaryKey val id: String,
    val title: String,
    val provider: String,
    val benefits: String,
    val description: String,
    val deadline: String,
    val status: String, // "Buka" atau "Tutup"
    val link: String,
    val category: String, // "Dalam Negeri", "Luar Negeri", "Pemerintah"
    val requirements: String,
    val isCustom: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "saved_courses")
data class SavedCourseEntity(
    @PrimaryKey val id: String,
    val title: String,
    val instructor: String,
    val platform: String,
    val price: String, // "Gratis" atau nominal rupiah
    val rating: Double,
    val description: String,
    val link: String,
    val category: String, // "Teknologi", "Desain", "Bisnis", "Bahasa"
    val isCustom: Boolean = false,
    val savedAt: Long = System.currentTimeMillis()
)
