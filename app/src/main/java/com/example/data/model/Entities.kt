package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "books")
data class Book(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val title: String,
    val author: String,
    val category: String, // "Philosophy", "Classics", "Fiction", "Research", "Journals"
    val content: String, // The actual text or ebook content
    val coverUrl: String = "",
    val progress: Float = 0f, // 0.0 to 1.0
    val currentPage: Int = 1,
    val totalPages: Int = 10,
    val year: String = "",
    val description: String = ""
)

@Entity(tableName = "annotations")
data class Annotation(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookId: String,
    val excerpt: String, // Selected text
    val note: String, // User's custom commentary
    val commentType: String = "Philosophical Commentary", // "Philosophical Commentary", "Historical Note", "Cross-Reference", "Footnote"
    val tags: String = "", // Comma-separated tags
    val offsetStart: Int = 0,
    val offsetEnd: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "literary_connections")
data class LiteraryConnection(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val sourceBookId: String,
    val targetBookId: String,
    val sourceConcept: String, // Custom keyword/tag or book page
    val targetConcept: String,
    val description: String, // Intellectual comment explaining the link
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "reading_journals")
data class ReadingJournal(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val bookId: String? = null, // Linked book
    val title: String,
    val body: String,
    val timestamp: Long = System.currentTimeMillis()
)
