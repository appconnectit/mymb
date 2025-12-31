package com.appconnectit.mymb

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appconnectit.mymb.auth.ConfirmationScreen
import com.appconnectit.mymb.auth.LoginScreen
import com.appconnectit.mymb.auth.SignUpScreen
import com.appconnectit.mymb.policies.PrivacyPolicyScreen
import com.appconnectit.mymb.policies.TermsAndConditionsScreen
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
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = { Text(text = "Home") },
                    actions = {
                        IconButton(onClick = { /* Already on Home, so do nothing */ }) {
                            Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                        }
                        IconButton(onClick = {
                            auth.signOut()
                        }) {
                            Icon(imageVector = Icons.AutoMirrored.Filled.ExitToApp , contentDescription = "Sign Out")
                        }
                    }
                )
            }
        ) { innerPadding ->
            Greeting(
                name = "Android",
                modifier = Modifier.padding(innerPadding)
            )
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

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyMBTheme {
        Greeting("Android")
    }
}
