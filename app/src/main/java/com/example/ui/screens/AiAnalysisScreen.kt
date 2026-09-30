package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CinematicCard
import com.example.ui.components.MetricChip
import com.example.ui.components.StudioPrimaryButton
import com.example.ui.components.StudioSecondaryButton
import com.example.ui.components.StudioStage
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioSurfaceHighlight
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.viewmodel.StudioViewModel

@Composable
fun AiAnalysisScreen(
    viewModel: StudioViewModel,
    modifier: Modifier = Modifier
) {
    val isAnalyzing by viewModel.isAnalyzing.collectAsState()
    val stageMessage by viewModel.analysisStageMessage.collectAsState()
    val progress by viewModel.analysisProgress.collectAsState()
    val aiResult by viewModel.aiResult.collectAsState()
    val targetKeyframes by viewModel.targetKeyframes.collectAsState()
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Stage Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(StudioCyan),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "3", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
            Column {
                Text(
                    text = "AI Motion Analysis",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = StudioTextPrimary
                )
                Text(
                    text = "Extracting camera kinematics, optical flow & keyframe vectors",
                    fontSize = 12.sp,
                    color = StudioTextSecondary
                )
            }
        }

        // Processing Card
        if (isAnalyzing) {
            CinematicCard(
                borderColor = StudioCyan,
                backgroundColor = Color(0xFF0D1420)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        color = StudioCyan,
                        trackColor = StudioSurfaceHighlight,
                        modifier = Modifier.size(54.dp),
                        strokeWidth = 4.dp
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stageMessage,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextPrimary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${(progress * 100).toInt()}% Complete",
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        color = StudioCyan
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        color = StudioCyan,
                        trackColor = Color(0xFF1E2838),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    StudioSecondaryButton(
                        text = "Cancel Analysis",
                        icon = Icons.Default.Cancel,
                        onClick = { viewModel.cancelAnalysis() },
                        modifier = Modifier.fillMaxWidth(0.6f),
                        testTag = "cancel_analysis_button"
                    )
                }
            }
        } else if (aiResult != null) {
            // Results Overview Card
            CinematicCard(borderColor = StudioEmerald.copy(alpha = 0.5f)) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = StudioEmerald, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Analysis Complete",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = StudioTextPrimary
                            )
                        }

                        // Demo Mode vs Live AI tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (aiResult!!.isDemo) StudioAmber.copy(alpha = 0.2f) else StudioCyan.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (aiResult!!.isDemo) "DEMO ANALYSIS" else "GEMINI MULTIMODAL",
                                color = if (aiResult!!.isDemo) StudioAmber else StudioCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Motion Style: ${aiResult!!.motionStyle}",
                        fontSize = 13.sp,
                        color = StudioTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = aiResult!!.notes,
                        fontSize = 11.sp,
                        color = StudioTextSecondary,
                        lineHeight = 15.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Metrics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        MetricChip(label = "Confidence", value = "${(aiResult!!.overallConfidence * 100).toInt()}%", icon = Icons.Default.Sensors, highlight = true)
                        MetricChip(label = "Events", value = "${aiResult!!.events.size} detected")
                        MetricChip(label = "Keyframes", value = "${targetKeyframes.size} adapted", icon = Icons.Default.Diamond)
                        MetricChip(label = "Scene Cuts", value = "${aiResult!!.sceneCutsCount}")
                    }
                }
            }

            // Detected Events breakdown
            CinematicCard {
                Column {
                    Text(
                        text = "Detected Reference Events",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    aiResult!!.events.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(StudioSurfaceHighlight)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(StudioCyan)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = event.type.displayName,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = StudioTextPrimary
                                    )
                                    Text(
                                        text = event.description,
                                        fontSize = 11.sp,
                                        color = StudioTextSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = String.format("%.1fs - %.1fs", event.startTimeMs / 1000f, event.endTimeMs / 1000f),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = StudioAmber
                                )
                                Text(
                                    text = "${(event.confidence * 100).toInt()}% conf",
                                    fontSize = 10.sp,
                                    color = StudioTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StudioSecondaryButton(
                    text = "Re-Analyze",
                    icon = Icons.Default.Refresh,
                    onClick = { viewModel.runAiAnalysis() },
                    modifier = Modifier.weight(1f),
                    testTag = "reanalyze_button"
                )
                StudioPrimaryButton(
                    text = "View Timeline & Preview",
                    icon = Icons.AutoMirrored.Filled.ArrowForward,
                    onClick = { viewModel.setStage(StudioStage.PREVIEW) },
                    modifier = Modifier.weight(1.5f),
                    testTag = "proceed_to_preview_button"
                )
            }
        } else {
            // Not started yet
            CinematicCard {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = StudioCyan,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Ready for Analysis",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Extract motion curves, camera zooms, and tracking paths from reference video.",
                        fontSize = 12.sp,
                        color = StudioTextSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    StudioPrimaryButton(
                        text = "START AI ANALYSIS",
                        icon = Icons.Default.AutoAwesome,
                        onClick = { viewModel.runAiAnalysis() },
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "start_analysis_button"
                    )
                }
            }
        }
    }
}
