package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "habitat_memberships")
data class HabitatMembershipEntity(
    @PrimaryKey val id: String,
    val habitatId: String,
    val userId: String,
    val role: HabitatRole,
    val syncState: SyncState
)
