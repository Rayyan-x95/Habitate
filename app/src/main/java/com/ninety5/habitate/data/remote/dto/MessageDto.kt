package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.JsonClass
import com.ninety5.habitate.data.local.entity.MessageStatus

@JsonClass(generateAdapter = true)
data class MessageDto(
    val id: String,
    val chatId: String,
    val senderId: String,
    val content: String?,
    val mediaUrl: String?,
    val status: MessageStatus,
    val createdAt: Long
)
