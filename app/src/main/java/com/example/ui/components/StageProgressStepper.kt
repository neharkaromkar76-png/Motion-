package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.StudioAmber
import com.example.ui.theme.StudioCyan
import com.example.ui.theme.StudioEmerald
import com.example.ui.theme.StudioSurfaceElevated
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import com.example.ui.theme.StudioTextTertiary

enum class StudioStage(val index: Int, val shortLabel: String, val fullLabel: String) {
    HOME(0, "Home", "Projects & Home"),
    REFERENCE(1, "Reference", "Select Reference Video"),
    TARGET(2, "Target", "Select Target Video"),
    ANALYSIS(3, "AI Analyze", "Analyze Reference Motion"),
    TIMELINE(4, "Timeline", "Keyframe Timeline"),
    PREVIEW(5, "Preview", "Real-Time Preview"),
    FINE_TUNE(6, "Fine-Tune", "Fine-Tune Keyframes"),
    RENDER_EXPORT(7, "Export", "Render & Export")
}

@Composable
fun StageProgressStepper(
    currentStage: StudioStage,
    completedStages: Set<StudioStage>,
    onStageSelected: (StudioStage) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(StudioSurfaceElevated, RoundedCornerShape(12.dp))
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val workflowStages = StudioStage.values().filter { it != StudioStage.HOME }
            workflowStages.forEachIndexed { index, stage ->
                val isCurrent = stage == currentStage
                val isCompleted = completedStages.contains(stage)
                val isUnlocked = isCompleted || isCurrent || stage.index <= (completedStages.maxOfOrNull { it.index } ?: 0) + 1

                val circleBg by animateColorAsState(
                    targetValue = when {
                        isCurrent -> StudioCyan
                        isCompleted -> StudioEmerald
                        else -> Color(0xFF1E2638)
                    },
                    label = "circleBg"
                )

                val contentColor by animateColorAsState(
                    targetValue = when {
                        isCurrent -> Color.Black
                        isCompleted -> Color.Black
                        else -> StudioTextTertiary
                    },
                    label = "contentColor"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(enabled = isUnlocked) { onStageSelected(stage) }
                        .padding(horizontal = 6.dp, vertical = 4.dp)
                        .testTag("stepper_stage_${stage.name.lowercase()}")
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(circleBg),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isCompleted && !isCurrent) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Completed",
                                tint = contentColor,
                                modifier = Modifier.size(14.dp)
                            )
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                color = contentColor,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = stage.shortLabel,
                        color = if (isCurrent) StudioCyan else if (isCompleted) StudioTextPrimary else StudioTextSecondary,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Medium
                    )

                    if (index < workflowStages.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Box(
                            modifier = Modifier
                                .width(12.dp)
                                .height(2.dp)
                                .background(if (isCompleted) StudioEmerald.copy(alpha = 0.5f) else Color(0xFF263248))
                        )
                    }
                }
            }
        }
    }
}
