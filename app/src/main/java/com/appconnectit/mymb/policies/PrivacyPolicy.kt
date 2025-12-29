package com.appconnectit.mymb.policies

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PrivacyPolicyScreen() {
    Text(
        text = "This is the privacy policy. Your data is safe with us.",
        modifier = Modifier.padding(16.dp)
    )
}