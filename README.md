# 🎓 EduSearch — Aplikasi Pencari Kursus Online

> Tugas Final Lab Mobile 2026 | H071241082 | Tema: Pendidikan

## Deskripsi

EduSearch adalah aplikasi Android yang membantu pengguna menemukan dan menyimpan kursus online dari Udemy. Aplikasi ini mengambil data kursus secara real-time melalui API dan menyimpan kursus favorit secara lokal menggunakan SQLite, sehingga tetap bisa diakses saat offline.

## Fitur Utama

- 🔍 **Browse Kursus** — Menampilkan daftar kursus dari Udemy API (programming)
- 🔖 **Simpan Kursus** — Simpan kursus favorit ke SQLite untuk akses offline
- 🌐 **Buka di Udemy** — Langsung buka halaman kursus di browser
- 🌙 **Dark / Light Mode** — Tema gelap dan terang menggunakan DayNight
- 🔄 **Tombol Retry** — Refresh data saat tidak ada koneksi internet
- 📴 **Mode Offline** — Tampilkan data tersimpan ketika tidak ada jaringan

## Spesifikasi Teknis

| Komponen | Implementasi |
|---|---|
| Activity | SplashActivity (Launcher), MainActivity, DetailActivity |
| Intent | MainActivity → DetailActivity dengan data kursus |
| RecyclerView | Daftar kursus di HomeFragment & SavedFragment |
| Fragment | HomeFragment, SavedFragment, SettingsFragment |
| Navigation | Navigation Component + BottomNavigationView |
| Background Thread | Executor + Handler (DB operations & API response) |
| Networking | Retrofit + OkHttp (Udemy API via RapidAPI) |
| Local Storage | SQLite (CourseDbHelper) untuk kursus tersimpan |
| SharedPreferences | Menyimpan preferensi dark mode |
| Dark/Light Theme | DayNight theme (MaterialComponents.DayNight) |

## API

Menggunakan **Udemy Paid Courses For Free API** dari RapidAPI:
- Host: `udemy-paid-courses-for-free-api.p.rapidapi.com`
- Endpoint: `GET /rapidapi/courses/`

## Cara Menjalankan

1. Clone repo ini
2. Buka dengan Android Studio
3. Sync Gradle
4. Run di emulator/device (min SDK 24)

## Screenshot

> (Tambahkan screenshot setelah APK selesai di-build)

## Struktur Project

```
EduSearch/
├── activity/       # SplashActivity, MainActivity, DetailActivity
├── fragment/       # HomeFragment, SavedFragment, SettingsFragment
├── adapter/        # CourseAdapter (RecyclerView)
├── model/          # Course, CourseResponse, Instructor
├── network/        # ApiClient, UdemyApiService (Retrofit)
└── db/             # CourseDbHelper, SavedCourse (SQLite)
```

---

**Mahasiswa:** Ryan Firmansyah | **NIM:** H071241082 | **Unhas 2026**
