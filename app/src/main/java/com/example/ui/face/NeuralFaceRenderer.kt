package com.example.ui.face

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import com.example.service.AegisState
import com.example.service.UserEmotion
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-fidelity futuristic Canvas drawing a multi-layered, interactive neural core (Aegis's face).
 * Emulates a responsive, professional 3D human-AI representation that dances to audio and emotions.
 */
@Composable
fun NeuralFaceRenderer(
    aegisState: AegisState,
    detectedEmotion: UserEmotion,
    userVoiceVolume: Float,
    robotTalkVolume: Float,
    modifier: Modifier = Modifier
) {
    // Continuous rotation parameters for physical 3D ring illusions
    val infiniteTransition = rememberInfiniteTransition(label = "core_rotation")
    
    val ringRotation1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteSpec(12000)
    )
    val ringRotation2 by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteSpec(8000)
    )
    val breathingPulse by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    // Fluid state transitions based on volume levels and intelligence state
    val compositeActivityVolume = (userVoiceVolume * 0.7f + robotTalkVolume * 1.3f).coerceIn(0f, 30f)
    val dynamicStateScale by animateFloatAsState(
        targetValue = 1f + (compositeActivityVolume / 22f),
        animationSpec = spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessLow)
    )

    // Emotional Palette Assignment mapping to user feedback
    val emotionalColors = remember(detectedEmotion, aegisState) {
        when {
            aegisState == AegisState.THINKING -> listOf(
                Color(0xFF8E24AA), // Electric Purple
                Color(0xFFBA68C8), // Violet
                Color(0xFF3F51B5)  // Deep Indigo Code
            )
            aegisState == AegisState.ERROR -> listOf(
                Color(0xFFE53935), // Emergency Red
                Color(0xFFFF8A80), // Alert Coral
                Color(0xFF263238)  // Obsidian Alert
            )
            detectedEmotion == UserEmotion.STRESSED -> listOf(
                Color(0xFF00E676), // Healing Green (to soothe the user)
                Color(0xFF69F0AE), // Calm Sage
                Color(0xFF1DE9B6)  // Turquoise
            )
            detectedEmotion == UserEmotion.ANALYTICAL -> listOf(
                Color(0xFFFFB300), // Precision Gold
                Color(0xFFFFD54F), // High Amber
                Color(0xFF00E5FF)  // Electric Laser Cyan
            )
            detectedEmotion == UserEmotion.EXCITED -> listOf(
                Color(0xFFFF4081), // Energetic Pink
                Color(0xFFFFE082), // Golden Glow
                Color(0xFF00E5FF)  // Synthetic Cyan
            )
            detectedEmotion == UserEmotion.CONFUSED -> listOf(
                Color(0xFF0288D1), // Sky Blue
                Color(0xFFB2DFDB), // Pale Teal
                Color(0xFF5E35B1)  // Whimsical Violet
            )
            else -> listOf(
                Color(0xFF00B0FF), // Pure Cyan
                Color(0xFF00E5FF), // Neo Teal
                Color(0xFF1A237E)  // Dark Space Obsidian
            )
        }
    }

    val primaryColor = emotionalColors[0]
    val secondaryColor = emotionalColors[1]
    val accentColor = emotionalColors[2]

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp),
        contentAlignment = androidx.compose.ui.Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val baseRadius = size.minDimension / 4.2f
            val reactiveRadius = baseRadius * breathingPulse * dynamicStateScale

            // Draw Background Soft Ambient Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.28f), Color.Transparent),
                    center = center,
                    radius = reactiveRadius * 2.8f
                ),
                radius = reactiveRadius * 2.8f,
                center = center
            )

            // Draw Layer 3: Cyber Dot Grids (Simulating Neural Nodes)
            val numParticles = 24
            for (i in 0 until numParticles) {
                val angle = (i * (360f / numParticles)) + ringRotation1 * 0.3f
                val radian = Math.toRadians(angle.toDouble())
                val distance = reactiveRadius * 1.45f + sin(radian * 4f + ringRotation2 * 0.1f) * 12f
                val px = center.x + (cos(radian) * distance).toFloat()
                val py = center.y + (sin(radian) * distance).toFloat()
                
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.65f),
                    radius = 3.2f,
                    center = Offset(px, py)
                )
            }

            // Draw Layer 2: Rotating Cybernetic Orbital Rings (Outer Grid)
            withTransform({
                rotate(ringRotation1, center)
            }) {
                // Drawing 8 Segmented Tech Arcs
                for (j in 0..7) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(primaryColor, accentColor.copy(alpha = 0.1f), primaryColor)),
                        startAngle = j * 45f + 5f,
                        sweepAngle = 35f,
                        useCenter = false,
                        topLeft = Offset(center.x - reactiveRadius * 1.25f, center.y - reactiveRadius * 1.25f),
                        size = androidx.compose.ui.geometry.Size(reactiveRadius * 2.5f, reactiveRadius * 2.5f),
                        style = Stroke(width = 4.5f, cap = StrokeCap.Round)
                    )
                }
            }

            withTransform({
                rotate(ringRotation2, center)
            }) {
                // Layer 1: Inner Segmented Arcs
                for (k in 0..3) {
                    drawArc(
                        color = secondaryColor.copy(alpha = 0.5f),
                        startAngle = k * 90f + 15f,
                        sweepAngle = 60f,
                        useCenter = false,
                        topLeft = Offset(center.x - reactiveRadius * 0.95f, center.y - reactiveRadius * 0.95f),
                        size = androidx.compose.ui.geometry.Size(reactiveRadius * 1.9f, reactiveRadius * 1.9f),
                        style = Stroke(width = 2.5f)
                    )
                }
            }

            // Draw Core: Morphing 3D-inspired Sine Wave Waves (Visualizing Vocal Activity)
            val path = Path()
            val points = 72
            val waveFreq = if (aegisState == AegisState.THINKING) 6.0 else 3.5
            val currentAmps = 14f + compositeActivityVolume * 2.2f

            for (p in 0..points) {
                val angleRad = (p * (2 * Math.PI / points))
                val waveOffset = sin(angleRad * waveFreq + Math.toRadians(ringRotation1.toDouble() * 1.5)) * currentAmps
                val r = reactiveRadius * 0.7f + waveOffset.toFloat()
                
                val rx = center.x + (cos(angleRad) * r).toFloat()
                val ry = center.y + (sin(angleRad) * r).toFloat()
                
                if (p == 0) {
                    path.moveTo(rx, ry)
                } else {
                    path.lineTo(rx, ry)
                }
            }
            path.close()

            // Draw Core Volume fill (Liquid AI face)
            drawPath(
                path = path,
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor, secondaryColor, accentColor.copy(alpha = 0.2f)),
                    center = center,
                    radius = reactiveRadius * 0.92f
                )
            )

            // Dynamic Core Rim Stroke
            drawPath(
                path = path,
                color = Color.White.copy(alpha = 0.85f),
                style = Stroke(width = 3.5f)
            )

            // Glowing Concentric Center Dot
            drawCircle(
                color = Color.White,
                radius = 8.dp.toPx() * breathingPulse,
                center = center
            )
        }
    }
}

private fun infiniteSpec(duration: Int): InfiniteRepeatableSpec<Float> {
    return infiniteRepeatable(
        animation = tween(duration, easing = LinearEasing),
        repeatMode = RepeatMode.Restart
    )
}
