package com.example

import com.example.data.model.EasingType
import com.example.data.model.Keyframe
import com.example.data.model.MotionType
import com.example.data.model.SubjectRegion
import com.example.data.model.TimingMappingMode
import com.example.data.model.VideoMetadata
import com.example.engine.motion.Interpolator
import com.example.engine.motion.MotionTransferEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class KeyframeInterpolationTest {

    @Test
    fun testInterpolator_atExactKeyframes() {
        val kf1 = Keyframe(UUID.randomUUID().toString(), 0L, 0.5f, 0.5f, 1.0f, 0f, easing = EasingType.SMOOTH)
        val kf2 = Keyframe(UUID.randomUUID().toString(), 2000L, 0.6f, 0.4f, 1.5f, 10f, easing = EasingType.SMOOTH)

        val t0 = Interpolator.evaluateKeyframeAtTime(0L, listOf(kf1, kf2))
        assertEquals(0.5f, t0.x, 0.001f)
        assertEquals(1.0f, t0.scale, 0.001f)

        val tEnd = Interpolator.evaluateKeyframeAtTime(2000L, listOf(kf1, kf2))
        assertEquals(0.6f, tEnd.x, 0.001f)
        assertEquals(1.5f, tEnd.scale, 0.001f)
    }

    @Test
    fun testInterpolator_midpointEasing() {
        val kf1 = Keyframe(UUID.randomUUID().toString(), 0L, 0.0f, 0.0f, 1.0f, 0f, easing = EasingType.LINEAR)
        val kf2 = Keyframe(UUID.randomUUID().toString(), 1000L, 1.0f, 1.0f, 2.0f, 0f, easing = EasingType.LINEAR)

        val mid = Interpolator.evaluateKeyframeAtTime(500L, listOf(kf1, kf2))
        assertEquals(0.5f, mid.x, 0.01f)
        assertEquals(1.5f, mid.scale, 0.01f)
    }

    @Test
    fun testMotionTransfer_durationMappingAndClamping() {
        val refMeta = VideoMetadata("uri1", "ref.mp4", 10000L, 1920, 1080)
        val targetMeta = VideoMetadata("uri2", "target.mp4", 20000L, 1080, 1920)
        val subject = SubjectRegion(0.5f, 0.5f, 0.4f, 0.5f, 0.9f, "Subject")

        val refKeyframes = listOf(
            Keyframe(UUID.randomUUID().toString(), 0L, 0.5f, 0.5f, 1.0f),
            Keyframe(UUID.randomUUID().toString(), 5000L, 0.45f, 0.50f, 1.4f, motionType = MotionType.ZOOM_IN),
            Keyframe(UUID.randomUUID().toString(), 10000L, 0.5f, 0.5f, 1.0f)
        )

        val transferred = MotionTransferEngine.transferMotion(
            referenceKeyframes = refKeyframes,
            referenceMetadata = refMeta,
            targetMetadata = targetMeta,
            targetSubject = subject,
            timingMode = TimingMappingMode.NORMALIZE,
            motionIntensity = 1.0f
        )

        assertTrue(transferred.isNotEmpty())
        assertEquals(0L, transferred.first().timestampMs)
        assertEquals(20000L, transferred.last().timestampMs)

        // Middle keyframe should be mapped proportionally to ~10000ms
        val midKf = transferred.find { it.timestampMs in 9500L..10500L }
        assertTrue("Midpoint keyframe mapped to target duration", midKf != null)
        assertTrue("Relative zoom preserved", midKf!!.scale > 1.2f)
    }
}
