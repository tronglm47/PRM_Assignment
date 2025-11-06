package com.example.prm_assignment

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.prm_assignment.ui.AuthViewModel
import com.example.prm_assignment.ui.LoginScreen
import com.example.prm_assignment.ui.RegisterScreen
import com.example.prm_assignment.ui.VerifyOtpScreen
import com.example.prm_assignment.ui.theme.PRM_AssignmentTheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PRM_AssignmentTheme {
                val authState by authViewModel.authState.collectAsState()
                var screen by remember { mutableStateOf("login") } // login | register | verify

                // Auto-navigate to HomeActivity when login is successful
                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        val intent = Intent(this@MainActivity, Class.forName("com.example.prm_assignment.HomeActivity"))
                        startActivity(intent)
                        finish()
                    }
                }

                // If have pending email -> go to verify
                LaunchedEffect(authState.pendingVerifyEmail) {
                    if (!authState.pendingVerifyEmail.isNullOrEmpty()) screen = "verify"
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    when (screen) {
                        "register" -> RegisterScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = authViewModel,
                            onNavigateLogin = { screen = "login" },
                            onNavigateVerify = { screen = "verify" }
                        )
                        "verify" -> VerifyOtpScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = authViewModel,
                            onBackToLogin = { screen = "login" }
                        )
                        else -> LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = authViewModel,
                            onLoginSuccess = { authViewModel.loadUserProfile() },
                            onNavigateRegister = { screen = "register" }
                        )
                    }
                }
            }
        }
    }
}