package com.example.prm_assignment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VerifyOtpScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel? = null,
    onBackToLogin: () -> Unit = {}
) {
    val authState by viewModel?.authState?.collectAsState() ?: remember { mutableStateOf(AuthState()) }
    var otp by remember { mutableStateOf("") }

    // Auto-return to login if verified successfully (pendingVerifyEmail cleared and successMessage set)
    LaunchedEffect(authState.pendingVerifyEmail, authState.successMessage) {
        if (authState.pendingVerifyEmail == null && (authState.successMessage?.contains("Xác minh", true) == true)) {
            onBackToLogin()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(top = 20.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(20.dp))
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(Color(0xFFEAF6FF)),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Build, contentDescription = null, tint = Color(0xFF4B9BFF), modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("Xác minh OTP", style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 24.sp))
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Mã OTP đã được gửi tới: ${authState.pendingVerifyEmail ?: "email của bạn"}",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(24.dp))

        if (authState.errorMessage != null) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))) {
                Text(authState.errorMessage ?: "", color = Color(0xFFC62828), modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(12.dp))
        }
        if (authState.successMessage != null) {
            Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Text(authState.successMessage ?: "", color = Color(0xFF2E7D32), modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = otp,
            onValueChange = { otp = it },
            label = { Text("Nhập OTP") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            enabled = !authState.isLoading
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = { viewModel?.verifyOtp(otp.trim()) },
            enabled = otp.length >= 4 && !authState.isLoading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Xác minh", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onBackToLogin, enabled = !authState.isLoading) {
            Text("Quay về đăng nhập")
        }
    }
}

