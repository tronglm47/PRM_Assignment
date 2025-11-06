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
import com.example.prm_assignment.HomeActivity
import com.example.prm_assignment.ui.AuthViewModel
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

                // Auto-navigate to HomeActivity when login is successful
                LaunchedEffect(authState.isLoggedIn) {
                    if (authState.isLoggedIn) {
                        val intent = Intent(this@MainActivity, Class.forName("com.example.prm_assignment.HomeActivity"))
                        startActivity(intent)
                        finish()
                    }
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = authViewModel,
                        onLoginSuccess = {
                            // Tự động chuyển sang HomeActivity khi login thành công
                            authViewModel.loadUserProfile()
                        }
                    )
                }
            }
        }
    }
}