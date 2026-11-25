package com.example.pocketlibrary

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class BookEntity(
    @PrimaryKey
    val apiKey: String,
    val title: String,
    val author: String,
    val publishYear: Int?,
    val coverUrl: String?
)