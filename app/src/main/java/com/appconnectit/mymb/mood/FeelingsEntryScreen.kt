package com.appconnectit.mymb.mood

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import java.util.Calendar
import java.util.Date
import kotlin.math.roundToInt

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun FeelingsEntryScreen(mood: Float, onCancel: () -> Unit, onSubmit: () -> Unit) {
    var selectedFeelings by remember { mutableStateOf(setOf<String>()) }
    var notes by remember { mutableStateOf("") }
    var feelings by remember { mutableStateOf(listOf<String>()) }
    var userGender by remember { mutableStateOf<String?>(null) }
    var menstrualCycleDay by remember { mutableStateOf(0f) }
    val context = LocalContext.current

    val db = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val user = auth.currentUser

    LaunchedEffect(user) {
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        userGender = document.getString("gender")
                    }
                }
        }
    }

    LaunchedEffect(Unit) {
        Log.d("LunarPhaseDebug", "Today's Lunar Phase: ${getLunarPhase(Date())}")
    }

    fun performSubmit(location: String) {
        if (user != null) {
            val moodEntry = hashMapOf<String, Any>(
                "userId" to user.uid,
                "mood" to mood,
                "feelings" to selectedFeelings.toList(),
                "notes" to notes,
                "timestamp" to FieldValue.serverTimestamp(),
                "lunarPhase" to getLunarPhase(Date()),
                "location" to location
            )
            if (userGender == "Female") {
                moodEntry["menstrualCycle"] = getMenstrualCycleLabel(menstrualCycleDay)
            }
            db.collection("moodEntries").add(moodEntry).addOnSuccessListener { onSubmit() }
        }
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getCurrentLocation(context) { lat, long ->
                    performSubmit("$lat, $long")
                }
            } else {
                performSubmit("Permission Denied")
            }
        }
    )

    // Define pastel colors
    val neutralColorStart = Color(0xFFB2DFDB) // Lighter Teal
    val neutralColorEnd = Color(0xFF4DB6AC)   // Teal
    val unpleasantColorStart = Color(0xFFD1C4E9) // Lighter Purple
    val unpleasantColorEnd = Color(0xFF7E57C2)   // Deep Purple
    val pleasantColorStart = Color(0xFFFFF59D) // Lighter Yellow
    val pleasantColorEnd = Color(0xFFFFE0B2)   // Lighter Peach

    val startColor by animateColorAsState(
        targetValue = when {
            mood < 3f -> lerp(neutralColorStart, unpleasantColorStart, (3f - mood) / 3f)
            else -> lerp(neutralColorStart, pleasantColorStart, (mood - 3f) / 3f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "startColorAnimation"
    )

    val endColor by animateColorAsState(
        targetValue = when {
            mood < 3f -> lerp(neutralColorEnd, unpleasantColorEnd, (3f - mood) / 3f)
            else -> lerp(neutralColorEnd, pleasantColorEnd, (mood - 3f) / 3f)
        },
        animationSpec = tween(durationMillis = 500),
        label = "endColorAnimation"
    )

    LaunchedEffect(mood) {
        db.collection("moodfeelings").document(mood.toInt().toString()).get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    feelings = document.get("feelings") as? List<String> ?: emptyList()
                }
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(startColor, endColor)))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Select the feelings that best describe your mood:")
        Spacer(modifier = Modifier.height(16.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            feelings.forEach { feeling ->
                Surface(
                    modifier = Modifier.padding(4.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (selectedFeelings.contains(feeling)) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                    onClick = {
                        selectedFeelings = if (selectedFeelings.contains(feeling)) {
                            selectedFeelings - feeling
                        } else {
                            selectedFeelings + feeling
                        }
                    }
                ) {
                    Text(text = feeling, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Tell us more about your feelings") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        if (userGender == "Female") {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(getMenstrualCycleLabel(menstrualCycleDay))
                Slider(
                    value = menstrualCycleDay,
                    onValueChange = { menstrualCycleDay = it },
                    steps = 5,
                    valueRange = 0f..6f,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
        Row {
            Button(onClick = onCancel, modifier = Modifier.padding(end = 8.dp)) {
                Text("Cancel")
            }
            Button(onClick = {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getCurrentLocation(context) { lat, long ->
                        performSubmit("$lat, $long")
                    }
                } else {
                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_COARSE_LOCATION)
                }
            }, modifier = Modifier.padding(start = 8.dp)) {
                Text("Submit")
            }
        }
    }
}

fun getMenstrualCycleLabel(value: Float): String {
    return when (value.roundToInt()) {
        0 -> "Approaching Periods"
        1 -> "Day 1"
        2 -> "Day 2"
        3 -> "Day 3"
        4 -> "Day 4"
        5 -> "Day 5"
        6 -> "Extended Periods"
        else -> ""
    }
}

fun getLunarPhase(date: Date): String {
    val cal = Calendar.getInstance()
    cal.time = date
    val year = cal.get(Calendar.YEAR)
    val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

    val phase = (year + dayOfYear / 365.25 - 2000) * 12.3685
    var fraction = phase - phase.toInt()
    if (fraction < 0) {
        fraction += 1.0
    }

    val phaseIndex = (fraction * 8 + 0.5).toInt() and 7

    return when (phaseIndex) {
        0 -> "New Moon"
        1 -> "Waxing Crescent"
        2 -> "First Quarter"
        3 -> "Waxing Gibbous"
        4 -> "Full Moon"
        5 -> "Waning Gibbous"
        6 -> "Last Quarter"
        7 -> "Waning Crescent"
        else -> "New Moon" // Fallback
    }
}

fun getCurrentLocation(context: Context, onLocation: (Double, Double) -> Unit) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    onLocation(location.latitude, location.longitude)
                }
            }
    }
}
