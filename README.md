# ScholarSpace

> **Platform Beasiswa & Kursus Online untuk Mahasiswa Indonesia**

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" />
  <img src="https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Material3-4F46E5?style=for-the-badge&logo=jetpackcompose&logoColor=white" />
  <img src="https://img.shields.io/badge/Min%20SDK-24-brightgreen?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Target%20SDK-35-blue?style=for-the-badge" />
</p>

<p align="center">
  <b>Final Project · Praktikum Pemrograman Mobile 2026</b><br/>
  Ryan Firmansyah · NIM H071241082 · Universitas Hasanuddin
</p>

---

## Tentang Aplikasi

**ScholarSpace** adalah aplikasi Android modern yang membantu mahasiswa Indonesia menemukan peluang beasiswa dan kursus online terbaik — semua dalam satu tempat. Nama ini berasal dari **Scholar** (beasiswa) + **Space** (ruang belajar).

Aplikasi ini dibangun menggunakan **Kotlin + Jetpack Compose** dengan arsitektur **MVVM**, dilengkapi:
- Database lokal **Room (SQLite)** untuk menyimpan favorit secara offline
- Jaringan **Retrofit** untuk mengambil berita pendidikan dan AI response secara real-time
- **Asisten AI** berbasis Google Gemini untuk menjawab pertanyaan seputar beasiswa
- Sistem **autentikasi** (Register & Login) dengan persistensi akun

---

## Tampilan Aplikasi

| Dashboard | Beasiswa | Kursus |
|---|---|---|
| Search terintegrasi di hero gradient, Stats Row, colored category cards | Header strip berwarna per kategori (Pemerintah, Luar Negeri, Dalam Negeri, Swasta) | Header strip berwarna per platform (Dicoding, Udemy, Coursera), rating bintang |

| AI Asisten | Berita | Profil |
|---|---|---|
| Chat real-time dengan Gemini, fallback keyword offline | Berita pendidikan live via RapidAPI, tombol Retry | Foto profil, edit nama/universitas/jurusan, statistik |

---

## Fitur Lengkap

| Fitur | Deskripsi |
|---|---|
| 🎓 **Direktori Beasiswa** | 6+ beasiswa preloaded (LPDP, Chevening, Erasmus+, MEXT, Unggulan, Djarum Plus) + tambah manual |
| 📖 **Direktori Kursus** | Katalog dari Dicoding, Udemy, Coursera dengan rating bintang & instruktur |
| 🔖 **Bookmark Offline** | Simpan beasiswa & kursus ke Room Database — tetap tampil saat tanpa internet |
| 🤖 **AI Asisten Gemini** | Chat real-time dengan Google Gemini AI, fallback offline berbasis keyword |
| 📰 **Berita Pendidikan** | Berita live via Retrofit + RapidAPI, dengan tombol Retry dan indikator loading |
| ➕ **Tambah Data Manual** | Form input untuk menambah beasiswa/kursus kustom ke database |
| 🔐 **Login & Register** | Sistem autentikasi akun lokal via Room Database |
| 🖼️ **Foto Profil** | Pilih & simpan foto dari galeri, update real-time tanpa reload |
| 🌙 **Dark / Light Mode** | Toggle tema, state tersimpan persisten via SharedPreferences |
| 🎨 **Splash Screen** | Animasi pembuka dengan bounce + fade + gradient indigo |
| 🔍 **Search Real-time** | Filter beasiswa/kursus langsung dari hero berdasarkan judul, provider, deskripsi |
| 🏷️ **Filter Kategori** | Chip selector kategori: Dalam Negeri, Luar Negeri, Pemerintah, Swasta, dll. |
| 📊 **Quick Stats** | Ringkasan jumlah Beasiswa, Kursus, dan yang Terbuka di dashboard |

---

## Pemenuhan Ketentuan Teknis Lab

| No | Ketentuan | Status | Implementasi |
|---|---|---|---|
| 1 | Min. 2 Activity | ✅ | `SplashActivity` (launcher) + `MainActivity` (main app) |
| 2 | Intent navigasi antar Activity | ✅ | `SplashActivity → MainActivity` via `Intent(this, MainActivity::class.java)` |
| 3 | RecyclerView / LazyColumn | ✅ | `LazyColumn` di Dashboard, Tersimpan, AI Asisten, Berita |
| 4 | Min. 2 Screen + Navigation | ✅ | 5 screen utama — bottom navigation bar |
| 5 | Background Thread (Executor/Handler) | ✅ | `Executors.newSingleThreadExecutor()` di ViewModel + `Handler.postDelayed()` di Splash |
| 6 | Networking Retrofit + tombol Retry | ✅ | `NewsApiClient` + `GeminiClient` via Retrofit/OkHttp + tombol "Coba Lagi" |
| 7 | SQLite / SharedPreferences | ✅ | Room Database (SQLite) untuk data + SharedPreferences untuk tema & session |
| 8 | Data tampil saat offline | ✅ | Bookmark & data custom tetap ter-load dari Room DB |
| 9 | Dark Mode & Light Mode | ✅ | Toggle di Profil, state persisten via `SharedPreferences` |
| 10 | GitHub repo + README lengkap | ✅ | Repo ini |
| 11 | APK bisa diinstall, tidak crash | ✅ | Build SUCCESS, package `com.ryan.scholarspace`, min SDK 24 |

---

## Arsitektur

Pola arsitektur: **MVVM (Model-View-ViewModel)** dengan Kotlin Coroutines dan StateFlow.

```
ScholarSpace/
│
├── app/src/main/java/com/ryan/scholarspace/
│   ├── SplashActivity.kt          ← Activity 1: Animasi splash, Handler navigation
│   ├── MainActivity.kt            ← Activity 2: Semua UI screen & Compose components
│   ├── NewsScreen.kt              ← Screen berita (Retrofit)
│   │
│   ├── data/
│   │   ├── database/
│   │   │   ├── ScholarSpaceDatabase.kt   ← Room Database
│   │   │   ├── ScholarSpaceDao.kt        ← DAO queries (Flow-based)
│   │   │   └── Entities.kt              ← Table entities & UserEntity
│   │   │
│   │   ├── model/
│   │   │   └── Models.kt                ← Data class Scholarship & Course
│   │   │
│   │   ├── network/
│   │   │   ├── GeminiClient.kt          ← Google Gemini AI via Retrofit
│   │   │   └── NewsApiClient.kt         ← NewsAPI via RapidAPI (Retrofit)
│   │   │
│   │   └── repository/
│   │       └── ScholarSpaceRepository.kt ← Repository layer + preloaded data
│   │
│   └── ui/
│       ├── theme/
│       │   ├── Color.kt     ← Skema warna indigo (LightPrimary #4F46E5)
│       │   ├── Theme.kt     ← MaterialTheme setup
│       │   └── Type.kt      ← Tipografi Material3
│       │
│       └── viewmodel/
│           └── ScholarSpaceViewModel.kt  ← AndroidViewModel, StateFlow, Executor, Auth
│
├── .env                  ← API keys (tidak di-commit)
├── .env.example          ← Template konfigurasi
└── README.md
```

### Alur Data

```
UI (Compose) ←→ ViewModel (StateFlow) ←→ Repository ←→ Room DB
                      ↕
              Retrofit (Gemini / NewsAPI)
```

---

## Penjelasan Teknis

### 1. Dua Activity + Intent

```kotlin
// SplashActivity.kt — Activity 1 (Launcher)
class SplashActivity : ComponentActivity() {
    private val handler = Handler(Looper.getMainLooper())
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handler.postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 2800L)
    }
}
```

### 2. Background Thread — Executor & Handler

```kotlin
// ViewModel.kt — Executor untuk inisialisasi data di background thread
private val backgroundExecutor = Executors.newSingleThreadExecutor()

private fun runBackgroundDataInit() {
    backgroundExecutor.execute {
        Log.d("ScholarSpace", "Executor: ${repository.preloadedScholarships.size} beasiswa siap")
    }
}

// SplashActivity.kt — Handler untuk delay navigasi
handler.postDelayed({ startActivity(...) }, 2800L)
```

### 3. Room Database (SQLite)

```kotlin
// Entities.kt — saved_scholarships & saved_courses tables
@Entity(tableName = "saved_scholarships")
data class SavedScholarshipEntity(@PrimaryKey val id: String, ...)

// DAO dengan Flow untuk reactive UI updates
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
        @Query("language") language: String = "id"
    ): NewsResponse
}
```

### 5. SharedPreferences

```kotlin
// ScholarSpaceViewModel.kt
private val prefs = application.getSharedPreferences("scholarspace_prefs", Context.MODE_PRIVATE)

// Dark mode toggle — tersimpan antar sesi
val isDarkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
fun toggleDarkMode() {
    isDarkMode.value = !isDarkMode.value
    prefs.edit().putBoolean("dark_mode", isDarkMode.value).apply()
}

// Session login tersimpan
prefs.edit().putString("logged_in_user_id", user.id).apply()
```

### 6. Offline Data dari Room

```kotlin
// Repository.kt — Gabungkan preloaded data + favorit dari DB
val allScholarshipsFlow: Flow<List<Scholarship>> = savedScholarshipsFlow.map { savedList ->
    val savedMap = savedList.associateBy { it.id }
    preloadedScholarships.map { pre -> pre.copy(isFavorite = savedMap[pre.id] != null) } +
    savedList.filter { it.isCustom }.map { it.toScholarship() }
}
```

### 7. Foto Profil — Internal Storage + Coil

```kotlin
// Foto disimpan ke internal storage agar tidak hilang setelah reinstall
fun updateProfilePhoto(uri: Uri) {
    val internalFile = File(context.filesDir, "profile_photo_$userId.jpg")
    context.contentResolver.openInputStream(uri)?.use { input ->
        internalFile.outputStream().use { output -> input.copyTo(output) }
    }
    // Timestamp suffix untuk cache-busting Coil
    profilePhotoUri.value = internalFile.absolutePath + "?v=" + System.currentTimeMillis()
}
```

---

## Teknologi yang Digunakan

| Library / Framework | Versi | Kegunaan |
|---|---|---|
| Kotlin | 2.0 | Bahasa utama |
| Jetpack Compose | BOM 2024.x | UI declarative |
| Material3 | — | Design system |
| Room | 2.x | Local database (SQLite) |
| Retrofit2 | 2.x | HTTP networking |
| OkHttp | 4.x | HTTP client + logging |
| Moshi | 1.x | JSON parsing |
| Coil | 2.7.0 | Image loading async |
| Google Gemini API | 1.x | AI assistant |
| Kotlin Coroutines | 1.x | Async + background threads |
| ViewModel + StateFlow | — | MVVM state management |
| KSP | — | Code generation (Room, Moshi) |

---

## Cara Setup & Jalankan

### Prasyarat

- Android Studio Hedgehog 2023.1.1+
- JDK 11+
- Android SDK (min 24, target 35)
- Perangkat Android atau Emulator API 24+

### Langkah

```bash
# 1. Clone repository
git clone https://github.com/Ryannfirmansyah/ScholarSpace.git
cd ScholarSpace

# 2. Setup API key (opsional — untuk fitur AI & Berita)
cp .env.example .env
# Edit .env: GEMINI_API_KEY=YOUR_KEY_FROM_AISTUDIO

# 3. Build APK debug
./gradlew assembleDebug

# 4. Install ke device
adb install -r app/build_new/outputs/apk/debug/app-debug.apk
```

### Buka di Android Studio

1. **File → Open** → pilih folder `ScholarSpace`
2. Tunggu Gradle sync selesai (pertama kali ~2–5 menit)
3. Pilih device / emulator, klik **Run ▶**

---

## Konfigurasi API

| API | Sumber | Catatan |
|---|---|---|
| Google Gemini AI | [Google AI Studio](https://aistudio.google.com/) | Daftarkan `GEMINI_API_KEY` di file `.env` |
| NewsAPI | [RapidAPI](https://rapidapi.com/) | Sudah terkonfigurasi di source code (free tier) |

> File `.env` sudah di-`.gitignore`. Gunakan `.env.example` sebagai template.
> Tanpa API key, AI Asisten tetap berfungsi via fallback keyword offline.

---

## Desain UI

- **Color Scheme**: Indigo — Light Primary `#4F46E5`, Dark `#818CF8`
- **Background**: Light `#F8FAFC` (Slate-50), Dark `#0F172A` (Slate-900)
- **Cards**: Corner radius 16dp, elevation 2dp, colored header strip per kategori/platform
- **Gradient**: Indigo `primary → secondary` di hero, profil, dan detail screen
- **Animasi**: Spring bounce (logo splash), fade transition, pulse loading indicator
- **Search**: Terintegrasi dalam gradient hero dengan teks putih
- **Edge-to-edge**: Status bar & navigation bar terintegrasi dengan UI

---

## Developer

| | |
|---|---|
| **Nama** | Ryan Firmansyah |
| **NIM** | H071241082 |
| **Program Studi** | Sistem Informasi |
| **Universitas** | Universitas Hasanuddin (Unhas) |
| **Semester** | 4 (2026) |
| **Mata Kuliah** | Praktikum Pemrograman Mobile |

---

<p align="center">
  Dibuat dengan Kotlin + Jetpack Compose + Material3
</p>
