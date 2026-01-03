package com.ninety5.habitate.core.utils

import android.content.Context
import android.util.Log
import java.io.File
import java.io.FileWriter
import java.io.PrintWriter
import java.util.UUID

object DebugLogger {
    // #region agent log
    private val sessionId = UUID.randomUUID().toString()
    private var context: Context? = null
    
    fun init(context: Context) {
        this.context = context.applicationContext
    }

    private fun escapeJson(str: String): String {
        return str.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    private fun valueToJson(value: Any?): String {
        return when (value) {
            null -> "null"
            is String -> "\"${escapeJson(value)}\""
            is Number -> value.toString()
            is Boolean -> value.toString()
            is Map<*, *> -> {
                val entries = value.entries.joinToString(",") { (k, v) ->
                    "\"${escapeJson(k.toString())}\":${valueToJson(v)}"
                }
                "{$entries}"
            }
            is List<*> -> {
                val items = value.joinToString(",") { valueToJson(it) }
                "[$items]"
            }
            else -> "\"${escapeJson(value.toString())}\""
        }
    }

    fun log(
        location: String,
        message: String,
        data: Map<String, Any?> = emptyMap(),
        hypothesisId: String? = null,
        runId: String = "run1"
    ) {
        try {
            val logId = "log_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(8)}"
            val timestamp = System.currentTimeMillis()
            
            val json = buildString {
                append("{")
                append("\"id\":\"${escapeJson(logId)}\",")
                append("\"timestamp\":$timestamp,")
                append("\"location\":\"${escapeJson(location)}\",")
                append("\"message\":\"${escapeJson(message)}\",")
                append("\"data\":${valueToJson(data)},")
                append("\"sessionId\":\"${escapeJson(sessionId)}\",")
                append("\"runId\":\"${escapeJson(runId)}\",")
                append("\"hypothesisId\":${if (hypothesisId != null) "\"${escapeJson(hypothesisId)}\"" else "null"}")
                append("}")
            }

            // Try to write to external files directory (accessible via ADB)
            val ctx = context
            if (ctx != null) {
                val logDir = File(ctx.getExternalFilesDir(null), "debug_logs")
                logDir.mkdirs()
                val logFile = File(logDir, "debug.log")
                
                PrintWriter(FileWriter(logFile, true)).use { writer ->
                    writer.println(json)
                }
                Log.d("DebugLogger", "Log written to: ${logFile.absolutePath}")
            } else {
                // Fallback: try workspace path (for testing)
                val workspaceRoot = System.getProperty("user.dir") ?: ""
                if (workspaceRoot.isNotEmpty()) {
                    val logFile = File(workspaceRoot, ".cursor/debug.log")
                    logFile.parentFile?.mkdirs()
                    PrintWriter(FileWriter(logFile, true)).use { writer ->
                        writer.println(json)
                    }
                } else {
                    Log.e("DebugLogger", "No context initialized and no workspace root available")
                }
            }
        } catch (e: Exception) {
            Log.e("DebugLogger", "Failed to write debug log: ${e.message}", e)
            e.printStackTrace()
        }
    }
    // #endregion
}

