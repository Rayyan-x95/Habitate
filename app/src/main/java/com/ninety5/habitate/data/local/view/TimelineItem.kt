package com.ninety5.habitate.data.local.view

import androidx.room.DatabaseView

@DatabaseView(
    viewName = "timeline_view",
    value = """
        SELECT id, 'post' as type, createdAt as timestamp, contentText as title, NULL as subtitle, isArchived FROM posts
        UNION ALL
        SELECT id, 'workout' as type, startTs as timestamp, type as title, NULL as subtitle, isArchived FROM workouts
        UNION ALL
        SELECT id, 'story' as type, createdAt as timestamp, caption as title, NULL as subtitle, 0 as isArchived FROM stories WHERE isSaved = 1
        UNION ALL
        SELECT id, 'task' as type, updatedAt as timestamp, title as title, status as subtitle, CASE WHEN status = 'ARCHIVED' THEN 1 ELSE 0 END as isArchived FROM tasks WHERE status = 'DONE' OR status = 'ARCHIVED'
        UNION ALL
        SELECT id, 'insight' as type, createdAt as timestamp, title as title, description as subtitle, isDismissed as isArchived FROM insights
        UNION ALL
        SELECT id, 'journal' as type, date as timestamp, COALESCE(title, 'Journal Entry') as title, content as subtitle, 0 as isArchived FROM journal_entries
    """
)
data class TimelineItem(
    val id: String,
    val type: String, // 'post', 'workout', 'story', 'task'
    val timestamp: Long,
    val title: String?,
    val subtitle: String?,
    val isArchived: Boolean
)
