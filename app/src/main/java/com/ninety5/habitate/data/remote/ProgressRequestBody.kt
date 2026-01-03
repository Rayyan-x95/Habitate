package com.ninety5.habitate.data.remote

import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okio.BufferedSink
import okio.Okio
import okio.source
import java.io.File

class ProgressRequestBody(
    private val file: File,
    private val contentType: String,
    private val onProgress: (Float) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = contentType.toMediaTypeOrNull()

    override fun contentLength(): Long = file.length()

    override fun writeTo(sink: BufferedSink) {
        val source = file.source()
        var total: Long = 0
        var read: Long

        try {
            while (source.read(sink.buffer, 2048).also { read = it } != -1L) {
                total += read
                sink.flush()
                onProgress(total.toFloat() / contentLength())
            }
        } finally {
            source.close()
        }
    }
}
