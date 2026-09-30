package com.example.engine.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Shader
import android.net.Uri
import com.example.data.model.VideoMetadata

object SampleMediaHelper {

    const val REFERENCE_SAMPLE_URI = "keyframe_sample://reference_cyberpunk_whip_pan.mp4"
    const val TARGET_SAMPLE_URI = "keyframe_sample://target_urban_gameplay.mp4"

    fun isSampleUri(uri: Uri): Boolean {
        val s = uri.toString()
        return s.contains("keyframe_sample") || s.contains("sample_reference") || s.contains("sample_target")
    }

    fun isReferenceSample(uri: Uri): Boolean {
        return uri.toString().contains("reference")
    }

    fun getSampleMetadata(uri: Uri): VideoMetadata {
        val isRef = isReferenceSample(uri)
        return if (isRef) {
            VideoMetadata(
                uri = uri.toString(),
                fileName = "Cyberpunk_WhipPan_Reference.mp4",
                durationMs = 8000L,
                width = 1080,
                height = 1920,
                fps = 60f,
                rotation = 0,
                bitrate = 14000000L,
                fileSize = 13500000L,
                hasAudio = true
            )
        } else {
            VideoMetadata(
                uri = uri.toString(),
                fileName = "Urban_Action_Target.mp4",
                durationMs = 14000L,
                width = 1080,
                height = 1920,
                fps = 30f,
                rotation = 0,
                bitrate = 9500000L,
                fileSize = 16200000L,
                hasAudio = true
            )
        }
    }

    fun generateSampleFrame(
        isReference: Boolean,
        timeMs: Long,
        width: Int = 1080,
        height: Int = 1920
    ): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val tSec = timeMs / 1000f

        if (isReference) {
            // Draw Cyberpunk Cinematic Reference Scene
            val gradient = LinearGradient(
                0f, 0f, width.toFloat(), height.toFloat(),
                Color.rgb(15, 12, 41), Color.rgb(36, 36, 62),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            // Cybernetic grid lines
            paint.color = Color.argb(45, 0, 229, 255)
            paint.strokeWidth = 2f
            for (y in 0 until height step 80) {
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
            }
            for (x in 0 until width step 80) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
            }

            // Reference Motion HUD
            paint.color = Color.rgb(0, 229, 255)
            paint.textSize = 44f
            paint.isFakeBoldText = true
            canvas.drawText("REFERENCE MOTION SOURCE", 60f, 160f, paint)

            paint.color = Color.rgb(255, 179, 0)
            paint.textSize = 34f
            canvas.drawText("TC: ${String.format("%02.2fs", tSec)}  |  DYNAMIC WHIP PAN", 60f, 220f, paint)

            // Dynamic Motion Vector Arrow
            paint.color = Color.rgb(0, 229, 255)
            paint.strokeWidth = 8f
            val midY = height / 2f
            val panShift = (kotlin.math.sin(tSec * 2.0) * 180f).toFloat()
            val arrowX = (width / 2f) + panShift
            canvas.drawLine(width / 2f, midY, arrowX, midY, paint)
            canvas.drawCircle(arrowX, midY, 24f, paint)

            // Dynamic zoom reticle
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.rgb(255, 179, 0)
            val zoomRadius = 240f + (kotlin.math.sin(tSec * 1.5) * 50f).toFloat()
            canvas.drawCircle(arrowX, midY, zoomRadius, paint)
            paint.style = Paint.Style.FILL

        } else {
            // Draw Target Gameplay / Action Scene
            val gradient = LinearGradient(
                0f, 0f, 0f, height.toFloat(),
                Color.rgb(18, 24, 38), Color.rgb(10, 14, 22),
                Shader.TileMode.CLAMP
            )
            paint.shader = gradient
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
            paint.shader = null

            // Subtle studio grid
            paint.color = Color.argb(30, 255, 255, 255)
            paint.strokeWidth = 2f
            for (y in 0 until height step 100) {
                canvas.drawLine(0f, y.toFloat(), width.toFloat(), y.toFloat(), paint)
            }
            for (x in 0 until width step 100) {
                canvas.drawLine(x.toFloat(), 0f, x.toFloat(), height.toFloat(), paint)
            }

            // Target Video HUD
            paint.color = Color.rgb(0, 230, 118)
            paint.textSize = 44f
            paint.isFakeBoldText = true
            canvas.drawText("TARGET FOOTAGE (ORIGINAL)", 60f, 160f, paint)

            paint.color = Color.rgb(148, 163, 184)
            paint.textSize = 34f
            canvas.drawText("Timecode: ${String.format("%02.2fs", tSec)}  |  1080x1920", 60f, 220f, paint)

            // Primary Character / Subject Center
            val subX = width / 2f
            val subY = height / 2f - 40f

            // Subject avatar representation
            paint.color = Color.rgb(0, 229, 255)
            canvas.drawCircle(subX, subY - 140f, 70f, paint) // Head

            paint.color = Color.rgb(33, 150, 243)
            val torso = Path().apply {
                moveTo(subX - 110f, subY + 160f)
                lineTo(subX + 110f, subY + 160f)
                lineTo(subX + 80f, subY - 50f)
                lineTo(subX - 80f, subY - 50f)
                close()
            }
            canvas.drawPath(torso, paint) // Torso

            // Subject tracking bounding box
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 4f
            paint.color = Color.rgb(255, 179, 0)
            canvas.drawRect(subX - 180f, subY - 240f, subX + 180f, subY + 220f, paint)
            paint.style = Paint.Style.FILL

            paint.textSize = 28f
            paint.color = Color.rgb(255, 179, 0)
            canvas.drawText("[PRIMARY SUBJECT DETECTED]", subX - 170f, subY - 260f, paint)
        }

        return bitmap
    }
}
