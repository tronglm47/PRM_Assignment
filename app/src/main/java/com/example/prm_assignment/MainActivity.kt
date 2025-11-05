package com.example.prm_assignment

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
import com.example.prm_assignment.ui.HomePage
import com.example.prm_assignment.ui.LoginScreen
import com.example.prm_assignment.ui.theme.PRM_AssignmentTheme

class MainActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PRM_AssignmentTheme {
                val authState by authViewModel.authState.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    if (authState.isLoggedIn) {
                        HomePage(
                            modifier = Modifier.padding(innerPadding),
                            userProfile = authState.userProfile,
                            onLogout = {
                                authViewModel.logout()
                            }
                        )
                    } else {
                        LoginScreen(
                            modifier = Modifier.padding(innerPadding),
                            viewModel = authViewModel,
                            onLoginSuccess = {
                                // Tự động chuyển sang HomePage khi login thành công
                                authViewModel.loadUserProfile()
                            }
                        )
                    }
                }
            }
        }
    }
}