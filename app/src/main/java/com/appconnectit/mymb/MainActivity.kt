package com.appconnectit.mymb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appconnectit.mymb.auth.ConfirmationScreen
import com.appconnectit.mymb.auth.LoginScreen
import com.appconnectit.mymb.auth.SignUpScreen
import com.appconnectit.mymb.mood.MoodEntryScreen
import com.appconnectit.mymb.policies.PrivacyPolicyScreen
import com.appconnectit.mymb.policies.TermsAndConditionsScreen
import com.appconnectit.mymb.profile.EditProfileScreen
import com.appconnectit.mymb.profile.SettingsScreen
import com.appconnectit.mymb.ui.theme.MyMBTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyMBTheme {
                MyMBApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyMBApp() {
    val navController = rememberNavController()
    val auth = FirebaseAuth.getInstance()
    val context = LocalContext.current
    var isAuthenticated by remember { mutableStateOf(auth.currentUser != null && auth.currentUser?.isEmailVerified == true) }

    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            isAuthenticated = user != null && user.isEmailVerified
        }
        auth.addAuthStateListener(listener)
        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    if (isAuthenticated) {
        var showMenu by remember { mutableStateOf(false) }
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = "Home") },
                    actions = {
                        IconButton(onClick = { navController.navigate("mood_entry") }) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                        }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More")
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Edit Profile") },
                                    onClick = { 
                                        navController.navigate("edit_profile")
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Settings") },
                                    onClick = { 
                                        navController.navigate("settings")
                                        showMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Sign Out") },
                                    onClick = { 
                                        auth.signOut()
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            NavHost(navController = navController, startDestination = "mood_entry", modifier = Modifier.padding(innerPadding)) {
                composable("mood_entry") { MoodEntryScreen() }
                composable("edit_profile") { EditProfileScreen(onCancel = { navController.navigate("mood_entry") }) }
                composable("settings") { SettingsScreen() }
            }
        }
    } else {
        NavHost(navController = navController, startDestination = "login") {
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        val user = auth.currentUser
                        if (user != null && !user.isEmailVerified) {
                            Toast.makeText(context, "Please verify your email address.", Toast.LENGTH_SHORT).show()
                            auth.signOut()
                        }
                    },
                    onSignUpClicked = { navController.navigate("signup") }
                )
            }
            composable("signup") {
                SignUpScreen(
                    onSignUpSuccess = { email ->
                        navController.navigate("confirmation/$email")
                    },
                    onGoToTerms = { navController.navigate("terms") },
                    onGoToPrivacy = { navController.navigate("privacy") }
                )
            }
            composable("confirmation/{email}") { backStackEntry ->
                val email = backStackEntry.arguments?.getString("email") ?: ""
                ConfirmationScreen(email = email, onGoToLogin = {
                    navController.navigate("login") {
                        popUpTo("login") { inclusive = true }
                    }
                })
            }
            composable("terms") {
                TermsAndConditionsScreen()
            }
            composable("privacy") {
                PrivacyPolicyScreen()
            }
        }
    }
}
