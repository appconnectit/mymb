package com.appconnectit.mymb.mood

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun MoodEntryScreen() {
    val ripples = remember { List(7) { Animatable(0f) } }
    val rippleColors = remember {
        listOf(
            Color(0xFFBBDEFB),
            Color(0xFF90CAF9),
            Color(0xFF64B5F6),
            Color(0xFF42A5F5),
            Color(0xFF64B5F6),
            Color(0xFF90CAF9),
            Color(0xFFBBDEFB)
        )
    }

    LaunchedEffect(Unit) {
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

    var sliderPosition by remember { mutableStateOf(3f) }
    val moodLabels = listOf(
        "Very Unpleasant",
        "Unpleasant",
        "Slightly Unpleasant",
        "Neutral",
        "Slightly Pleasant",
        "Pleasant",
        "Very Pleasant"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround
    ) {
        Text("How are you feeling?")

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)) { 
            Canvas(modifier = Modifier.fillMaxSize()) {
                ripples.forEachIndexed { index, anim ->
                    val fraction = anim.value
                    if (fraction > 0f) {
                        val radius = size.minDimension * fraction
                        val alpha = (1f - fraction).coerceIn(0f, 1f)

                        drawCircle(
                            color = rippleColors[index].copy(alpha = alpha),
                            radius = radius,
                            center = center,
                            style = Stroke(width = (3.dp.toPx() * (1 - fraction)).coerceAtLeast(0f))
                        )
                    }
                }
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Slider(
                value = sliderPosition,
                onValueChange = { sliderPosition = it },
                steps = 5,
                valueRange = 0f..6f,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = moodLabels[sliderPosition.roundToInt()])
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { /* TODO: Handle next button click */ }) {
                Text("Next")
            }
        }
    }
}
