package com.example.data.repository

import com.example.data.database.EduSearchDao
import com.example.data.database.SavedCourseEntity
import com.example.data.database.SavedScholarshipEntity
import com.example.data.model.Course
import com.example.data.model.Scholarship
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class EduSearchRepository(private val dao: EduSearchDao) {

    // --- Preloaded Data Lists ---
    val preloadedScholarships = listOf(
        Scholarship(
            id = "pre_scholarship_1",
            title = "Beasiswa LPDP 2026",
            provider = "Kementerian Keuangan RI",
            benefits = "Biaya kuliah penuh (tuition fee), tunjangan hidup bulanan, tunjangan buku, asuransi kesehatan, dana penelitian, visa, dan tiket PP.",
            description = "Beasiswa bergengsi dari Pemerintah Indonesia untuk menempuh studi jenjang Magister (S2) dan Doktor (S3) di universitas terbaik dalam maupun luar negeri.",
            deadline = "02 Juli 2026",
            status = "Buka",
            link = "https://lpdp.kemenkeu.go.id/",
            category = "Pemerintah",
            requirements = "Warga Negara Indonesia (WNI), lulusan S1/S2, batas usia maksimal (35 tahun untuk S2, 40 tahun untuk S3), sertifikat bahasa Inggris resmi (TOEFL iBT/IELTS), surat rekomendasi akademis/profesional, dan melampirkan proposal studi."
        ),
        Scholarship(
            id = "pre_scholarship_2",
            title = "Chevening Awards UK",
            provider = "Pemerintah Inggris (UK FCDO)",
            benefits = "Uang sekolah penuh, tunjangan bulanan tetap, biaya perjalanan pulang-pergi kelas ekonomi ke UK, biaya kedatangan, dan visa kuliah.",
            description = "Proram beasiswa global Pemerintah Inggris yang mendanai pendidikan gelar Master satu tahun di universitas terkemuka di Inggris Raya bagi para pemimpin masa depan.",
            deadline = "05 November 2026",
            status = "Tutup",
            link = "https://www.chevening.org/",
            category = "Luar Negeri",
            requirements = "Kembali ke negara asal minimal 2 tahun setelah lulus, memiliki gelar sarjana (S1), minimal memiliki pengalaman kerja selama 2 tahun (2.800 jam), serta mendaftar ke 3 universitas UK yang berbeda."
        ),
        Scholarship(
            id = "pre_scholarship_3",
            title = "Erasmus Mundus Scholarships (EMJM)",
            provider = "Konsorsium Uni Eropa",
            benefits = "Pembebasan biaya kuliah, asuransi kesehatan penuh, kontribusi perjalanan, tiket udara PP, serta tunjangan bulanan €1.000 selama maksimal 24 bulan.",
            description = "Program beasiswa prestisius dari Uni Eropa yang mendanai mahasiswa internasional untuk belajar di minimal dua universitas Eropa yang tergabung dalam konsorsium sains pilihan.",
            deadline = "15 Januari 2027",
            status = "Tutup",
            link = "https://ec.europa.eu/programmes/erasmus-plus/",
            category = "Luar Negeri",
            requirements = "Terbuka bagi lulusan S1 dari seluruh dunia dengan nilai akademik unggul. Memiliki sertifikat kemahiran bahasa Inggris tingkat tinggi (IELTS minimal 6.5/7.0). CV, motivation letter, dan dua surat referensi."
        ),
        Scholarship(
            id = "pre_scholarship_4",
            title = "Beasiswa Monbukagakusho (MEXT)",
            provider = "Pemerintah Jepang (MEXT)",
            benefits = "Uang sekolah dibayar penuh, tunjangan bulanan sekitar 143.000 JPY (untuk Research Students), tiket pesawat kelas ekonomi PP Indonesia-Jepang, tanpa ikatan dinas.",
            description = "Beasiswa penuh dari Kementerian Pendidikan, Kebudayaan, Olahraga, Sains, dan Teknologi Jepang untuk belajar di universitas-universitas Jepang sebagai mahasiswa riset (S2/S3).",
            deadline = "15 Mei 2026",
            status = "Tutup",
            link = "https://www.id.emb-japan.go.jp/sch.html",
            category = "Luar Negeri",
            requirements = "Usia maksimal 34 tahun, indeks prestasi kumulatif (IPK) minimal 3.20 dari skala 4.00, bersedia belajar bahasa Jepang dasar sebelum studi utama dimulai."
        ),
        Scholarship(
            id = "pre_scholarship_5",
            title = "Beasiswa Unggulan Kemendikbud",
            provider = "Kemendikbudristek RI",
            benefits = "Pembayaran SPP penuh per semester, uang saku bulanan untuk menunjang kehidupan kuliah, serta bantuan biaya buku tahunan.",
            description = "Beasiswa yang ditujukan untuk siswa berprestasi nasional, guru, pegiat budaya, serta pegawai sipil di lingkungan Kemendikbudristek untuk jenjang S1, S2, dan S3.",
            deadline = "31 Agustus 2026",
            status = "Buka",
            link = "https://beasiswaunggulan.kemdikbud.go.id/",
            category = "Dalam Negeri",
            requirements = "Diterima di perguruan tinggi berakreditasi minimal B, berprestasi di tingkat nasional/internasional, lulus tes tertulis dan wawancara, memiliki TOEFL ITP 500 / IELTS 5.5."
        ),
        Scholarship(
            id = "pre_scholarship_6",
            title = "Djarum Beasiswa Plus",
            provider = "Djarum Foundation",
            benefits = "Bantuan biaya belajar bulanan sebesar Rp 1.000.000,- selama 1 tahun, serta exclusive soft skills training (Character Building, Leadership, Nation Building).",
            description = "Program beasiswaSwasta bagi mahasiswa berprestasi jenjang S1 yang aktif berorganisasi untuk mengembangkan kepemimpinan dan kemampuan adaptif.",
            deadline = "20 Mei 2026",
            status = "Buka",
            link = "https://djarumbeasiswaplus.org/",
            category = "Swasta",
            requirements = "Mahasiswa S1 semester IV, IPK minimal 3.00 pada semester III, aktif berorganisasi di dalam maupun luar kampus, tidak sedang menerima beasiswa dari pihak lain."
        )
    )

    val preloadedCourses = listOf(
        Course(
            id = "pre_course_1",
            title = "Belajar Dasar Pemrograman Kotlin",
            instructor = "Dicoding Academy Team",
            platform = "Dicoding",
            price = "Gratis",
            rating = 4.8,
            category = "Teknologi",
            link = "https://www.dicoding.com/courses/belajar-dasar-pemrograman-kotlin",
            description = "Belajar bahasa Kotlin dari dasar hingga konsep Object-Oriented Programming (OOP), Functional Programming, Generics, Konkurensi menggunakan Coroutines, serta kesiapan pembuatan aplikasi Android modern."
        ),
        Course(
            id = "pre_course_2",
            title = "Android Jetpack Compose Masterclass",
            instructor = "EduSearch Developer Team",
            platform = "Udemy",
            price = "Rp 129.000",
            rating = 4.9,
            category = "Teknologi",
            link = "https://www.udemy.com/",
            description = "Mulai bangun UI deklaratif modern di perangkat mobile Android menggunakan Jetpack Compose. Kursus ini memandu pembuatan State Management, Custom Animations, Navigasi Tipe Aman, integrasi Room DB, dan Material Design 3."
        ),
        Course(
            id = "pre_course_3",
            title = "Python for Data Science & AI Essentials",
            instructor = "IBM Skills Network",
            platform = "Coursera",
            price = "Gratis (Audit)",
            rating = 4.7,
            category = "Teknologi",
            link = "https://www.coursera.org/learn/python-for-applied-data-science-ai",
            description = "Kursus ramah pemula yang mencakup konsep dasar Python. Mempelajari struktur data, manipulasi data menggunakan Pandas & NumPy, visualisasi data, serta pengantar teknik kecerdasan buatan."
        ),
        Course(
            id = "pre_course_4",
            title = "Desain UI/UX Lengkap dengan Figma",
            instructor = "Andrea Wijaya (Product Designer)",
            platform = "Udemy",
            price = "Rp 99.000",
            rating = 4.6,
            category = "Desain",
            link = "https://www.udemy.com/",
            description = "Pelajari proses desain produk digital lengkap dari nol: pembuatan wireframe, moodboard, user flow, tipografi digital pilihan, pembuatan prototype interaktif, hingga teknik handoff ke tim enginner."
        ),
        Course(
            id = "pre_course_5",
            title = "English for Academic Purposes (IELTS Prep)",
            instructor = "Macquarie University",
            platform = "Coursera",
            price = "Berbayar / Bantuan Cuma-cuma",
            rating = 4.8,
            category = "Bahasa",
            link = "https://www.coursera.org/specializations/ielts-preparation",
            description = "Mempersiapkan ujian IELTS Anda dengan strategi membaca cepat, menulis esai terstruktur, menyimak percakapan akademik berkualitas tinggi, serta melatih pelafalan lancar untuk meraih skor 7.0+."
        ),
        Course(
            id = "pre_course_6",
            title = "Manajemen Bisnis Digital & Ideasi Startup",
            instructor = "Google Careers",
            platform = "Coursera",
            price = "Gratis",
            rating = 4.9,
            category = "Bisnis",
            link = "https://www.coursera.org/",
            description = "Mengembangkan kualifikasi Anda di dunia bisnis digital. Kursus ini membahas pembuatan Canvas Business Model (BMC), strategi pemasaran digital bertarget tinggi, serta manajemen tim minimalis."
        )
    )

    // --- Database Flows ---
    val savedScholarshipsFlow: Flow<List<SavedScholarshipEntity>> = dao.getSavedScholarshipsFlow()
    val savedCoursesFlow: Flow<List<SavedCourseEntity>> = dao.getSavedCoursesFlow()

    // --- Combined Flow of All Available Scholarships ---
    // Includes preloaded + custom scholarships added by user. Adds 'isFavorite' based on saved DB mapping.
    val allScholarshipsFlow: Flow<List<Scholarship>> = savedScholarshipsFlow.map { savedList ->
        val savedMap = savedList.associateBy { it.id }
        
        // Custom entries created by user are identified from DB has isCustom = true
        val customScholarships = savedList.filter { it.isCustom }.map { entity ->
            Scholarship(
                id = entity.id,
                title = entity.title,
                provider = entity.provider,
                benefits = entity.benefits,
                description = entity.description,
                deadline = entity.deadline,
                status = entity.status,
                link = entity.link,
                category = entity.category,
                requirements = entity.requirements,
                isCustom = true,
                isFavorite = true
            )
        }

        val enrichedPreloaded = preloadedScholarships.map { pre ->
            val savedEntity = savedMap[pre.id]
            pre.copy(
                isFavorite = savedEntity != null
            )
        }

        enrichedPreloaded + customScholarships
    }

    // --- Combined Flow of All Available Courses ---
    // Includes preloaded + custom courses added by user. Adds 'isFavorite' based on saved DB mapping.
    val allCoursesFlow: Flow<List<Course>> = savedCoursesFlow.map { savedList ->
        val savedMap = savedList.associateBy { it.id }
        
        val customCourses = savedList.filter { it.isCustom }.map { entity ->
            Course(
                id = entity.id,
                title = entity.title,
                instructor = entity.instructor,
                platform = entity.platform,
                price = entity.price,
                rating = entity.rating,
                description = entity.description,
                link = entity.link,
                category = entity.category,
                isCustom = true,
                isFavorite = true
            )
        }

        val enrichedPreloaded = preloadedCourses.map { pre ->
            val savedEntity = savedMap[pre.id]
            pre.copy(
                isFavorite = savedEntity != null
            )
        }

        enrichedPreloaded + customCourses
    }

    // --- Scholarship DB Operations ---
    suspend fun saveScholarship(scholarship: Scholarship) {
        dao.insertScholarship(scholarship.toEntity())
    }

    suspend fun removeScholarship(id: String) {
        dao.deleteScholarshipById(id)
    }

    // --- Course DB Operations ---
    suspend fun saveCourse(course: Course) {
        dao.insertCourse(course.toEntity())
    }

    suspend fun removeCourse(id: String) {
        dao.deleteCourseById(id)
    }
}
