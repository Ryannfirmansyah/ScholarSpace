package com.ryan.scholarspace.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ryan.scholarspace.data.database.ScholarSpaceDatabase
import com.ryan.scholarspace.data.model.Course
import com.ryan.scholarspace.data.model.Scholarship
import com.ryan.scholarspace.data.network.Content
import com.ryan.scholarspace.data.network.GeminiClient
import com.ryan.scholarspace.data.network.NewsApiClient
import com.ryan.scholarspace.data.network.NewsArticle
import com.ryan.scholarspace.data.network.Part
import com.ryan.scholarspace.data.repository.ScholarSpaceRepository
import kotlinx.coroutines.flow.*
import com.ryan.scholarspace.data.database.UserEntity
import kotlinx.coroutines.launch
import java.util.UUID
import java.util.concurrent.Executors

enum class AppScreen {
    DASHBOARD, SAVED, ASSISTANT, NEWS, SETTINGS, PROFILE
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

sealed class NewsState {
    object Loading : NewsState()
    data class Success(val articles: List<NewsArticle>) : NewsState()
    data class Error(val message: String) : NewsState()
}

class ScholarSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ScholarSpaceDatabase.getDatabase(application)
    private val repository = ScholarSpaceRepository(database.ScholarSpaceDao())
    private val prefs = application.getSharedPreferences("scholarspace_prefs", Context.MODE_PRIVATE)

    // Executor for explicit background thread operations (fulfills lab requirement)
    private val backgroundExecutor = Executors.newSingleThreadExecutor()

    val currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentItemTab = MutableStateFlow(ItemTab.SCHOLARSHIPS)
    val searchQuery = MutableStateFlow("")
    val selectedScholarshipCategory = MutableStateFlow("Semua")
    val selectedCourseCategory = MutableStateFlow("Semua")
    val activeDetailScholarship = MutableStateFlow<Scholarship?>(null)
    val activeDetailCourse = MutableStateFlow<Course?>(null)
    val showAddScholarshipDialog = MutableStateFlow(false)
    val showAddCourseDialog = MutableStateFlow(false)

    // Dark mode state persisted via SharedPreferences
    val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))

    // --- Auth State ---
    val currentUser = MutableStateFlow<UserEntity?>(null)
    val isLoggedIn = MutableStateFlow(false)

    // --- News State (Retrofit API) ---
    val newsState = MutableStateFlow<NewsState>(NewsState.Loading)

    val scholarships: StateFlow<List<Scholarship>> = combine(
        repository.allScholarshipsFlow, searchQuery, selectedScholarshipCategory
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
        repository.allCoursesFlow, searchQuery, selectedCourseCategory
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

    val savedScholarships: StateFlow<List<Scholarship>> = repository.allScholarshipsFlow.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val savedCourses: StateFlow<List<Course>> = repository.allCoursesFlow.map { list ->
        list.filter { it.isFavorite }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage(sender = MessageSender.AI,
            text = "Halo! Saya ScholarSpace AI, asisten akademik pintar Anda. Ada beasiswa atau kelas pelatihan gratis yang ingin Anda tanyakan hari ini?"))
    )
    val aiIsLoading = MutableStateFlow(false)

    init {
        val savedUserId = prefs.getString("logged_in_user_id", null)
        if (savedUserId != null) {
            viewModelScope.launch {
                val user = repository.getUserById(savedUserId)
                if (user != null) { currentUser.value = user; isLoggedIn.value = true }
            }
        }
        fetchNews()
        runBackgroundDataInit()
    }

    // Explicit Executor background thread usage (lab requirement: Executor/Handler)
    private fun runBackgroundDataInit() {
        backgroundExecutor.execute {
            android.util.Log.d("ScholarSpace", "[Executor] Background init: ${repository.preloadedScholarships.size} beasiswa, ${repository.preloadedCourses.size} kursus siap.")
            val savedDarkMode = prefs.getBoolean("dark_mode", false)
            android.util.Log.d("ScholarSpace", "[Executor] Preferensi tema dimuat: darkMode=$savedDarkMode")
        }
    }

    // --- Fetch News dari API (Retrofit) ---
    fun fetchNews() {
        viewModelScope.launch {
            newsState.value = NewsState.Loading
            val result = NewsApiClient.getEducationNews()
            newsState.value = result.fold(
                onSuccess = { articles -> NewsState.Success(articles) },
                onFailure = { e -> NewsState.Error(e.message ?: "Gagal memuat berita") }
            )
        }
    }

    fun toggleFavoriteScholarship(scholarship: Scholarship) {
        viewModelScope.launch {
            if (scholarship.isFavorite) {
                repository.removeScholarship(scholarship.id)
                if (activeDetailScholarship.value?.id == scholarship.id)
                    activeDetailScholarship.value = activeDetailScholarship.value?.copy(isFavorite = false)
            } else {
                repository.saveScholarship(scholarship.copy(isFavorite = true))
                if (activeDetailScholarship.value?.id == scholarship.id)
                    activeDetailScholarship.value = activeDetailScholarship.value?.copy(isFavorite = true)
            }
        }
    }

    fun toggleFavoriteCourse(course: Course) {
        viewModelScope.launch {
            if (course.isFavorite) {
                repository.removeCourse(course.id)
                if (activeDetailCourse.value?.id == course.id)
                    activeDetailCourse.value = activeDetailCourse.value?.copy(isFavorite = false)
            } else {
                repository.saveCourse(course.copy(isFavorite = true))
                if (activeDetailCourse.value?.id == course.id)
                    activeDetailCourse.value = activeDetailCourse.value?.copy(isFavorite = true)
            }
        }
    }

    fun deleteCustomScholarship(id: String) {
        viewModelScope.launch {
            repository.removeScholarship(id)
            if (activeDetailScholarship.value?.id == id) activeDetailScholarship.value = null
        }
    }

    fun deleteCustomCourse(id: String) {
        viewModelScope.launch {
            repository.removeCourse(id)
            if (activeDetailCourse.value?.id == id) activeDetailCourse.value = null
        }
    }

    fun addNewScholarship(title: String, provider: String, benefits: String, description: String,
                          deadline: String, category: String, requirements: String, link: String) {
        viewModelScope.launch {
            repository.saveScholarship(Scholarship(
                id = "custom_schol_${UUID.randomUUID()}", title = title, provider = provider,
                benefits = benefits, description = description, deadline = deadline, status = "Buka",
                link = if (link.startsWith("http")) link else "https://$link",
                category = category, requirements = requirements, isCustom = true, isFavorite = true
            ))
            showAddScholarshipDialog.value = false
        }
    }

    fun addNewCourse(title: String, instructor: String, platform: String, price: String,
                     rating: Double, category: String, description: String, link: String) {
        viewModelScope.launch {
            repository.saveCourse(Course(
                id = "custom_course_${UUID.randomUUID()}", title = title, instructor = instructor,
                platform = platform, price = price, rating = rating, category = category,
                description = description, link = if (link.startsWith("http")) link else "https://$link",
                isCustom = true, isFavorite = true
            ))
            showAddCourseDialog.value = false
        }
    }

    fun sendChatMessage(text: String) {
        if (text.isBlank() || aiIsLoading.value) return
        chatMessages.value = chatMessages.value + ChatMessage(sender = MessageSender.USER, text = text)
        aiIsLoading.value = true
        viewModelScope.launch {
            val history = chatMessages.value.dropLast(1).map {
                Content(parts = listOf(Part(text = it.text)))
            }
            val reply = GeminiClient.askGemini(text, history)
            chatMessages.value = chatMessages.value + ChatMessage(sender = MessageSender.AI, text = reply)
            aiIsLoading.value = false
        }
    }

    fun clearChat() {
        chatMessages.value = listOf(ChatMessage(sender = MessageSender.AI,
            text = "Riwayat percakapan telah dibersihkan. Ada hal lain yang bisa saya bantu?"))
    }

    fun toggleDarkMode() {
        isDarkMode.value = !isDarkMode.value
        prefs.edit().putBoolean("dark_mode", isDarkMode.value).apply()
    }

    fun login(email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByEmail(email.trim().lowercase())
            when {
                user == null -> onError("Email tidak terdaftar")
                user.password != password -> onError("Password salah")
                else -> {
                    currentUser.value = user
                    isLoggedIn.value = true
                    prefs.edit().putString("logged_in_user_id", user.id).apply()
                    onSuccess()
                }
            }
        }
    }

    fun register(fullName: String, email: String, username: String, password: String,
                 university: String, major: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            if (repository.isEmailRegistered(email.trim().lowercase())) {
                onError("Email sudah terdaftar"); return@launch
            }
            val user = UserEntity(
                id = UUID.randomUUID().toString(),
                fullName = fullName.trim(), email = email.trim().lowercase(),
                username = username.trim(), password = password,
                university = university.trim(), major = major.trim()
            )
            repository.registerUser(user)
            currentUser.value = user
            isLoggedIn.value = true
            prefs.edit().putString("logged_in_user_id", user.id).apply()
            onSuccess()
        }
    }

    fun updateProfile(fullName: String, university: String, major: String) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            val updated = user.copy(fullName = fullName.trim(), university = university.trim(), major = major.trim())
            repository.updateUser(updated)
            currentUser.value = updated
        }
    }

    fun logout() {
        currentUser.value = null
        isLoggedIn.value = false
        prefs.edit().remove("logged_in_user_id").apply()
        currentScreen.value = AppScreen.DASHBOARD
    }

    override fun onCleared() {
        super.onCleared()
        backgroundExecutor.shutdown()
    }
}
