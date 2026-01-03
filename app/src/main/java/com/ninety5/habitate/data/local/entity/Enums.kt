package com.ninety5.habitate.data.local.entity

enum class SyncState { PENDING, SYNCED, FAILED }
enum class TaskStatus { OPEN, DONE, ARCHIVED }
enum class WorkoutSource { MANUAL, HEALTH_CONNECT }
enum class SyncStatus { PENDING, IN_PROGRESS, FAILED, COMPLETED }
enum class Visibility { PUBLIC, PRIVATE, FRIENDS }
enum class HabitatRole { OWNER, ADMIN, MEMBER }
enum class HabitatPrivacy { PUBLIC, PRIVATE, SECRET }
enum class ChatType { DIRECT, HABITAT, FOCUS }
enum class MessageStatus { SENDING, SENT, DELIVERED, READ, FAILED }
enum class TaskPriority { LOW, MEDIUM, HIGH }
