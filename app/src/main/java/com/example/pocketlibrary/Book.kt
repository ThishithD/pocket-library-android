package com.example.pocketlibrary

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BookApiResponse(
    @Json(name = "docs")
    val docs: List<BookDoc>
)

@JsonClass(generateAdapter = true)
data class BookDoc(
    @Json(name = "key")
    val key: String,

    @Json(name = "title")
    val title: String,

    @Json(name = "author_name")
    val authorName: List<String>?,

    @Json(name = "first_publish_year")
    val firstPublishYear: Int?,

    @Json(name = "cover_i")
    val coverId: Int?
) {
    val author: String
        get() = authorName?.joinToString(", ") ?: "Unknown Author"

    val coverImageUrl: String?
        get() = coverId?.let { "https://covers.openlibrary.org/b/id/$it-M.jpg" }
}