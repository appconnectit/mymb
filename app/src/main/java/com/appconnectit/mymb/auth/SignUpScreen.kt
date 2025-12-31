package com.appconnectit.mymb.auth

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appconnectit.mymb.ui.theme.MyMBTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

@Composable
fun SignUpScreen(
    onSignUpSuccess: (String) -> Unit,
    onGoToTerms: () -> Unit,
    onGoToPrivacy: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var tob by remember { mutableStateOf("") }
    var cob by remember { mutableStateOf("") }
    var therapistEmail by remember { mutableStateOf("") }
    var agreedToTerms by remember { mutableStateOf(false) }

    var emailDirty by remember { mutableStateOf(false) }
    var passwordDirty by remember { mutableStateOf(false) }
    var confirmPasswordDirty by remember { mutableStateOf(false) }
    var dobDirty by remember { mutableStateOf(false) }
    var tobDirty by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var dobError by remember { mutableStateOf<String?>(null) }
    var tobError by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun validateEmail() {
        emailError = if (email.isNotBlank() && !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            "Invalid email address"
        } else {
            null
        }
    }

    fun validatePassword() {
        val errors = mutableListOf<String>()
        if (password.length < 8) errors.add("at least 8 characters")
        if (!password.any { it.isLowerCase() }) errors.add("a lowercase character")
        if (!password.any { it.isUpperCase() }) errors.add("an uppercase character")
        if (!password.any { !it.isLetterOrDigit() }) errors.add("a non-alphanumeric character")

        passwordError = if (errors.isNotEmpty()) {
            "Password must contain " + errors.joinToString(", ")
        } else {
            null
        }
    }

    fun validateConfirmPassword() {
        confirmPasswordError = if (password != confirmPassword) {
            "Passwords do not match"
        } else {
            null
        }
    }

    fun validateDob() {
        dobError = if (dob.isBlank()) "Please enter your date of birth" else null
    }

    fun validateTob() {
        tobError = if (tob.isBlank()) "Please enter your time of birth" else null
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
    )

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
        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") })

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; emailDirty = true; validateEmail() },
            label = { Text("Email") },
            isError = emailError != null,
            supportingText = { emailError?.let { Text(it, color = Color.Red) } },
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused && emailDirty) {
                    validateEmail()
                }
            }
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; passwordDirty = true; validatePassword() },
            label = { Text("Password") },
            isError = passwordError != null,
            supportingText = { passwordError?.let { Text(it, color = Color.Red) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused && passwordDirty) {
                    validatePassword()
                }
            }
        )

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; confirmPasswordDirty = true; validateConfirmPassword() },
            label = { Text("Confirm Password") },
            isError = confirmPasswordError != null,
            supportingText = { confirmPasswordError?.let { Text(it, color = Color.Red) } },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.onFocusChanged {
                if (!it.isFocused && confirmPasswordDirty) {
                    validateConfirmPassword()
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
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGenderExpanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(expanded = isGenderExpanded, onDismissRequest = { isGenderExpanded = false }) {
                genderOptions.forEach { option ->
                    DropdownMenuItem(text = { Text(option) }, onClick = {
                        gender = option
                        isGenderExpanded = false
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

        OutlinedTextField(value = cob, onValueChange = { cob = it }, label = { Text("City of Birth") })
        OutlinedTextField(value = therapistEmail, onValueChange = { therapistEmail = it }, label = { Text("Therapist's Email") })

        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = agreedToTerms, onCheckedChange = { agreedToTerms = it })
            Text("I agree to the ")
            TextButton(onClick = onGoToTerms) {
                Text("Terms and Conditions")
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("and the ")
            TextButton(onClick = onGoToPrivacy) {
                Text("Privacy Policy")
            }
        }

        Button(
            onClick = {
                validateEmail()
                validatePassword()
                validateConfirmPassword()
                validateDob()
                validateTob()

                if (emailError == null && passwordError == null && confirmPasswordError == null && dobError == null && tobError == null && agreedToTerms) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val user = auth.currentUser
                                user?.sendEmailVerification()?.addOnCompleteListener { verificationTask ->
                                    if (verificationTask.isSuccessful) {
                                        val userData = hashMapOf(
                                            "name" to name,
                                            "gender" to gender,
                                            "dob" to dob,
                                            "tob" to tob,
                                            "cob" to cob,
                                            "therapistEmail" to therapistEmail
                                        )
                                        db.collection("users").document(user.uid)
                                            .set(userData)
                                            .addOnSuccessListener { onSignUpSuccess(email) }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(context, "Failed to save user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                            }
                                    } else {
                                        Toast.makeText(context, "Failed to send verification email: ${verificationTask.exception?.message}", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            } else {
                                Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else if (!agreedToTerms) {
                    Toast.makeText(context, "You must agree to the terms and conditions", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Sign Up")
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
fun SignUpScreenPreview() {
    MyMBTheme {
        SignUpScreen(
            onSignUpSuccess = {},
            onGoToTerms = {},
            onGoToPrivacy = {}
        )
    }
}
