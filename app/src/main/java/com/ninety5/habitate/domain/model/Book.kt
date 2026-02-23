package com.ninety5.habitate.domain.model

/**
 * Domain model for a book.
 */
data class Book(
    val key: String,
    val title: String,
    val authors: List<String>,
    val firstPublishYear: Int?,
    val coverId: Int?
)
