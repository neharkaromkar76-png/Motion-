package com.example.engine.motion

import com.example.data.model.Keyframe
import com.example.data.model.MotionType
import com.example.data.model.SubjectRegion
import com.example.data.model.TimingMappingMode
import com.example.data.model.VideoMetadata
import java.util.UUID

object MotionTransferEngine {

    fun transferMotion(
        referenceKeyframes: List<Keyframe>,
        referenceMetadata: VideoMetadata,
        targetMetadata: VideoMetadata,
        targetSubject: SubjectRegion,
        timingMode: TimingMappingMode = TimingMappingMode.NORMALIZE,
        motionIntensity: Float = 1.0f
    ): List<Keyframe> {
        if (referenceKeyframes.isEmpty()) {
            return listOf(
                Keyframe(UUID.randomUUID().toString(), 0L, targetSubject.centerX, targetSubject.centerY, 1.0f),
                Keyframe(UUID.randomUUID().toString(), targetMetadata.durationMs, targetSubject.centerX, targetSubject.centerY, 1.0f)
            )
        }

        val refDuration = maxOf(1000L, referenceMetadata.durationMs)
        val targetDuration = maxOf(1000L, targetMetadata.durationMs)
        val sortedRef = referenceKeyframes.sortedBy { it.timestampMs }

        // Initial base reference values
        val baseRefX = sortedRef.first().x
        val baseRefY = sortedRef.first().y
        val baseRefScale = maxOf(0.5f, sortedRef.first().scale)

        val targetKeyframes = mutableListOf<Keyframe>()

        for (refKf in sortedRef) {
            // Map timestamp based on chosen timing strategy
            val mappedTimestampMs: Long = when (timingMode) {
                TimingMappingMode.NORMALIZE -> {
                    // Normalize (0% to 100% time mapped to target)
                    val normalizedProgress = refKf.timestampMs.toFloat() / refDuration.toFloat()
                    (normalizedProgress * targetDuration).toLong().coerceIn(0L, targetDuration)
                }
                TimingMappingMode.STRETCH -> {
                    val ratio = targetDuration.toFloat() / refDuration.toFloat()
                    (refKf.timestampMs * ratio).toLong().coerceIn(0L, targetDuration)
                }
                TimingMappingMode.COMPRESS -> {
                    refKf.timestampMs.coerceIn(0L, targetDuration)
                }
                TimingMappingMode.SCENE_BASED -> {
                    val normalizedProgress = refKf.timestampMs.toFloat() / refDuration.toFloat()
                    (normalizedProgress * targetDuration).toLong().coerceIn(0L, targetDuration)
                }
            }

            // Calculate RELATIVE motion vectors from reference
            val deltaX = (refKf.x - baseRefX) * motionIntensity
            val deltaY = (refKf.y - baseRefY) * motionIntensity

            // Relative scale multiplier
            val relativeScaleMultiplier = refKf.scale / baseRefScale
            val targetScale = (1.0f + (relativeScaleMultiplier - 1.0f) * motionIntensity).coerceIn(1.0f, 2.8f)

            // Calculate target position centered around target subject
            val targetBaseX = targetSubject.centerX
            val targetBaseY = targetSubject.centerY

            var newX = targetBaseX + deltaX
            var newY = targetBaseY + deltaY

            // Subject-aware boundary safety clamping:
            // Ensure camera frame does not reveal black borders outside [0..1]
            // Safe margin based on current zoom level
            val maxAllowedShift = (1f - (1f / targetScale)) / 2f
            val safeXMin = 0.5f - maxAllowedShift
            val safeXMax = 0.5f + maxAllowedShift
            val safeYMin = 0.5f - maxAllowedShift
            val safeYMax = 0.5f + maxAllowedShift

            newX = newX.coerceIn(minOf(safeXMin, safeXMax), maxOf(safeXMin, safeXMax))
            newY = newY.coerceIn(minOf(safeYMin, safeYMax), maxOf(safeYMin, safeYMax))

            // Relative rotation scaled by intensity
            val newRotation = (refKf.rotation * motionIntensity).coerceIn(-35f, 35f)

            targetKeyframes.add(
                Keyframe(
                    id = UUID.randomUUID().toString(),
                    timestampMs = mappedTimestampMs,
                    x = newX,
                    y = newY,
                    scale = targetScale,
                    rotation = newRotation,
                    easing = refKf.easing,
                    motionType = refKf.motionType,
                    confidence = refKf.confidence
                )
            )
        }

        // Ensure keyframe at timestamp 0 and targetDuration exist
        val sortedTargets = targetKeyframes.sortedBy { it.timestampMs }.toMutableList()

        if (sortedTargets.none { it.timestampMs == 0L }) {
            val first = sortedTargets.first()
            sortedTargets.add(0, first.copy(id = UUID.randomUUID().toString(), timestampMs = 0L))
        }
        if (sortedTargets.none { it.timestampMs == targetDuration }) {
            val last = sortedTargets.last()
            sortedTargets.add(last.copy(id = UUID.randomUUID().toString(), timestampMs = targetDuration))
        }

        return sortedTargets.distinctBy { it.timestampMs }.sortedBy { it.timestampMs }
    }
}
