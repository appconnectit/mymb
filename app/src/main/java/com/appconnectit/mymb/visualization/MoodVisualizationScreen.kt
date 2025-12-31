package com.appconnectit.mymb.visualization

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.Timestamp
import java.util.Date

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
            db.collection("mood_entries")
                .whereEqualTo("userId", user.uid)
                .orderBy("timestamp", Query.Direction.ASCENDING)
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
                    }
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
            LazyColumn {
                items(moodEntries) { entry ->
                    Text("Mood: ${entry.mood} at ${entry.timestamp}")
                }
            }
        } else {
            Text("No mood entries yet.")
        }
    }
}
