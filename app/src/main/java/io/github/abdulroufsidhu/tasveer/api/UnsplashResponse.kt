package io.github.abdulroufsidhu.tasveer.api

data class UnsplashResponse(
    val result: List<UnsplashPic?>?,
    val total: Long,
    val total_pages: Long,
)
data class UnsplashPic (
    val id: String,
    val urls: Urls
) {
    data class Urls(
        val raw: String,
        val full: String,
        val regular: String,
        val small: String,
        val thumb: String,
        val small_s3: String,
    )
}