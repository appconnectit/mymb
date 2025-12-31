package com.appconnectit.mymb.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appconnectit.mymb.ui.theme.MyMBTheme

@Composable
fun ConfirmationScreen(email: String, onGoToLogin: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "A verification link has been sent to your email address at $email. Please verify your email to login.",
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        Button(onClick = onGoToLogin) {
            Text("Go to Login")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ConfirmationScreenPreview() {
    MyMBTheme {
        ConfirmationScreen(email = "test@example.com", onGoToLogin = {})
    }
}