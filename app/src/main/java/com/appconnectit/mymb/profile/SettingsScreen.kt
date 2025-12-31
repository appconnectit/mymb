package com.appconnectit.mymb.profile

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.appconnectit.mymb.reminders.ReminderReceiver
import com.appconnectit.mymb.ui.theme.MyMBTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

data class Reminder(val id: String, val time: String)

@Composable
fun SettingsScreen() {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current
    var reminders by remember { mutableStateOf(listOf<Reminder>()) }

    fun fetchReminders() {
        if (user != null) {
            db.collection("reminders").whereEqualTo("userId", user.uid).get()
                .addOnSuccessListener { documents ->
                    val reminderList = mutableListOf<Reminder>()
                    for (document in documents) {
                        reminderList.add(Reminder(document.id, document.getString("time") ?: ""))
                    }
                    reminders = reminderList
                }
        }
    }

    LaunchedEffect(user) {
        fetchReminders()
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission Accepted
        } else {
            // Permission Denied
        }
    }

    fun scheduleReminder(hour: Int, minute: Int) {
        val intent = Intent(context, ReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    val calendar = Calendar.getInstance()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                val newReminder = hashMapOf(
                    "userId" to user?.uid,
                    "time" to "$hour:$minute"
                )
                db.collection("reminders").add(newReminder).addOnSuccessListener { fetchReminders() }
                scheduleReminder(hour, minute)
            } else {
                launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            auth.sendPasswordResetEmail(auth.currentUser?.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Password reset email sent", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Failed to send password reset email", Toast.LENGTH_SHORT).show()
                    }
                }
        }) {
            Text("Reset Password")
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Reminders")
            IconButton(onClick = { timePickerDialog.show() }) {
                Icon(Icons.Default.Add, contentDescription = "Add Reminder")
            }
        }

        LazyColumn {
            items(reminders) { reminder ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(reminder.time)
                    Row {
                        IconButton(onClick = {
                            val editTimePickerDialog = TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    db.collection("reminders").document(reminder.id)
                                        .update("time", "$hour:$minute")
                                        .addOnSuccessListener { fetchReminders() }
                                },
                                calendar.get(Calendar.HOUR_OF_DAY),
                                calendar.get(Calendar.MINUTE),
                                false
                            )
                            editTimePickerDialog.show()
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit Reminder")
                        }
                        IconButton(onClick = {
                            db.collection("reminders").document(reminder.id).delete().addOnSuccessListener { fetchReminders() }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Reminder")
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {
    MyMBTheme {
        SettingsScreen()
    }
}
