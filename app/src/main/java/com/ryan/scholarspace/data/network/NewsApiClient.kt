package com.ryan.scholarspace.data.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class NewsArticle(
    @Json(name = "title") val title: String = "",
    @Json(name = "description") val description: String? = null,
    @Json(name = "url") val url: String = "",
    @Json(name = "urlToImage") val urlToImage: String? = null,
    @Json(name = "publishedAt") val publishedAt: String = "",
    @Json(name = "source") val source: NewsSource? = null
)

@JsonClass(generateAdapter = true)
data class NewsSource(
    @Json(name = "name") val name: String = ""
)

@JsonClass(generateAdapter = true)
data class NewsResponse(
    @Json(name = "status") val status: String = "",
    @Json(name = "totalResults") val totalResults: Int = 0,
    @Json(name = "articles") val articles: List<NewsArticle> = emptyList()
)

interface NewsApiService {
    @GET("v2/everything")
    suspend fun getEducationNews(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") apiHost: String,
        @Query("q") query: String = "beasiswa pendidikan Indonesia",
        @Query("language") language: String = "id",
        @Query("sortBy") sortBy: String = "publishedAt",
        @Query("pageSize") pageSize: Int = 10
    ): NewsResponse
}

object NewsApiClient {
    private const val BASE_URL = "https://news-api14.p.rapidapi.com/"
    private const val API_HOST = "news-api14.p.rapidapi.com"
    // Gunakan API key RapidAPI gratis - daftar di rapidapi.com
    private const val API_KEY = "874f31c739msh89718c96aa3af87p1c731ejsn238ce3f29621"

    private val moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .build()

    private val service: NewsApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(NewsApiService::class.java)
    }

    suspend fun getEducationNews(): Result<List<NewsArticle>> {
        return try {
            val response = service.getEducationNews(
                apiKey = API_KEY,
                apiHost = API_HOST,
                query = "beasiswa pendidikan kuliah",
                language = "id",
                pageSize = 10
            )
            if (response.articles.isNotEmpty()) {
                Result.success(response.articles)
            } else {
                Result.failure(Exception("Tidak ada berita tersedia"))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}
