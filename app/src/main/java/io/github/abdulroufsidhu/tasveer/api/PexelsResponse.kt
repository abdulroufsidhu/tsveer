package io.github.abdulroufsidhu.tasveer.api

data class PexelsResponse(
    val data: List<PexelPic?>?,
    val pagination: Pagination
) {
    data class Pagination(
        val current_page: Int,
        val total_pages: Int,
        val total_results: Int,
    )
}

data class PexelPic(
    val id: String,
    val type: String,
    val attributes: Attribute
) {
    data class Image(
        val small: String,
        val medium: String,
        val large: String,
        val download: String,
        val download_link: String,
    )

    data class Attribute(
        val id: Long,
        val image: Image
    )
}