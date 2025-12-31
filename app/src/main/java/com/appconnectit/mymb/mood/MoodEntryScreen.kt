package com.appconnectit.mymb.mood

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun MoodEntryScreen(onNext: (Float) -> Unit) {
    val ripples = remember { List(7) { Animatable(0f) } }
    var sliderPosition by remember { mutableStateOf(3f) }

    // Define more distinct pastel colors
    val neutralColorStart = Color(0xFFB2DFDB) // Lighter Teal
    val neutralColorEnd = Color(0xFF4DB6AC)   // Teal
    val unpleasantColorStart = Color(0xFFD1C4E9) // Lighter Purple
    val unpleasantColorEnd = Color(0xFF7E57C2)   // Deep Purple
    val pleasantColorStart = Color(0xFFFFF59D) // Lighter Yellow
    val pleasantColorEnd = Color(0xFFFFE0B2)   // Lighter Peach

    val startColor by animateColorAsState(
        targetValue = when {
            sliderPosition < 3f -> lerpColor(neutralColorStart, unpleasantColorStart, (3f - sliderPosition) / 3f)
            else -> lerpColor(neutralColorStart, pleasantColorStart, (sliderPosition - 3f) / 3f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "startColorAnimation"
    )

    val endColor by animateColorAsState(
        targetValue = when {
            sliderPosition < 3f -> lerpColor(neutralColorEnd, unpleasantColorEnd, (3f - sliderPosition) / 3f)
            else -> lerpColor(neutralColorEnd, pleasantColorEnd, (sliderPosition - 3f) / 3f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "endColorAnimation"
    )

    val rippleBaseColor by animateColorAsState(
        targetValue = when {
            sliderPosition < 3f -> lerpColor(Color(0xFF00695C), Color(0xFF311B92), (3f - sliderPosition) / 3f) // Darker Teal to Deeper Purple
            else -> lerpColor(Color(0xFF00695C), Color(0xFFE65100), (sliderPosition - 3f) / 3f) // Darker Teal to Darker Orange
        },
        animationSpec = tween(durationMillis = 500),
        label = "rippleColorAnimation"
    )

    val rippleColors = remember(rippleBaseColor) {
        List(7) { i -> rippleBaseColor.copy(alpha = 0.8f - (i * 0.1f)) }
    }

    val phaseAnim = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        launch {
            phaseAnim.animateTo(
                targetValue = (2 * PI).toFloat(),
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 6000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                )
            )
        }
        ripples.forEachIndexed { index, animatable ->
            launch {
                delay(index * 600L)
                animatable.animateTo(
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 4200, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    )
                )
            }
        }
    }

    val moodLabels = listOf(
        "Very Unpleasant",
        "Unpleasant",
        "Slightly Unpleasant",
        "Neutral",
        "Slightly Pleasant",
        "Pleasant",
        "Very Pleasant"
    )

    val distortion = remember(sliderPosition) {
        ((3f - sliderPosition) / 3f).coerceIn(0f, 1f)
    }
    val pleasantness = remember(sliderPosition) {
        ((sliderPosition - 3f) / 3f).coerceIn(0f, 1f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(startColor, endColor)))
            .padding(16.dp)
    ) {
        Text(
            text = "How are you feeling?",
            style = MaterialTheme.typography.headlineMedium.copy(color = Color.Black.copy(alpha = 0.7f)),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 32.dp)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .align(Alignment.Center)
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val centerRadius = pleasantness * 75.dp.toPx() // Increased pleasantness effect
                if (centerRadius > 0) {
                    drawCircle(
                        color = pleasantColorStart.copy(alpha = (pleasantness * 0.9f).coerceIn(0f, 1f)), // Increased opacity
                        radius = centerRadius
                    )
                }

                ripples.forEachIndexed { index, anim ->
                    val fraction = anim.value
                    if (fraction > 0f) {
                        val alpha = (1f - fraction).coerceIn(0f, 1f)
                        val strokeWidth = (3.dp.toPx() * (1 - fraction)).coerceAtLeast(0f)
                        val baseRadius = size.minDimension / 2 * fraction

                        if (distortion > 0) {
                            val path = Path()
                            val numPoints = 120
                            val angleStep = (2 * PI / numPoints).toFloat()
                            val waveFrequency = 7f
                            val waveAmplitude = baseRadius * 0.05f * distortion

                            for (i in 0..numPoints) {
                                val angle = i * angleStep
                                val currentRadius = baseRadius + waveAmplitude * sin(angle * waveFrequency + phaseAnim.value)
                                val x = center.x + currentRadius * cos(angle)
                                val y = center.y + currentRadius * sin(angle)

                                if (i == 0) {
                                    path.moveTo(x, y)
                                } else {
                                    path.lineTo(x, y)
                                }
                            }
                            path.close()
                            drawPath(
                                path = path,
                                color = rippleColors[index].copy(alpha = alpha),
                                style = Stroke(width = strokeWidth)
                            )
                        } else {
                            drawCircle(
                                color = rippleColors[index].copy(alpha = alpha),
                                radius = baseRadius,
                                center = center,
                                style = Stroke(width = strokeWidth)
                            )
                        }
                    }
                }
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = moodLabels[sliderPosition.roundToInt()],
                style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black.copy(alpha = 0.7f))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                steps = 5,
                valueRange = 0f..6f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { onNext(sliderPosition) }) {
                Text("Next")
            }
        }
    }
}

fun lerpColor(start: Color, end: Color, fraction: Float): Color {
    val red = start.red + (end.red - start.red) * fraction
    val green = start.green + (end.green - start.green) * fraction
    val blue = start.blue + (end.blue - start.blue) * fraction
    return Color(red, green, blue)
}
