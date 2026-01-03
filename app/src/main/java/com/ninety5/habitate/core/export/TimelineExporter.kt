package com.ninety5.habitate.core.export

import android.content.Context
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.ninety5.habitate.data.local.view.TimelineItem
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TimelineExporter @Inject constructor(
    @ApplicationContext private val context: Context,
    private val moshi: Moshi
) {

    suspend fun exportToJson(items: List<TimelineItem>, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val type = Types.newParameterizedType(List::class.java, TimelineItem::class.java)
            val adapter = moshi.adapter<List<TimelineItem>>(type)
            val json = adapter.toJson(items)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(json.toByteArray())
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun exportToPdf(items: List<TimelineItem>, uri: Uri): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint()
            paint.textSize = 12f
            
            var y = 40f
            val margin = 40f
            val lineHeight = 20f

            paint.textSize = 18f
            paint.isFakeBoldText = true
            canvas.drawText("Habitate Timeline Export", margin, y, paint)
            y += lineHeight * 2
            paint.textSize = 12f
            paint.isFakeBoldText = false

            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

            for (item in items) {
                if (y > 800) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 40f
                }

                val dateStr = dateFormat.format(Date(item.timestamp))
                paint.isFakeBoldText = true
                canvas.drawText("$dateStr - ${item.type.uppercase()}", margin, y, paint)
                y += lineHeight
                
                paint.isFakeBoldText = false
                val title = item.title ?: "Untitled"
                canvas.drawText(title, margin, y, paint)
                y += lineHeight

                if (item.subtitle != null) {
                    paint.color = android.graphics.Color.GRAY
                    canvas.drawText(item.subtitle, margin, y, paint)
                    paint.color = android.graphics.Color.BLACK
                    y += lineHeight
                }
                
                y += lineHeight // Spacer
            }

            pdfDocument.finishPage(page)

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
