package com.ryan.scholarspace.data.network

import com.ryan.scholarspace.BuildConfig
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class Part(@Json(name = "text") val text: String? = null)

@JsonClass(generateAdapter = true)
data class Content(@Json(name = "parts") val parts: List<Part>)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(@Json(name = "text") val text: String? = null)

@JsonClass(generateAdapter = true)
data class ContentResponse(@Json(name = "parts") val parts: List<PartResponse>? = null)

@JsonClass(generateAdapter = true)
data class Candidate(@Json(name = "content") val content: ContentResponse? = null)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    @Json(name = "candidates") val candidates: List<Candidate>? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-1.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }

    suspend fun askGemini(
        prompt: String,
        conversationHistory: List<Content> = emptyList(),
        appContext: String = ""
    ): String {
        val apiKey = try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getOfflineResponse(prompt)
        }

        val currentContent = Content(parts = listOf(Part(text = prompt)))
        val fullContents = conversationHistory.takeLast(10) + currentContent

        val appData = if (appContext.isNotEmpty()) "\n\n$appContext" else ""
        val systemInstruction = """
            Anda adalah ScholarSpace AI, asisten cerdas yang dapat menjawab pertanyaan apa pun secara ramah, informatif, dan akurat dalam bahasa Indonesia.

            Keahlian utama Anda:
            - Beasiswa (LPDP, Chevening, Erasmus, MEXT, Beasiswa Unggulan, Djarum Plus, dll.)
            - Kursus online (Dicoding, Udemy, Coursera, dll.)
            - Tips strategi mendaftar beasiswa dan menulis motivation letter
            - Rekomendasi belajar dan pengembangan diri
            - Informasi universitas dan pendidikan tinggi di Indonesia dan dunia
            - Pertanyaan umum di luar topik pendidikan juga Anda jawab dengan baik

            Gunakan format yang mudah dibaca: paragraf singkat, poin-poin, atau tabel jika relevan.$appData
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = fullContents,
            systemInstruction = Content(parts = listOf(Part(text = systemInstruction)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Maaf, tidak dapat menerima jawaban dari AI saat ini."
        } catch (e: Exception) {
            e.printStackTrace()
            getOfflineResponse(prompt)
        }
    }

    private fun getOfflineResponse(prompt: String): String {
        val q = prompt.lowercase().trim()

        // Greetings
        if (q.matches(Regex(".*(\\bhalo\\b|\\bhai\\b|\\bhi\\b|\\bhello\\b|selamat (pagi|siang|sore|malam)|\\bhei\\b|\\bhey\\b).*"))) {
            return """Halo! Selamat datang di ScholarSpace AI! 👋

Saya siap membantu Anda menemukan informasi tentang:
• 🎓 Beasiswa (LPDP, Chevening, Erasmus, MEXT, dan lainnya)
• 📚 Kursus online (Dicoding, Udemy, Coursera)
• 💡 Tips & strategi mendaftar beasiswa
• ✍️ Cara menulis motivation letter

Silakan tanyakan apa yang ingin Anda ketahui!"""
        }

        // Thanks
        if (q.matches(Regex(".*(terima kasih|makasih|\\bthanks\\b|thank you|trims).*"))) {
            return "Sama-sama! 😊 Senang bisa membantu. Jika ada pertanyaan lain tentang beasiswa atau kursus, jangan ragu bertanya ya!"
        }

        // LPDP
        if (q.contains("lpdp")) {
            return """🏆 **Beasiswa LPDP 2026**
Provider: Kementerian Keuangan RI
Status: ✅ Buka | Deadline: 02 Juli 2026

💰 **Manfaat:**
Biaya kuliah penuh, tunjangan hidup bulanan, tunjangan buku, asuransi kesehatan, dana penelitian, visa, dan tiket PP.

✅ **Syarat Utama:**
• WNI, lulusan S1/S2
• Usia maks. 35 tahun (S2) / 40 tahun (S3)
• TOEFL iBT min. 80 / IELTS min. 6.5 (untuk LN)
• Surat rekomendasi akademis/profesional
• Proposal studi

🔗 lpdp.kemenkeu.go.id

💡 **Tips:** Persiapkan esai kontribusi yang kuat dan mulai siapkan sertifikat bahasa Inggris dari sekarang!"""
        }

        // Chevening
        if (q.contains("chevening") || (q.contains("inggris") && q.contains("beasiswa") && !q.contains("bahasa"))) {
            return """🇬🇧 **Chevening Awards UK**
Provider: Pemerintah Inggris (UK FCDO)
Status: ❌ Tutup | Deadline: 05 November 2026

💰 **Manfaat:**
Uang sekolah penuh, tunjangan bulanan tetap, tiket PP kelas ekonomi ke UK, biaya kedatangan, dan visa kuliah.

✅ **Syarat Utama:**
• Kembali ke negara asal min. 2 tahun setelah lulus
• Gelar sarjana (S1)
• Pengalaman kerja min. 2 tahun (2.800 jam)
• Mendaftar ke 3 universitas UK berbeda

🔗 chevening.org

💡 **Tips:** Chevening sangat mementingkan kepemimpinan & kontribusi sosial. Tonjolkan pengalaman organisasi dan dampak nyata Anda!"""
        }

        // Erasmus
        if (q.contains("erasmus") || (q.contains("eropa") && q.contains("beasiswa"))) {
            return """🇪🇺 **Erasmus Mundus Scholarships (EMJM)**
Provider: Konsorsium Uni Eropa
Status: ❌ Tutup | Deadline: 15 Januari 2027

💰 **Manfaat:**
Pembebasan biaya kuliah, asuransi kesehatan, tiket PP, tunjangan bulanan €1.000 selama maks. 24 bulan.

✅ **Syarat Utama:**
• Terbuka bagi lulusan S1 dari seluruh dunia
• Nilai akademik unggul (IPK tinggi)
• IELTS min. 6.5 / 7.0
• CV, motivation letter, 2 surat referensi

🔗 ec.europa.eu/programmes/erasmus-plus

💡 **Tips:** Erasmus mencari mahasiswa berprestasi yang bisa beradaptasi di lingkungan multikultural Eropa. Motivation letter yang personal sangat menentukan!"""
        }

        // MEXT / Japan
        if (q.contains("mext") || q.contains("monbukagakusho") || (q.contains("jepang") && q.contains("beasiswa"))) {
            return """🇯🇵 **Beasiswa Monbukagakusho (MEXT)**
Provider: Pemerintah Jepang (MEXT)
Status: ❌ Tutup | Deadline: 15 Mei 2026

💰 **Manfaat:**
Uang sekolah penuh, tunjangan bulanan ~143.000 JPY, tiket pesawat PP, tanpa ikatan dinas.

✅ **Syarat Utama:**
• Usia maks. 34 tahun
• IPK min. 3.20 dari skala 4.00
• Bersedia belajar bahasa Jepang dasar sebelum studi

🔗 id.emb-japan.go.jp/sch.html

💡 **Tips:** Pendaftaran melalui Kedubes Jepang di Jakarta. Belajar bahasa Jepang dasar (N5/N4) memberikan nilai plus besar!"""
        }

        // Beasiswa Unggulan
        if (q.contains("unggulan") || (q.contains("kemendikbud") && q.contains("beasiswa"))) {
            return """🏅 **Beasiswa Unggulan Kemendikbud**
Provider: Kemendikbudristek RI
Status: ✅ Buka | Deadline: 31 Agustus 2026

💰 **Manfaat:**
SPP penuh per semester, uang saku bulanan, dan bantuan biaya buku tahunan.

✅ **Syarat Utama:**
• Diterima di PT berakreditasi min. B
• Berprestasi di tingkat nasional/internasional
• Lulus tes tertulis dan wawancara
• TOEFL ITP 500 / IELTS 5.5

🔗 beasiswaunggulan.kemdikbud.go.id

💡 **Tips:** Kumpulkan portofolio prestasi akademik dan non-akademik sebanyak mungkin sebelum mendaftar!"""
        }

        // Djarum
        if (q.contains("djarum")) {
            return """🎯 **Djarum Beasiswa Plus**
Provider: Djarum Foundation
Status: ✅ Buka | Deadline: 20 Mei 2026

💰 **Manfaat:**
Bantuan biaya belajar Rp 1.000.000/bulan selama 1 tahun + soft skills training eksklusif (Character Building, Leadership, Nation Building).

✅ **Syarat Utama:**
• Mahasiswa S1 semester IV
• IPK min. 3.00 pada semester III
• Aktif berorganisasi dalam & luar kampus
• Tidak sedang menerima beasiswa lain

🔗 djarumbeasiswaplus.org

💡 **Tips:** Djarum sangat menghargai kontribusi organisasi. Dokumentasikan semua kegiatan kepemimpinan yang pernah Anda ikuti!"""
        }

        // List semua beasiswa
        if (q.contains("beasiswa") && (q.contains("apa saja") || q.contains("semua") || q.contains("daftar") || q.contains("list") || q == "beasiswa")) {
            return """📋 **Daftar Beasiswa di ScholarSpace:**

1. 🏆 **Beasiswa LPDP 2026** (Pemerintah) — 02 Jul 2026 ✅ Buka
2. 🇬🇧 **Chevening Awards UK** (Luar Negeri) — 05 Nov 2026 ❌ Tutup
3. 🇪🇺 **Erasmus Mundus EMJM** (Luar Negeri) — 15 Jan 2027 ❌ Tutup
4. 🇯🇵 **Monbukagakusho MEXT** (Luar Negeri) — 15 Mei 2026 ❌ Tutup
5. 🏅 **Beasiswa Unggulan Kemendikbud** (Dalam Negeri) — 31 Agt 2026 ✅ Buka
6. 🎯 **Djarum Beasiswa Plus** (Swasta) — 20 Mei 2026 ✅ Buka

Tanyakan nama beasiswa tertentu untuk info lengkap syarat, manfaat, dan tipsnya!"""
        }

        // List semua kursus
        if (q.contains("kursus") && (q.contains("apa saja") || q.contains("semua") || q.contains("daftar") || q.contains("list"))) {
            return """📚 **Daftar Kursus di ScholarSpace:**

1. 📱 **Belajar Dasar Pemrograman Kotlin** — Dicoding | Gratis | ⭐ 4.8
2. 🎨 **Android Jetpack Compose Masterclass** — Udemy | Rp 129.000 | ⭐ 4.9
3. 🐍 **Python for Data Science & AI** — Coursera | Gratis (Audit) | ⭐ 4.7
4. 🖌️ **Desain UI/UX Lengkap dengan Figma** — Udemy | Rp 99.000 | ⭐ 4.6
5. 🗣️ **English for Academic Purposes (IELTS Prep)** — Coursera | ⭐ 4.8
6. 💼 **Manajemen Bisnis Digital & Startup** — Coursera | Gratis | ⭐ 4.9

Tanya kursus tertentu untuk detail lengkap dan link pendaftarannya!"""
        }

        // Kotlin / Dicoding / Android dev
        if (q.contains("kotlin") || q.contains("dicoding") || (q.contains("android") && (q.contains("kursus") || q.contains("belajar")))) {
            return """📱 **Belajar Dasar Pemrograman Kotlin**
Platform: Dicoding Academy | Gratis | ⭐ 4.8
Instruktur: Dicoding Academy Team

📖 **Materi:**
Kotlin dari dasar hingga OOP, Functional Programming, Generics, Coroutines, dan kesiapan membuat aplikasi Android modern.

✅ Cocok untuk: Pemula yang ingin masuk dunia Android Development
🔗 dicoding.com/courses/belajar-dasar-pemrograman-kotlin

💡 Setelah selesai, lanjutkan dengan Android Jetpack Compose Masterclass di Udemy!"""
        }

        // Jetpack Compose / Android Studio
        if (q.contains("jetpack") || q.contains("compose") || q.contains("android studio") || q.contains("masterclass")) {
            return """🎨 **Android Jetpack Compose Masterclass**
Platform: Udemy | Rp 129.000 | ⭐ 4.9
Instruktur: ScholarSpace Developer Team

📖 **Materi:**
UI deklaratif modern: State Management, Custom Animations, Navigasi, Room DB, Material Design 3.

✅ Cocok untuk: Developer yang sudah paham dasar Kotlin
🔗 udemy.com

💡 Udemy sering diskon besar. Pantau harganya sebelum membeli!"""
        }

        // Python / Data Science / ML / AI
        if (q.contains("python") || q.contains("data science") || q.contains("machine learning") || q.contains("data sains") || q.contains("ibm")) {
            return """🐍 **Python for Data Science & AI Essentials**
Platform: Coursera | Gratis (Audit) | ⭐ 4.7
Instruktur: IBM Skills Network

📖 **Materi:**
Dasar Python, Pandas & NumPy, visualisasi data, dan pengantar kecerdasan buatan.

✅ Cocok untuk: Mahasiswa yang ingin karir di bidang Data/AI
🔗 coursera.org/learn/python-for-applied-data-science-ai

💡 Pilih mode "Audit" untuk akses gratis. Bayar hanya jika ingin sertifikat resmi!"""
        }

        // UI/UX / Figma / Design
        if (q.contains("figma") || q.contains("ui/ux") || q.contains("ui ux") || q.contains("desain") || q.contains("design") || (q.contains("ui") && q.contains("kursus"))) {
            return """🖌️ **Desain UI/UX Lengkap dengan Figma**
Platform: Udemy | Rp 99.000 | ⭐ 4.6
Instruktur: Andrea Wijaya (Product Designer)

📖 **Materi:**
Wireframe, moodboard, user flow, tipografi, prototype interaktif, hingga handoff ke engineer.

✅ Cocok untuk: Mahasiswa yang ingin jadi UI/UX Designer atau Product Designer
🔗 udemy.com

💡 Bangun portofolio di Dribbble/Behance langsung saat belajar. Ini yang paling dilihat oleh rekruter!"""
        }

        // IELTS / TOEFL / Bahasa Inggris
        if (q.contains("ielts") || q.contains("toefl") || q.contains("bahasa inggris") || q.contains("english") || q.contains("ielts prep")) {
            return """🗣️ **English for Academic Purposes (IELTS Prep)**
Platform: Coursera | Berbayar / Financial Aid | ⭐ 4.8
Instruktur: Macquarie University

📖 **Materi:**
Strategi membaca cepat, menulis esai, menyimak akademik, pelafalan untuk skor IELTS 7.0+.

✅ Cocok untuk: Pendaftar beasiswa luar negeri yang butuh skor IELTS tinggi
🔗 coursera.org/specializations/ielts-preparation

📊 **Target Skor IELTS per Beasiswa:**
• LPDP Luar Negeri: min. 6.5
• Chevening UK: min. 6.5–7.0
• Erasmus Mundus: min. 6.5–7.0
• MEXT Jepang: tidak wajib IELTS

💡 Latihan soal setiap hari lebih efektif daripada belajar banyak sekaligus!"""
        }

        // Bisnis / Startup / Digital Marketing
        if (q.contains("bisnis") || q.contains("startup") || q.contains("digital marketing") || q.contains("business") || q.contains("bmc")) {
            return """💼 **Manajemen Bisnis Digital & Ideasi Startup**
Platform: Coursera | Gratis | ⭐ 4.9
Instruktur: Google Careers

📖 **Materi:**
Canvas Business Model (BMC), strategi pemasaran digital bertarget, dan manajemen tim minimalis.

✅ Cocok untuk: Mahasiswa yang ingin membangun bisnis atau karir digital marketing
🔗 coursera.org

💡 Sertifikat Google Careers sangat diakui di industri dan bisa didapat gratis via financial aid!"""
        }

        // Tips mendaftar beasiswa
        if ((q.contains("tips") || q.contains("cara") || q.contains("strategi") || q.contains("panduan")) && q.contains("beasiswa")) {
            return """💡 **Tips Sukses Mendaftar Beasiswa:**

**1. Persiapan Dokumen (3–6 bulan sebelum deadline):**
• Transkrip nilai dengan IPK terbaik
• Sertifikat bahasa (IELTS/TOEFL) — mulai les dari sekarang!
• Surat rekomendasi dari dosen/atasan
• CV/Resume format profesional (1 halaman)

**2. Motivation Letter yang Kuat:**
• Ceritakan "Mengapa Saya?" secara spesifik
• Hubungkan tujuan studi dengan kontribusi ke Indonesia
• Jangan generik — buat personal dan autentik
• Minta feedback dari 3+ orang sebelum submit

**3. Strategi Mendaftar:**
• Daftar lebih dari 1 beasiswa sekaligus
• Pelajari nilai-nilai pemberi beasiswa
• Bergabung komunitas penerima beasiswa untuk tips insider

**4. Wawancara:**
• Latihan mock interview dengan teman
• Gunakan metode STAR (Situation, Task, Action, Result)
• Tunjukkan kepercayaan diri dan rencana yang jelas

Beasiswa mana yang Anda targetkan? Saya bisa bantu lebih spesifik!"""
        }

        // Motivation letter
        if (q.contains("motivation letter") || q.contains("esai") || q.contains("essay") || (q.contains("surat") && q.contains("motivasi"))) {
            return """✍️ **Cara Menulis Motivation Letter Beasiswa yang Kuat:**

**Struktur yang Direkomendasikan:**

**Paragraf 1 — Opening Hook:**
Mulai dengan kisah singkat yang relevan atau pernyataan kuat. Hindari kalimat klise seperti "Saya adalah mahasiswa dari..."

**Paragraf 2 — Why You?**
Latar belakang akademik, pengalaman, dan keahlian relevan. Sertakan angka & bukti konkret.

**Paragraf 3 — Why This Program/Scholarship?**
Riset mendalam tentang program tersebut. Sebutkan nama profesor, riset, atau keunggulan spesifik.

**Paragraf 4 — Future Goals & Contribution:**
Hubungkan studi dengan kontribusi nyata ke Indonesia. Ini sangat penting untuk LPDP & Chevening!

**Paragraf 5 — Closing:**
Ringkasan percaya diri + ucapan terima kasih.

📝 **Format:** 1–2 halaman, font 11–12pt, spasi 1.5
💡 Sesuaikan isinya untuk SETIAP beasiswa yang didaftar!"""
        }

        // Rekomendasi belajar / mulai dari mana
        if (q.contains("rekomendasi") || q.contains("mulai dari mana") || q.contains("harus mulai") || (q.contains("belajar") && q.contains("apa"))) {
            return """🎯 **Rekomendasi Belajar Berdasarkan Minat:**

💻 **Teknologi/Programming:**
→ Mulai: Kotlin Dasar (Dicoding — Gratis)
→ Lanjut: Android Jetpack Compose (Udemy)
→ Ekspansi: Python for Data Science (Coursera)

🎨 **Desain:**
→ UI/UX Lengkap dengan Figma (Udemy)
→ Bangun portofolio di Dribbble/Behance

📊 **Data & AI:**
→ Python for Data Science — IBM (Coursera — Gratis Audit)

💼 **Bisnis/Entrepreneur:**
→ Manajemen Bisnis Digital (Coursera — Google — Gratis)

🌍 **Beasiswa Luar Negeri:**
→ Persiapkan IELTS/TOEFL terlebih dahulu
→ Target: LPDP (S2/S3), Chevening (UK), Erasmus (Eropa)

Apa minat atau tujuan karir Anda? Saya bisa berikan rekomendasi yang lebih personal!"""
        }

        // General beasiswa
        if (q.contains("beasiswa")) {
            return """🎓 **Informasi Beasiswa di ScholarSpace:**

ScholarSpace memiliki 6 beasiswa pilihan:
✅ **Sedang Buka:** LPDP (02 Jul), Beasiswa Unggulan (31 Agt), Djarum Plus (20 Mei)
📌 **Segera Buka Kembali:** Chevening (Nov 2026), Erasmus (Jan 2027)

💡 **Rekomendasi Cepat:**
• **LPDP** → terbaik untuk S2/S3 dalam & luar negeri
• **Chevening** → untuk pemimpin dengan pengalaman kerja
• **Erasmus** → prestasi akademik tinggi + ingin ke Eropa
• **MEXT** → minat ke Jepang
• **Unggulan** → prestasi nasional, kampus dalam negeri
• **Djarum** → mahasiswa semester 4, aktif organisasi

Tanyakan nama beasiswa tertentu untuk info lengkap!"""
        }

        // General kursus
        if (q.contains("kursus") || q.contains("course") || q.contains("belajar")) {
            return """📚 **Kursus di ScholarSpace:**

ScholarSpace punya 6 kursus pilihan dari platform terpercaya:
🆓 **Gratis:** Kotlin (Dicoding), Python IBM (Coursera), Bisnis Digital (Coursera)
💰 **Berbayar:** Jetpack Compose (Udemy), UI/UX Figma (Udemy), IELTS Prep (Coursera)

**Kategori Tersedia:**
• 💻 Teknologi (Kotlin, Android, Python)
• 🎨 Desain (UI/UX Figma)
• 🗣️ Bahasa (IELTS Prep)
• 💼 Bisnis (Digital Marketing & Startup)

Ketik "daftar kursus" untuk melihat semua kursus, atau tanyakan kursus tertentu!"""
        }

        // Universitas / kampus
        if (q.contains("universitas") || q.contains("kampus") || q.contains(" ui ") || q.contains("itb") || q.contains("ugm") || q.contains("unhas") || q.contains("universitas terbaik")) {
            return """🏫 **Universitas Terbaik di Indonesia (QS Ranking 2024):**

1. **Universitas Indonesia (UI)** — Jakarta | Peringkat #1 Indonesia
2. **Institut Teknologi Bandung (ITB)** — Bandung | Teknik & Sains terbaik
3. **Universitas Gadjah Mada (UGM)** — Yogyakarta | Multidisiplin terkuat
4. **IPB University** — Bogor | Pertanian & Lingkungan
5. **Universitas Airlangga (UNAIR)** — Surabaya | Kesehatan & Hukum
6. **Universitas Hasanuddin (UNHAS)** — Makassar | Indonesia Timur #1

💡 **Tips Memilih Kampus:**
• Cek akreditasi jurusan (min. B untuk Beasiswa Unggulan)
• Pertimbangkan fasilitas riset untuk yang ingin LPDP/S3
• Lihat alumni network dan tingkat employability

Apakah Anda ingin info spesifik tentang kampus tertentu?"""
        }

        // Fallback
        return """🤖 **ScholarSpace AI**

Terima kasih atas pertanyaan Anda! 😊

Saya bisa membantu dengan:
🎓 **Beasiswa** — LPDP, Chevening, Erasmus, MEXT, Unggulan, Djarum
📚 **Kursus** — Kotlin, Android, Python, UI/UX, IELTS, Bisnis
💡 **Tips** — Cara daftar beasiswa, menulis motivation letter
🏫 **Kampus** — Rekomendasi universitas terbaik Indonesia

**Contoh pertanyaan:**
• "Apa syarat beasiswa LPDP?"
• "Rekomendasikan kursus untuk pemula programming"
• "Tips menulis motivation letter beasiswa"
• "Daftar semua beasiswa yang tersedia"
• "Kursus UI/UX terbaik ada apa?"

Silakan tanyakan lebih spesifik, saya siap membantu! 💪"""
    }
}
