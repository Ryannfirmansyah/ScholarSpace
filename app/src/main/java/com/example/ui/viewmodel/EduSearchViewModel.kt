package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.EduSearchDatabase
import com.example.data.model.Course
import com.example.data.model.Scholarship
import com.example.data.network.Content
import com.example.data.network.GeminiClient
import com.example.data.network.Part
import com.example.data.repository.EduSearchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen {
    DASHBOARD, SAVED, ASSISTANT, SETTINGS
}

enum class ItemTab {
    SCHOLARSHIPS, COURSES
}

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val sender: MessageSender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class MessageSender {
    USER, AI
}

class EduSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val database = EduSearchDatabase.getDatabase(application)
    private val repository = EduSearchRepository(database.eduSearchDao())

    // --- Navigation and Active Tabs State ---
    val currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentItemTab = MutableStateFlow(ItemTab.SCHOLARSHIPS)

    // --- Search & Filtering States ---
    val searchQuery = MutableStateFlow("")
    val selectedScholarshipCategory = MutableStateFlow("Semua") // "Semua", "Dalam Negeri", "Luar Negeri", "Pemerintah", "Swasta"
    val selectedCourseCategory = MutableStateFlow("Semua") // "Semua", "Teknologi", "Desain", "Bisnis", "Bahasa"

    // --- Selected Detail States ---
    val activeDetailScholarship = MutableStateFlow<Scholarship?>(null)
    val activeDetailCourse = MutableStateFlow<Course?>(null)

    // --- Dialogs (Form Adding) State ---
    val showAddScholarshipDialog = MutableStateFlow(false)
    val showAddCourseDialog = MutableStateFlow(false)

    // --- Core Database Flows Enriched with UI Categories ---
    val scholarships: StateFlow<List<Scholarship>> = combine(
        repository.allScholarshipsFlow,
        searchQuery,
        selectedScholarshipCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) ||
                    item.provider.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            
            val matchesCategory = category == "Semua" || item.category.equals(category, ignoreCase = true)
            
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val courses: StateFlow<List<Course>> = combine(
        repository.allCoursesFlow,
        searchQuery,
        selectedCourseCategory
    ) { list, query, category ->
        list.filter { item ->
            val matchesQuery = item.title.contains(query, ignoreCase = true) ||
                    item.instructor.contains(query, ignoreCase = true) ||
                    item.platform.contains(query, ignoreCase = true) ||
                    item.description.contains(query, ignoreCase = true)
            
            val matchesCategory = category == "Semua" || item.category.equals(category, ignoreCase = true)
            
            matchesQuery && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Saved Favorites Flows ---
    val savedScholarships: StateFlow<List<Scholarship>> = repository.allScholarshipsFlow.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCourses: StateFlow<List<Course>> = repository.allCoursesFlow.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- AI Assistant Chat State ---
    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Halo! Saya EduSearch AI, asisten akademik pintar Anda. Ada beasiswa atau kelas pelatihan gratis yang ingin Anda tanyakan hari ini?"
            )
        )
    )
    val aiIsLoading = MutableStateFlow(false)

    // --- Dark Mode State ---
    val isDarkMode = MutableStateFlow(false)

    // --- Toggle Favorite Handlers ---
    fun toggleFavoriteScholarship(scholarship: Scholarship) {
        viewModelScope.launch {
            if (scholarship.isFavorite) {
                // If it is a custom scholarship, we delete it from DB entirely. 
                // If preloaded, we delete from DB so it's no longer favorited.
                repository.removeScholarship(scholarship.id)
                // If the detail is active, update it
                if (activeDetailScholarship.value?.id == scholarship.id) {
                    activeDetailScholarship.value = activeDetailScholarship.value?.copy(isFavorite = false)
                }
            } else {
                val favorited = scholarship.copy(isFavorite = true)
                repository.saveScholarship(favorited)
                if (activeDetailScholarship.value?.id == scholarship.id) {
                    activeDetailScholarship.value = activeDetailScholarship.value?.copy(isFavorite = true)
                }
            }
        }
    }

    fun toggleFavoriteCourse(course: Course) {
        viewModelScope.launch {
            if (course.isFavorite) {
                repository.removeCourse(course.id)
                if (activeDetailCourse.value?.id == course.id) {
                    activeDetailCourse.value = activeDetailCourse.value?.copy(isFavorite = false)
                }
            } else {
                val favorited = course.copy(isFavorite = true)
                repository.saveCourse(favorited)
                if (activeDetailCourse.value?.id == course.id) {
                    activeDetailCourse.value = activeDetailCourse.value?.copy(isFavorite = true)
                }
            }
        }
    }

    // --- Delete Custom Listings ---
    fun deleteCustomScholarship(id: String) {
        viewModelScope.launch {
            repository.removeScholarship(id)
            if (activeDetailScholarship.value?.id == id) {
                activeDetailScholarship.value = null
            }
        }
    }

    fun deleteCustomCourse(id: String) {
        viewModelScope.launch {
            repository.removeCourse(id)
            if (activeDetailCourse.value?.id == id) {
                activeDetailCourse.value = null
            }
        }
    }

    // --- Manual Addition Handlers ---
    fun addNewScholarship(
        title: String,
        provider: String,
        benefits: String,
        description: String,
        deadline: String,
        category: String,
        requirements: String,
        link: String
    ) {
        viewModelScope.launch {
            val customSchol = Scholarship(
                id = "custom_schol_${UUID.randomUUID()}",
                title = title,
                provider = provider,
                benefits = benefits,
                description = description,
                deadline = deadline,
                status = "Buka",
                link = if (link.startsWith("http")) link else "https://$link",
                category = category,
                requirements = requirements,
                isCustom = true,
                isFavorite = true
            )
            repository.saveScholarship(customSchol)
            showAddScholarshipDialog.value = false
        }
    }

    fun addNewCourse(
        title: String,
        instructor: String,
        platform: String,
        price: String,
        rating: Double,
        category: String,
        description: String,
        link: String
    ) {
        viewModelScope.launch {
            val customCourse = Course(
                id = "custom_course_${UUID.randomUUID()}",
                title = title,
                instructor = instructor,
                platform = platform,
                price = price,
                rating = rating,
                category = category,
                description = description,
                link = if (link.startsWith("http")) link else "https://$link",
                isCustom = true,
                isFavorite = true
            )
            repository.saveCourse(customCourse)
            showAddCourseDialog.value = false
        }
    }

    // --- Gemini AI Interaction ---
    fun sendChatMessage(text: String) {
        if (text.isBlank() || aiIsLoading.value) return

        val userMsg = ChatMessage(sender = MessageSender.USER, text = text)
        chatMessages.value = chatMessages.value + userMsg
        aiIsLoading.value = true

        viewModelScope.launch {
            // Prepare history for Gemini API
            val history = chatMessages.value.dropLast(1).map {
                val role = if (it.sender == MessageSender.USER) "user" else "model"
                Content(parts = listOf(Part(text = it.text)))
            }

            val reply = GeminiClient.askGemini(text, history)
            
            chatMessages.value = chatMessages.value + ChatMessage(
                sender = MessageSender.AI,
                text = reply
            )
            aiIsLoading.value = false
        }
    }

    fun clearChat() {
        chatMessages.value = listOf(
            ChatMessage(
                sender = MessageSender.AI,
                text = "Riwayat percakapan telah dibersihkan. Ada hal lain yang bisa saya bantu terkait beasiswa, tes TOEFL/IELTS, atau kursus pemrograman?"
            )
        )
    }

    // --- Settings / Options ---
    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
    }
}
