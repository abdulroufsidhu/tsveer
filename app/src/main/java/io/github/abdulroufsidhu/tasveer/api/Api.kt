package io.github.abdulroufsidhu.tasveer.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.ANDROID
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.serialization.gson.gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class Api {

    companion object {
        private val client = HttpClient(OkHttp) {
            install(Logging) {
                logger = Logger.ANDROID
                level = io.ktor.client.plugins.logging.LogLevel.BODY
            }
            install(ContentNegotiation) {
                gson {
                    setLenient()
                    setPrettyPrinting()
                }
            }
        }
    }

    suspend fun pexels(
        pageNumber: Int = 0,
        query: String = "",
        orientation: String = "all",
        size: String = "all",
        sort: String = "popular"
    ): PexelsResponse = withContext(Dispatchers.IO) {
        val url = if (query.isNotBlank()) {
            "https://www.pexels.com/en-us/api/v3/search/photos?page=${pageNumber}&per_page=12&query=${query}&orientation=${orientation}&size=${size}&color=all&sort=${sort}&seo_tags=true"
        } else {
            "https://www.pexels.com/en-us/api/v2/feed?seed=${Clock.System.now().toString().replace(":", " %3A").replace(" ", "+")}&per_page=12&seo_tags=true&page=${pageNumber}"
        }
        val res = client.get(url) {
            header("Referer", "https://www.pexels.com/")
            header("Secret-Key", "H2jk9uKnhRmL6WPwh89zBezWvr")
        }.body<PexelsResponse>()
        res.copy(data = res.data ?: emptyList())
    }

    suspend fun unsplash(
        pageNumber: Int,
        query: String = ""
    ): List<UnsplashPic?> = withContext(Dispatchers.IO) {
        val url = if (query.isNotBlank()) {
            "https://unsplash.com/napi/search/photos?page=${pageNumber}&per_page=12&umt_medium=referral&query=${query}"
        } else {
            "https://unsplash.com/napi/photos?page=${pageNumber}&per_page=12&umt_medium=referral"
        }
        val request = client.get(url)
        val response =
            if (query.isNotBlank()) request.body<UnsplashResponse>().result else request.body<List<UnsplashPic?>?>()
        response?.filterNot { it?.urls?.full?.startsWith("https://plus.unsplash.com/") == true } ?: emptyList()
    }
}

