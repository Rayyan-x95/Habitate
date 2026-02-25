package com.ninety5.habitate.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "habitat_memberships",
    foreignKeys = [
        ForeignKey(
            entity = HabitatEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitatId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["habitatId", "userId"], unique = true),
        Index(value = ["habitatId"]),
        Index(value = ["userId"])
    ]
)
data class HabitatMembershipEntity(
    @PrimaryKey val id: String,
    val habitatId: String,
    val userId: String,
    val role: HabitatRole,
    val syncState: SyncState
)
