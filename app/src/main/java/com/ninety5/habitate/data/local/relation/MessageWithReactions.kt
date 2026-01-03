package com.ninety5.habitate.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.ninety5.habitate.data.local.entity.MessageEntity
import com.ninety5.habitate.data.local.entity.MessageReactionEntity

data class MessageWithReactions(
    @Embedded val message: MessageEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "messageId"
    )
    val reactions: List<MessageReactionEntity>
)
