package com.example.data.network

import com.example.BuildConfig
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
data class Part(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    @Json(name = "parts") val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    @Json(name = "contents") val contents: List<Content>,
    @Json(name = "systemInstruction") val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class PartResponse(
    @Json(name = "text") val text: String? = null
)

@JsonClass(generateAdapter = true)
data class ContentResponse(
    @Json(name = "parts") val parts: List<PartResponse>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    @Json(name = "content") val content: ContentResponse? = null
)

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

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

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

    suspend fun askGemini(prompt: String, conversationHistory: List<Content> = emptyList()): String {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return getOfflineResponse(prompt)
        }

        // Prepare the contents with conversation history + current prompt
        val currentContent = Content(parts = listOf(Part(text = prompt)))
        val fullContents = conversationHistory + currentContent

        val systemInstructionText = """
            Anda adalah ScholarSpace AI, asisten spesialis pendidikan tinggi dan pelatihan kursus online untuk mahasiswa di Indonesia. 
            Jawab semua pertanyaan terkait beasiswa (seperti LPDP, Beasiswa Unggulan, Erasmus, MEXT) dan rekomendasi kursus pemrograman/desain/bisnis (seperti Udemy, Coursera, Dicoding) dengan santun, informatif, dan terstruktur menggunakan bahasa Indonesia yang baik dan profesional.
            
            Jika ditanya tentang instansi atau kampus di Indonesia, berikan saran yang sangat membantu. 
            Selalu gunakan format paragraf atau poin-poin yang mudah dibaca oleh pengguna.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = fullContents,
            systemInstruction = Content(parts = listOf(Part(text = systemInstructionText)))
        )

        return try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                ?: "Maaf, tidak dapat menerima jawaban dari asisten AI saat ini."
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback gracefully to offline assistant intelligence
            getOfflineResponse(prompt)
        }
    }

    private fun getOfflineResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("lpdp") || lower.contains("beasiswa pemerintah") -> {
                "**[Edukasi Offline]** Beasiswa LPDP (Lembaga Pengelola Dana Pendidikan) di bawah Kemenkeu biasanya dibuka dalam 2 tahap setiap tahun (Januari dan Juni/Juli). " +
                        "Syarat utamanya adalah: \n" +
                        "1. TOEFL iBT 80+ atau IELTS 6.5+ untuk luar negeri.\n" +
                        "2. LoA Unconditional jika ada.\n" +
                        "3. Rencana studi & proposal penelitian.\n\n" +
                        "Saran AI offline: Mulailah mempersiapkan esai kontribusi dan sertifikat IELTS dari sekarang!"
            }
            lower.contains("bahasa inggris") || lower.contains("ielts") || lower.contains("toefl") -> {
                "**[Edukasi Offline]** Untuk syarat bahasa Inggris beasiswa luar negeri, IELTS biasanya menjadi standar emas (min. 6.5 atau 7.0).\n\n" +
                        "Rekomendasi kursus gratis offline:\n" +
                        "- **IELTS Preparation Specialization** di Coursera (Macquarie University)\n" +
                        "- Saluran YouTube **IELTS Liz** atau **E2 IELTS** untuk latihan gratis."
            }
            lower.contains("programming") || lower.contains("kotlin") || lower.contains("pemrograman") || lower.contains("android") -> {
                "**[Edukasi Offline]** Belajar Android modern paling baik menggunakan bahasa Kotlin dan Jetpack Compose.\n\n" +
                        "Rekomendasi kursus:\n" +
                        "1. **Belajar Dasar Pemrograman Kotlin** di Dicoding (resmi bermitra dengan Google).\n" +
                        "2. **Android Jetpack Compose Masterclass** di Udemy.\n\n" +
                        "Fokuslah pada State Management, MVVM, dan Room database untuk membuat aplikasi tangguh."
            }
            lower.contains("desain") || lower.contains("ui") || lower.contains("ux") || lower.contains("figma") -> {
                "**[Edukasi Offline]** Desain UI/UX berpusat pada pemecahan masalah pengguna.\n\n" +
                        "Rekomendasi kursus:\n" +
                        "1. **Google UX Design Professional Certificate** di Coursera.\n" +
                        "2. **Desain UI/UX dengan Figma untuk Pemula** di Udemy.\n\n" +
                        "Pelajari wireframing, color theory, dan buatlah portofolio di Dribbble/Behance."
            }
            else -> {
                "**[ScholarSpace AI Mode Pintar]** Terima kasih atas pertanyaannya! Beasiswa dan Kursus adalah jembatan emas menuju karier impian Anda.\n\n" +
                        "Anda bisa bertanya secara spesifik tentang:\n" +
                        "1. Persyaratan beasiswa luar negeri (Chevening, Erasmus, MEXT)\n" +
                        "2. Cara mendaftar LPDP dan Beasiswa Unggulan\n" +
                        "3. Kursus teknologi (Kotlin, Flutter, Web, Python)\n" +
                        "4. Pelatihan bisnis/startup gratis.\n\n" +
                        "_Catatan: Harap masukkan API Key Gemini Anda di panel rahasia AI Studio jika ingin terhubung dengan AI live secara real-time._"
            }
        }
    }
}
