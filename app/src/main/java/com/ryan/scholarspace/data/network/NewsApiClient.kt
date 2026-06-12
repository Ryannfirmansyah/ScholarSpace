package com.ryan.scholarspace.data.network

import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.util.concurrent.TimeUnit

data class NewsArticle(
    val title: String = "",
    val description: String? = null,
    val url: String = "",
    val urlToImage: String? = null,
    val publishedAt: String = "",
    val source: NewsSource? = null
)

data class NewsSource(
    val name: String = ""
)

object NewsApiClient {
    private const val RSS_URL =
        "https://news.google.com/rss/search?q=beasiswa+pendidikan+kursus+Indonesia&hl=id&gl=ID&ceid=ID:id"

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    suspend fun getEducationNews(): Result<List<NewsArticle>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(RSS_URL)
                .header("User-Agent", "Mozilla/5.0 (Android)")
                .build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}"))
            }
            val xml = response.body?.string()
                ?: return@withContext Result.failure(Exception("Respons kosong"))
            val articles = parseRss(xml)
            if (articles.isEmpty()) Result.failure(Exception("Tidak ada berita tersedia"))
            else Result.success(articles)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private fun parseRss(xml: String): List<NewsArticle> {
        val articles = mutableListOf<NewsArticle>()
        try {
            val parser = Xml.newPullParser()
            parser.setInput(xml.reader())

            var inItem = false
            var tag = ""
            var title = ""; var desc = ""; var link = ""; var pubDate = ""; var sourceName = ""

            var event = parser.eventType
            while (event != XmlPullParser.END_DOCUMENT) {
                when (event) {
                    XmlPullParser.START_TAG -> {
                        tag = parser.name ?: ""
                        if (tag == "item") {
                            inItem = true
                            title = ""; desc = ""; link = ""; pubDate = ""; sourceName = ""
                        }
                        if (tag == "source" && inItem && parser.attributeCount > 0) {
                            sourceName = parser.getAttributeValue(null, "url") ?: ""
                        }
                    }
                    XmlPullParser.TEXT, XmlPullParser.CDSECT -> if (inItem) {
                        val t = parser.text?.trim() ?: ""
                        if (t.isNotEmpty()) when (tag) {
                            "title" -> title += t
                            "description" -> if (desc.isEmpty()) desc = t
                            "link" -> if (link.isEmpty()) link = t
                            "pubDate" -> if (pubDate.isEmpty()) pubDate = t
                            "source" -> if (sourceName.isEmpty()) sourceName = t
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (parser.name == "item" && inItem) {
                            val cleanTitle = title.stripHtml()
                            val cleanDesc = desc.stripHtml()
                            if (cleanTitle.isNotEmpty()) {
                                articles.add(
                                    NewsArticle(
                                        title = cleanTitle,
                                        description = cleanDesc.ifEmpty { null },
                                        url = link,
                                        publishedAt = formatDate(pubDate),
                                        source = NewsSource(sourceName.ifEmpty { "Google News" })
                                    )
                                )
                            }
                            inItem = false
                        }
                        tag = ""
                    }
                }
                event = parser.next()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return articles.take(15)
    }

    private fun String.stripHtml(): String =
        replace(Regex("<[^>]+>"), " ").replace(Regex("\\s+"), " ").trim()

    private fun formatDate(rssDate: String): String {
        val regex = Regex("""(\d{1,2})\s+(\w{3})\s+(\d{4})""")
        val match = regex.find(rssDate) ?: return rssDate.take(10)
        val (day, month, year) = match.destructured
        val monthNum = when (month.lowercase()) {
            "jan" -> "01"; "feb" -> "02"; "mar" -> "03"; "apr" -> "04"
            "may" -> "05"; "jun" -> "06"; "jul" -> "07"; "aug" -> "08"
            "sep" -> "09"; "oct" -> "10"; "nov" -> "11"; "dec" -> "12"
            else -> "01"
        }
        return "$year-$monthNum-${day.padStart(2, '0')}"
    }
}
