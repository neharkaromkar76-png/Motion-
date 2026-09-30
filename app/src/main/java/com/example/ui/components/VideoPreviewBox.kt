package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.FastRewind
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Keyframe
import com.example.data.model.SubjectRegion
import com.example.engine.motion.Interpolator
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioBorder
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

enum class PreviewMode {
    EDITED, ORIGINAL, SPLIT
}

@Composable
fun VideoPreviewBox(
    frameBitmap: Bitmap?,
    currentTimeMs: Long,
    totalDurationMs: Long,
    keyframes: List<Keyframe>,
    targetSubject: SubjectRegion?,
    isPlaying: Boolean,
    onTogglePlay: () -> Unit,
    onSeek: (Long) -> Unit,
    modifier: Modifier = Modifier,
    aspectRatio: Float = 16f / 9f
) {
    var previewMode by remember { mutableStateOf(PreviewMode.EDITED) }
    var showSubjectGuide by remember { mutableStateOf(false) }
    var splitFraction by remember { mutableFloatStateOf(0.5f) }

    val currentTransform = remember(currentTimeMs, keyframes) {
        Interpolator.evaluateKeyframeAtTime(currentTimeMs, keyframes)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF070A0F))
            .border(1.dp, StudioBorder, RoundedCornerShape(16.dp))
    ) {
        // Top Toolbar: Mode Switcher & Guide toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Segmented preview mode
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioSurfaceHighlight)
                    .padding(2.dp)
            ) {
                PreviewMode.values().forEach { mode ->
                    val selected = mode == previewMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (selected) StudioCyan else Color.Transparent)
                            .clickable { previewMode = mode }
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .testTag("preview_mode_${mode.name.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = when (mode) {
                                PreviewMode.EDITED -> "EDITED"
                                PreviewMode.ORIGINAL -> "ORIGINAL"
                                PreviewMode.SPLIT -> "SPLIT"
                            },
                            color = if (selected) Color.Black else StudioTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                }
            }

            // Subject guide toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { showSubjectGuide = !showSubjectGuide }
                    .background(if (showSubjectGuide) StudioAmber.copy(alpha = 0.2f) else Color.Transparent)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CenterFocusStrong,
                    contentDescription = "Subject Guide",
                    tint = if (showSubjectGuide) StudioAmber else StudioTextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Subject Guide",
                    color = if (showSubjectGuide) StudioAmber else StudioTextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Video Viewport
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio.coerceIn(0.5f, 2.2f))
                .background(Color.Black)
                .clip(RoundedCornerShape(0.dp)),
            contentAlignment = Alignment.Center
        ) {
            val boxWidth = maxWidth
            val boxHeight = maxHeight

            if (frameBitmap != null) {
                when (previewMode) {
                    PreviewMode.ORIGINAL -> {
                        // Original Untransformed
                        Image(
                            bitmap = frameBitmap.asImageBitmap(),
                            contentDescription = "Original Frame",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    PreviewMode.EDITED -> {
                        // Transformed with Keyframes
                        val scale = currentTransform.scale
                        // Normalize shift relative to viewport size
                        val transX = (currentTransform.x - 0.5f) * boxWidth.value * scale * -0.5f
                        val transY = (currentTransform.y - 0.5f) * boxHeight.value * scale * -0.5f

                        Image(
                            bitmap = frameBitmap.asImageBitmap(),
                            contentDescription = "Keyframed Frame",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    scaleX = scale
                                    scaleY = scale
                                    translationX = transX
                                    translationY = transY
                                    rotationZ = currentTransform.rotation
                                }
                        )
                    }
                    PreviewMode.SPLIT -> {
                        // Split view with interactive slider
                        Box(modifier = Modifier.fillMaxSize()) {
                            // Left side: original
                            Image(
                                bitmap = frameBitmap.asImageBitmap(),
                                contentDescription = "Original Frame",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Right side: edited
                            val scale = currentTransform.scale
                            val transX = (currentTransform.x - 0.5f) * boxWidth.value * scale * -0.5f
                            val transY = (currentTransform.y - 0.5f) * boxHeight.value * scale * -0.5f

                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(1f - splitFraction)
                                    .align(Alignment.CenterEnd)
                                    .clip(RoundedCornerShape(0.dp))
                            ) {
                                Image(
                                    bitmap = frameBitmap.asImageBitmap(),
                                    contentDescription = "Edited Frame",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            scaleX = scale
                                            scaleY = scale
                                            translationX = transX
                                            translationY = transY
                                            rotationZ = currentTransform.rotation
                                        }
                                )
                            }

                            // Divider line
                            Box(
                                modifier = Modifier
                                    .offset { IntOffset((boxWidth.toPx() * splitFraction).toInt() - 2, 0) }
                                    .width(4.dp)
                                    .fillMaxHeight()
                                    .background(StudioCyan)
                                    .pointerInput(Unit) {
                                        detectDragGestures { change, dragAmount ->
                                            change.consume()
                                            splitFraction = (splitFraction + (dragAmount.x / boxWidth.toPx())).coerceIn(0.1f, 0.9f)
                                        }
                                    }
                            )
                        }
                    }
                }
            } else {
                // Placeholder when video frame is loading/not loaded
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        imageVector = Icons.Default.ViewCarousel,
                        contentDescription = null,
                        tint = StudioTextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Video Preview Viewport",
                        color = StudioTextSecondary,
                        fontSize = 13.sp
                    )
                }
            }

            // Subject Guide Overlay
            if (showSubjectGuide && targetSubject != null) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val cx = targetSubject.centerX * w
                    val cy = targetSubject.centerY * h
                    val sw = targetSubject.width * w
                    val sh = targetSubject.height * h

                    // Draw bounding box
                    drawRect(
                        color = StudioAmber,
                        topLeft = Offset(cx - sw / 2, cy - sh / 2),
                        size = Size(sw, sh),
                        style = Stroke(width = 2.dp.toPx())
                    )

                    // Draw center reticle
                    drawLine(
                        color = StudioAmber,
                        start = Offset(cx - 15f, cy),
                        end = Offset(cx + 15f, cy),
                        strokeWidth = 2.dp.toPx()
                    )
                    drawLine(
                        color = StudioAmber,
                        start = Offset(cx, cy - 15f),
                        end = Offset(cx, cy + 15f),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Telemetry overlay in bottom-left corner
            Row(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(8.dp)
                    .background(Color.Black.copy(alpha = 0.65f), RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = String.format("Zoom: %.2fx", currentTransform.scale),
                    color = StudioCyan,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = String.format("Rot: %.1f°", currentTransform.rotation),
                    color = StudioAmber,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = String.format("Pos: (%.2f, %.2f)", currentTransform.x, currentTransform.y),
                    color = StudioTextSecondary,
                    fontSize = 10.sp,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // Scrubber and Playback Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(StudioSurfaceElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Slider
            val maxDur = maxOf(1000L, totalDurationMs)
            Slider(
                value = currentTimeMs.toFloat(),
                onValueChange = { onSeek(it.toLong()) },
                valueRange = 0f..maxDur.toFloat(),
                colors = SliderDefaults.colors(
                    thumbColor = StudioCyan,
                    activeTrackColor = StudioCyan,
                    inactiveTrackColor = Color(0xFF263248)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("preview_scrubber_slider")
            )

            // Playback buttons & Time code
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timecode
                val curSec = (currentTimeMs / 1000f)
                val totSec = (totalDurationMs / 1000f)
                Text(
                    text = String.format("%04.1fs / %04.1fs", curSec, totSec),
                    color = StudioTextPrimary,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.SemiBold
                )

                // Controls: Rewind, Play/Pause, Fast Forward
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { onSeek((currentTimeMs - 1000).coerceAtLeast(0L)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastRewind,
                            contentDescription = "Rewind 1s",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(StudioCyan)
                            .clickable { onTogglePlay() }
                            .testTag("preview_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.Black,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = { onSeek((currentTimeMs + 1000).coerceAtMost(maxDur)) },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FastForward,
                            contentDescription = "Forward 1s",
                            tint = StudioTextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Active keyframe counter
                val activeKf = keyframes.find { kotlin.math.abs(it.timestampMs - currentTimeMs) < 250 }
                if (activeKf != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(StudioAmber.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "◆ KEYFRAME",
                            color = StudioAmber,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    Text(
                        text = "${keyframes.size} Keyframes",
                        color = StudioTextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}
