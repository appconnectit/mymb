package com.appconnectit.mymb.profile

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appconnectit.mymb.ui.theme.MyMBTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun EditProfileScreen(onCancel: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val user = auth.currentUser
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var tob by remember { mutableStateOf("") }
    var cob by remember { mutableStateOf("") }
    var therapistEmail by remember { mutableStateOf("") }

    var nameDirty by remember { mutableStateOf(false) }
    var genderDirty by remember { mutableStateOf(false) }
    var dobDirty by remember { mutableStateOf(false) }
    var tobDirty by remember { mutableStateOf(false) }
    var cobDirty by remember { mutableStateOf(false) }
    var therapistEmailDirty by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var genderError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var tobError by remember { mutableStateOf<String?>(null) }
    var cobError by remember { mutableStateOf<String?>(null) }
    var therapistEmailError by remember { mutableStateOf<String?>(null) }

    val minDobCalendar = Calendar.getInstance().apply {
        set(1900, 0, 1)
    }

    fun validateName() {
        nameError = if (name.isBlank()) "Name cannot be empty" else null
    }

    fun validateGender() {
        genderError = if (gender.isBlank()) "Please select a gender" else null
    }

    fun validateDob() {
        if (dob.isBlank()) {
            dobError = "Please enter your date of birth"
            return
        }
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val date = sdf.parse(dob)
            if (date != null && date.before(minDobCalendar.time)) {
                dobError = "Date of birth cannot be before 01/01/1900"
            } else {
                dobError = null
            }
        } catch (e: Exception) {
            dobError = "Invalid date format"
        }
    }

    fun validateTob() {
        tobError = if (tob.isBlank()) "Please enter your time of birth" else null
    }

    fun validateCob() {
        cobError = if (cob.isBlank()) "City of birth cannot be empty" else null
    }

    fun validateTherapistEmail() {
        therapistEmailError = if (therapistEmail.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(therapistEmail).matches()) {
            "Invalid email address"
        } else {
            null
        }
    }

    LaunchedEffect(user) {
        if (user != null) {
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        name = document.getString("name") ?: ""
                        gender = document.getString("gender") ?: ""
                        dob = document.getString("dob") ?: ""
                        tob = document.getString("tob") ?: ""
                        cob = document.getString("cob") ?: ""
                        therapistEmail = document.getString("therapistEmail") ?: ""
                    }
                }
        }
    }

    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog = DatePickerDialog(
        context,
        { _, selectedYear, selectedMonth, selectedDay ->
            dob = "$selectedDay/${selectedMonth + 1}/$selectedYear"
            dobDirty = true
            validateDob()
        },
        year, month, day
    ).apply {
        datePicker.minDate = minDobCalendar.timeInMillis
    }

    val timePickerDialog = TimePickerDialog(
        context,
        { _, hour, minute ->
            tob = "$hour:$minute"
            tobDirty = true
            validateTob()
        },
        calendar.get(Calendar.HOUR_OF_DAY),
        calendar.get(Calendar.MINUTE),
        false
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it; nameDirty = true; validateName() },
            label = { Text("Name") },
            isError = nameError != null,
            supportingText = { nameError?.let { Text(it, color = Color.Red) } },
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused && nameDirty) {
                    validateName()
                }
            }
        )

        var isGenderExpanded by remember { mutableStateOf(false) }
        val genderOptions = listOf("Male", "Female", "Non-Binary", "Prefer not to say")

        @OptIn(ExperimentalMaterial3Api::class)
        ExposedDropdownMenuBox(expanded = isGenderExpanded, onExpandedChange = { isGenderExpanded = !isGenderExpanded }) {
            OutlinedTextField(
                value = gender,
                onValueChange = {},
                readOnly = true,
                label = { Text("Gender") },
                isError = genderError != null,
                supportingText = { genderError?.let { Text(it, color = Color.Red) } },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = isGenderExpanded, onDismissRequest = { isGenderExpanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        gender = option
                        isGenderExpanded = false
                        genderDirty = true
                        validateGender()
                    })
                }
            }
        }

        OutlinedTextField(
            value = dob,
            onValueChange = { dob = it; dobDirty = true; validateDob() },
            label = { Text("Date of Birth") },
            isError = dobError != null,
            supportingText = { dobError?.let { Text(it, color = Color.Red) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = DateVisualTransformation(),
            modifier = Modifier
                .onFocusChanged {
                    if (!it.isFocused && dobDirty) {
                        validateDob()
                    }
                }
                .clickable { datePickerDialog.show() }
        )

        OutlinedTextField(
            value = tob,
            onValueChange = { tob = it; tobDirty = true; validateTob() },
            label = { Text("Time of Birth") },
            isError = tobError != null,
            supportingText = { tobError?.let { Text(it, color = Color.Red) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = TimeVisualTransformation(),
            modifier = Modifier
                .onFocusChanged {
                    if (!it.isFocused && tobDirty) {
                        validateTob()
                    }
                }
                .clickable { timePickerDialog.show() }
        )

        OutlinedTextField(
            value = cob,
            onValueChange = { cob = it; cobDirty = true; validateCob() },
            label = { Text("City of Birth") },
            isError = cobError != null,
            supportingText = { cobError?.let { Text(it, color = Color.Red) } },
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused && cobDirty) {
                    validateCob()
                }
            }
        )

        OutlinedTextField(
            value = therapistEmail,
            onValueChange = { therapistEmail = it; therapistEmailDirty = true; validateTherapistEmail() },
            label = { Text("Therapist's Email") },
            isError = therapistEmailError != null,
            supportingText = { therapistEmailError?.let { Text(it, color = Color.Red) } },
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused && therapistEmailDirty) {
                    validateTherapistEmail()
                }
            }
        )

        Row {
            Button(
                onClick = onCancel,
                modifier = Modifier.padding(end = 8.dp)
            ) {
                Text("Cancel")
            }
            Button(
                onClick = {
                    validateName()
                    validateGender()
                    validateDob()
                    validateTob()
                    validateCob()
                    validateTherapistEmail()

                    if (nameError == null && genderError == null && dobError == null && tobError == null && cobError == null && therapistEmailError == null) {
                        if (user != null) {
                            val updatedUserData = hashMapOf(
                                "name" to name,
                                "gender" to gender,
                                "dob" to dob,
                                "tob" to tob,
                                "cob" to cob,
                                "therapistEmail" to therapistEmail
                            )
                            db.collection("users").document(user.uid)
                                .set(updatedUserData)
                                .addOnSuccessListener {
                                    Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    onCancel()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    }
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text("Save")
            }
        }
    }
}

class DateVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = if (text.text.length >= 8) text.text.substring(0..7) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i % 2 == 1 && i < 4) out += "/"
        }
        val numberOffsetTranslator = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 3) return offset + 1
                if (offset <= 8) return offset + 2
                return 10
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                if (offset <= 10) return offset - 2
                return 8
            }
        }
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), numberOffsetTranslator)
    }
}

class TimeVisualTransformation : VisualTransformation {
    override fun filter(text: androidx.compose.ui.text.AnnotatedString): androidx.compose.ui.text.input.TransformedText {
        val trimmed = if (text.text.length >= 4) text.text.substring(0..3) else text.text
        var out = ""
        for (i in trimmed.indices) {
            out += trimmed[i]
            if (i == 1) out += ":"
        }
        val numberOffsetTranslator = object : androidx.compose.ui.text.input.OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 1) return offset
                if (offset <= 4) return offset + 1
                return 5
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 2) return offset
                if (offset <= 5) return offset - 1
                return 4
            }
        }
        return androidx.compose.ui.text.input.TransformedText(androidx.compose.ui.text.AnnotatedString(out), numberOffsetTranslator)
    }
}


@Preview(showBackground = true)
@Composable
fun EditProfileScreenPreview() {
    MyMBTheme {
        EditProfileScreen(onCancel = {})
    }
}
