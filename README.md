# 🎓 ScholarSpace — Aplikasi Pencari Beasiswa & Kursus Online

> Tugas Final Lab Mobile 2026 | H071241082 | Ryan Firmansyah | Tema: Pendidikan

## Deskripsi

ScholarSpace adalah aplikasi Android berbasis Kotlin + Jetpack Compose yang membantu mahasiswa menemukan beasiswa dan kursus online terbaik. Aplikasi ini mengambil berita pendidikan secara real-time via API Retrofit, menyimpan data favorit secara lokal menggunakan Room Database, dan dilengkapi asisten AI berbasis Gemini untuk menjawab pertanyaan seputar beasiswa dan kursus.

## Fitur Utama

- 🎓 **Eksplor Beasiswa** — Daftar beasiswa lengkap (LPDP, Chevening, Erasmus, MEXT, dll) dengan filter kategori
- 📚 **Eksplor Kursus** — Kursus online terakreditasi (Dicoding, Udemy, Coursera) dengan filter kategori
- 🔖 **Simpan Favorit** — Bookmark beasiswa & kursus ke Room Database untuk akses offline
- 🤖 **AI Asisten** — Chat dengan ScholarSpace AI (Gemini) untuk tanya jawab seputar beasiswa
- 📰 **Berita Pendidikan** — Ambil berita pendidikan terkini via Retrofit + RapidAPI, dengan tombol retry saat offline
- 🌙 **Dark / Light Mode** — Toggle tema gelap dan terang
- ➕ **Tambah Manual** — Form input untuk menambah beasiswa/kursus kustom

## Spesifikasi Teknis

| Komponen | Implementasi |
|---|---|
| Activity | SplashActivity (Launcher) + MainActivity |
| Intent | Navigasi antar screen + buka URL berita di browser |
| RecyclerView | LazyColumn Jetpack Compose |
| Fragment & Navigation | 5 screen: Eksplor, Simpan, AI Asisten, Berita, Profil |
| Background Thread | Kotlin Coroutines (`viewModelScope.launch`) |
| Networking | Retrofit + OkHttp — NewsApiClient (RapidAPI) + GeminiClient |
| Tombol Refresh | Tombol "Coba Lagi" di NewsScreen saat gagal ambil data |
| Local Storage | Room Database (SQLite) untuk simpan beasiswa & kursus favorit |
| Offline Fallback | Data tersimpan tetap tampil saat tidak ada jaringan |
| Dark/Light Theme | Material3 DayNight via `AppCompatDelegate` |

## API yang Digunakan

- **News API** (RapidAPI) — Berita pendidikan terkini
- **Gemini AI API** (Google AI Studio) — Asisten AI ScholarSpace

## Cara Menjalankan

1. Clone repo ini
2. Buka dengan Android Studio (versi terbaru)
3. Sync Gradle
4. (Opsional) Masukkan Gemini API Key di file `.env`:
   ```
   GEMINI_API_KEY=your_api_key_here
   ```
5. Run di emulator atau device fisik (min SDK 24)

## Struktur Project

```
ScholarSpace/
├── data/
│   ├── database/     # ScholarSpaceDatabase, ScholarSpaceDao, Entities (Room)
│   ├── model/        # Course, Scholarship
│   ├── network/      # GeminiClient, NewsApiClient (Retrofit)
│   └── repository/   # ScholarSpaceRepository
├── ui/
│   ├── theme/        # ScholarSpaceTheme, Color, Typography
│   └── viewmodel/    # ScholarSpaceViewModel
├── MainActivity.kt   # Entry point + semua Composable screen
└── NewsScreen.kt     # Screen berita pendidikan (Retrofit API)
```

## Screenshot

> (Tambahkan screenshot setelah APK selesai di-build)

---

**Mahasiswa:** Ryan Firmansyah | **NIM:** H071241082 | **Program Studi:** Sistem Informasi | **Universitas Hasanuddin 2026**
