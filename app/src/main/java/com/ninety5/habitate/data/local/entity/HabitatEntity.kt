package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

import com.ninety5.habitate.data.local.entity.HabitatPrivacy

@Entity(tableName = "habitats")
data class HabitatEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String?,
    val coverImageUrl: String?,
    val memberCount: Int,
    val privacy: HabitatPrivacy = HabitatPrivacy.PUBLIC,
    val syncState: SyncState = SyncState.SYNCED,
    val updatedAt: Long = System.currentTimeMillis()
)
