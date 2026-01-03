package com.ninety5.habitate.data.remote.dto

import com.squareup.moshi.JsonClass
import com.ninety5.habitate.data.local.entity.ChatType

@JsonClass(generateAdapter = true)
data class ChatDto(
    val id: String,
    val type: ChatType,
    val title: String?,
    val lastMessage: String?,
    val updatedAt: Long
)
