# 📚 ScholarSpace — Platform Beasiswa & Kursus Online

> **Final Project Lab Mobile 2026** | Ryan Firmansyah | NIM: H071241082 | Universitas Hasanuddin

[![Android](https://img.shields.io/badge/Android-API%2024%2B-green)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0-blue)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material3-purple)](https://developer.android.com/jetpack/compose)
[![Room](https://img.shields.io/badge/Room-SQLite-orange)](https://developer.android.com/jetpack/androidx/releases/room)

---

## 📖 Deskripsi Aplikasi

**ScholarSpace** adalah aplikasi Android modern untuk mahasiswa Indonesia yang ingin menemukan peluang **beasiswa** dan **kursus online** terbaik. Aplikasi ini mengambil berita pendidikan secara real-time dari API eksternal via Retrofit, menyimpan favorit secara lokal menggunakan Room Database (SQLite), dan dilengkapi **Asisten AI berbasis Google Gemini** untuk menjawab pertanyaan seputar beasiswa dan strategi belajar.

Tema: **Pendidikan** — ScholarSpace = Scholar (beasiswa) + Space (ruang belajar).

---

## ✨ Fitur Lengkap

| Fitur | Deskripsi |
|-------|-----------|
| 🎓 **Direktori Beasiswa** | 6+ beasiswa preloaded (LPDP, Chevening, Erasmus, MEXT, Unggulan Kemendikbud, Djarum Plus) + tambah manual |
| 📖 **Direktori Kursus** | Katalog kursus dari Dicoding, Udemy, Coursera dengan rating & instruktur |
| 🔖 **Bookmark Offline** | Simpan beasiswa & kursus ke Room Database — tetap tampil saat offline |
| 🤖 **AI Asisten (Gemini)** | Chat real-time dengan Google Gemini AI, fallback offline berbasis keyword |
| 📰 **Berita Pendidikan** | Berita live via Retrofit + RapidAPI NewsAPI, dengan tombol Retry saat offline |
| ➕ **Tambah Manual** | Form input untuk menambah beasiswa/kursus kustom ke database |
| 🌙 **Dark / Light Mode** | Toggle tema gelap/terang, tersimpan persisten via SharedPreferences |
| 🎨 **Splash Screen** | Animasi pembuka elegan dengan bounce + fade + gradient indigo |
| 🔍 **Search Real-time** | Filter beasiswa/kursus berdasarkan judul, provider, deskripsi |
| 🏷️ **Filter Kategori** | Chip selector: Dalam Negeri, Luar Negeri, Pemerintah, Swasta, Teknologi, dst. |

---

## ✅ Ketentuan Teknis Lab (Checklist)

| No | Ketentuan Wajib | Status | Detail Implementasi |
|----|-----------------|--------|---------------------|
| 1 | **Min. 2 Activity** | ✅ | `SplashActivity` (launcher) + `MainActivity` |
| 2 | **Intent navigasi antar Activity** | ✅ | `SplashActivity` → `MainActivity` via `Intent(this, MainActivity::class.java)` |
| 3 | **RecyclerView / LazyColumn** | ✅ | `LazyColumn` di Dashboard, Tersimpan, AI Asisten, Berita |
| 4 | **Min. 2 Screen + Navigation** | ✅ | 5 screen: Eksplor, Tersimpan, AI Asisten, Berita, Profil — bottom nav bar |
| 5 | **Background Thread (Executor/Handler)** | ✅ | `Handler.postDelayed()` di `SplashActivity` + `Executors.newSingleThreadExecutor()` di `ScholarSpaceViewModel` |
| 6 | **Networking Retrofit + tombol Retry** | ✅ | `NewsApiClient` + `GeminiClient` (Retrofit/OkHttp) + tombol "Coba Lagi" di `NewsScreen` |
| 7 | **SQLite / SharedPreferences** | ✅ | Room Database (SQLite) untuk beasiswa/kursus + SharedPreferences untuk dark mode |
| 8 | **Data tampil saat offline** | ✅ | Bookmark & data custom tetap ter-load dari Room DB tanpa koneksi internet |
| 9 | **Dark Mode & Light Mode toggle** | ✅ | Switch di SettingsScreen, state persisten via `SharedPreferences` |
| 10 | **GitHub repo + README lengkap** | ✅ | Repo ini |
| 11 | **APK bisa diinstall, tidak crash** | ✅ | Build SUCCESS, package `com.ryan.scholarspace` |

---

## 🏗️ Arsitektur & Struktur Project

Pola arsitektur: **MVVM (Model-View-ViewModel)** dengan Kotlin Coroutines + StateFlow.

```
ScholarSpace/
│
├── app/src/main/java/com/ryan/scholarspace/
│   ├── SplashActivity.kt          ← Activity 1: Splash animasi, Handler navigation
│   ├── MainActivity.kt            ← Activity 2: Semua UI screen & komponen Compose
│   ├── NewsScreen.kt              ← Composable screen berita (Retrofit)
│   │
│   ├── data/
│   │   ├── database/
│   │   │   ├── ScholarSpaceDatabase.kt   ← Room DB (SQLite)
│   │   │   ├── ScholarSpaceDao.kt        ← DAO queries
│   │   │   └── Entities.kt              ← Table entities
│   │   │
│   │   ├── model/
│   │   │   └── Models.kt                ← Data class Scholarship & Course
│   │   │
│   │   ├── network/
│   │   │   ├── GeminiClient.kt          ← Google Gemini AI (Retrofit)
│   │   │   └── NewsApiClient.kt         ← NewsAPI via RapidAPI (Retrofit)
│   │   │
│   │   └── repository/
│   │       └── ScholarSpaceRepository.kt ← Repository + preloaded data
│   │
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt     ← Skema warna indigo (light & dark)
│       │   ├── Theme.kt     ← MaterialTheme setup
│       │   └── Type.kt      ← Skala tipografi Material3
│       │
│       └── viewmodel/
│           └── ScholarSpaceViewModel.kt  ← AndroidViewModel, StateFlow, Executor, SharedPrefs
│
├── .env                  ← API keys (tidak di-commit)
├── .env.example          ← Template konfigurasi API
└── README.md
```

---

## 🔧 Penjelasan Teknis

### 1. Dua Activity + Intent Navigation
```kotlin
// SplashActivity.kt — Activity 1 (Launcher)
class SplashActivity : ComponentActivity() {
    private val splashHandler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Handler: delay 2.8 detik, lalu navigate ke MainActivity
        splashHandler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2800L)
    }
}

// MainActivity.kt — Activity 2 (Main App)
class MainActivity : ComponentActivity() { ... }
```

### 2. Background Thread — Executor & Handler
```kotlin
// Executor di ScholarSpaceViewModel.kt
private val backgroundExecutor = Executors.newSingleThreadExecutor()

private fun runBackgroundDataInit() {
    backgroundExecutor.execute {
        // Background thread: inisialisasi data, log preferensi
        Log.d("ScholarSpace", "Executor: ${repository.preloadedScholarships.size} beasiswa siap")
    }
}

// Handler di SplashActivity.kt
private val splashHandler = Handler(Looper.getMainLooper())
splashHandler.postDelayed({ startActivity(...) }, 2800L)
```

### 3. Room Database (SQLite)
```kotlin
// Entities: saved_scholarships & saved_courses tables
@Entity(tableName = "saved_scholarships")
data class SavedScholarshipEntity(@PrimaryKey val id: String, ...)

// DAO dengan Flow untuk reactive updates
@Query("SELECT * FROM saved_scholarships ORDER BY savedAt DESC")
fun getSavedScholarshipsFlow(): Flow<List<SavedScholarshipEntity>>
```

### 4. Retrofit Networking
```kotlin
// NewsApiClient.kt — Retrofit service
interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEducationNews(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Query("q") query: String,
        ...
    ): NewsResponse
}
```

### 5. SharedPreferences (Dark Mode Persistence)
```kotlin
// ScholarSpaceViewModel.kt
private val prefs = application.getSharedPreferences("scholarspace_prefs", Context.MODE_PRIVATE)
val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))

fun toggleDarkMode() {
    isDarkMode.value = !isDarkMode.value
    prefs.edit().putBoolean("dark_mode", isDarkMode.value).apply()
}
```

### 6. Offline Data dari Room
```kotlin
// Repository: combine preloaded + DB favorit, isFavorite = true jika ada di DB
val allScholarshipsFlow: Flow<List<Scholarship>> = savedScholarshipsFlow.map { savedList ->
    val savedMap = savedList.associateBy { it.id }
    preloadedScholarships.map { pre -> pre.copy(isFavorite = savedMap[pre.id] != null) } +
    savedList.filter { it.isCustom }.map { it.toScholarship() }
}
```

---

## 🎨 Desain UI

- **Color Scheme**: Indigo — Light `#4F46E5`, Dark `#818CF8`
- **Background Dark**: `#0F172A` (Slate-900), Surface: `#1E293B` (Slate-800)
- **Cards**: Rounded corner 16dp, shadow elevation 2dp, left accent bar 4dp
- **Gradient headers**: Indigo `primary → secondary` di setiap screen
- **Animations**: Spring bounce (logo), fade transition antar screen, pulse dots (loading)
- **Edge-to-Edge**: Status bar & navigation bar terintegrasi dengan UI

---

## 🚀 Cara Build & Jalankan

### Prasyarat
- Android Studio Hedgehog 2023.1.1 atau lebih baru
- JDK 11+
- Android SDK 35 (target), min SDK 24

### Steps

```bash
# 1. Clone repo
git clone https://github.com/RyanFirmansyah/ScholarSpace.git
cd ScholarSpace

# 2. (Opsional) Setup Gemini API Key
cp .env.example .env
# Edit .env: GEMINI_API_KEY=YOUR_KEY_FROM_AISTUDIO

# 3. Build debug APK
./gradlew assembleDebug

# 4. Install ke device
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Atau buka di Android Studio
1. File → Open → pilih folder `ScholarSpace`
2. Tunggu Gradle sync selesai
3. Klik Run ▶

---

## 📦 Dependencies

```kotlin
// Compose + Material3
implementation(platform(libs.androidx.compose.bom))
implementation(libs.androidx.compose.material3)
implementation(libs.androidx.compose.material.icons.extended)

// Room Database
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
ksp(libs.androidx.room.compiler)

// Retrofit + Moshi
implementation(libs.retrofit)
implementation(libs.converter.moshi)
implementation(libs.moshi.kotlin)
implementation(libs.okhttp)
implementation(libs.logging.interceptor)

// Lifecycle + ViewModel
implementation(libs.androidx.lifecycle.viewmodel.compose)
implementation(libs.kotlinx.coroutines.android)
```

---

## 🔑 API Keys

| API | Sumber | Keterangan |
|-----|--------|------------|
| Gemini AI | [Google AI Studio](https://aistudio.google.com/) | Masukkan di `.env` sebagai `GEMINI_API_KEY` |
| NewsAPI | [RapidAPI](https://rapidapi.com/) | Sudah termasuk di source code (free tier) |

> ⚠️ File `.env` sudah di-`.gitignore`. Gunakan `.env.example` sebagai template.

---

## 👤 Developer

| | |
|-|-|
| **Nama** | Ryan Firmansyah |
| **NIM** | H071241082 |
| **Program Studi** | Sistem Informasi |
| **Universitas** | Universitas Hasanuddin (Unhas) |
| **Semester** | 4 (2026) |
| **Mata Kuliah** | Praktikum Pemrograman Mobile |

---

*Dibuat dengan Kotlin + Jetpack Compose + Material3 ❤️*
