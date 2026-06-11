package com.ryan.scholarspace.data.model

import com.ryan.scholarspace.data.database.SavedCourseEntity
import com.ryan.scholarspace.data.database.SavedScholarshipEntity

data class Scholarship(
    val id: String,
    val title: String,
    val provider: String,
    val benefits: String,
    val description: String,
    val deadline: String,
    val status: String, // "Buka" atau "Tutup"
    val link: String,
    val category: String, // "Dalam Negeri", "Luar Negeri", "Pemerintah", "Swasta"
    val requirements: String,
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
) {
    fun toEntity(): SavedScholarshipEntity {
        return SavedScholarshipEntity(
            id = id,
            title = title,
            provider = provider,
            benefits = benefits,
            description = description,
            deadline = deadline,
            status = status,
            link = link,
            category = category,
            requirements = requirements,
            isCustom = isCustom
        )
    }
}

data class Course(
    val id: String,
    val title: String,
    val instructor: String,
    val platform: String,
    val price: String,
    val rating: Double,
    val description: String,
    val link: String,
    val category: String, // "Teknologi", "Desain", "Bisnis", "Bahasa"
    val isCustom: Boolean = false,
    val isFavorite: Boolean = false
) {
    fun toEntity(): SavedCourseEntity {
        return SavedCourseEntity(
            id = id,
            title = title,
            instructor = instructor,
            platform = platform,
            price = price,
            rating = rating,
            description = description,
            link = link,
            category = category,
            isCustom = isCustom
        )
    }
}
