package com.appconnectit.mymb.visualization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.GridLines
import co.yml.charts.ui.linechart.model.IntersectionPoint
import co.yml.charts.ui.linechart.model.Line
import co.yml.charts.ui.linechart.model.LineChartData
import co.yml.charts.ui.linechart.model.LinePlotData
import co.yml.charts.ui.linechart.model.LineStyle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MoodEntry(
    val id: String,
    val mood: Float,
    val feelings: List<String>,
    val notes: String,
    val timestamp: Date
)

@Composable
fun MoodVisualizationScreen() {
    var moodEntries by remember { mutableStateOf<List<MoodEntry>>(emptyList()) }
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser

    LaunchedEffect(user) {
        if (user != null) {
            db.collection("moodEntries")
                .whereEqualTo("userId", user.uid)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .addOnSuccessListener { documents ->
                    val entries = documents.map { document ->
                        MoodEntry(
                            id = document.id,
                            mood = (document.get("mood") as? Double)?.toFloat() ?: 0f,
                            feelings = document.get("feelings") as? List<String> ?: emptyList(),
                            notes = document.getString("notes") ?: "",
                            timestamp = (document.get("timestamp") as? Timestamp)?.toDate() ?: Date()
                        )
                    }.reversed()
                    moodEntries = entries
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Mood Visualization")
        if (moodEntries.isNotEmpty()) {
            val points = moodEntries.mapIndexed { index, entry -> Point(index.toFloat(), entry.mood) }
            val sdf = remember { SimpleDateFormat("MMM d", Locale.getDefault()) }
            val xAxisData = AxisData.Builder()
                .axisStepSize(100.dp)
                .backgroundColor(Color.Transparent)
                .steps(points.size - 1)
                .labelData { i ->
                    if (i < moodEntries.size) {
                        sdf.format(moodEntries[i].timestamp)
                    } else ""
                }
                .build()

            val yAxisData = AxisData.Builder()
                .steps(5)
                .backgroundColor(Color.Transparent)
                .labelData { i -> (i * (10 / 5f)).toString() }
                .build()

            val lineChartData = LineChartData(
                linePlotData = LinePlotData(
                    lines = listOf(
                        Line(
                            dataPoints = points,
                            lineStyle = LineStyle(),
                            intersectionPoint = IntersectionPoint()
                        )
                    )
                ),
                xAxisData = xAxisData,
                yAxisData = yAxisData,
                gridLines = GridLines(),
                backgroundColor = Color.White
            )

            LineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                lineChartData = lineChartData
            )
        }
    }
}
