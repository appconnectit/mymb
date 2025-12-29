package com.appconnectit.mymb.policies

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun TermsAndConditionsScreen() {
    Text(
        text = "These are the terms and conditions. Please read them carefully.",
        modifier = Modifier.padding(16.dp)
    )
}