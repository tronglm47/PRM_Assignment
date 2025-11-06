package com.example.prm_assignment.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel? = null,
    onNavigateLogin: () -> Unit = {},
    onNavigateVerify: () -> Unit = {}
) {
    val authState by viewModel?.authState?.collectAsState() ?: remember { mutableStateOf(AuthState()) }

    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Navigate to Verify screen when pendingVerifyEmail is set
    LaunchedEffect(authState.pendingVerifyEmail) {
        if (!authState.pendingVerifyEmail.isNullOrEmpty()) {
            onNavigateVerify()
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
            Icon(
                imageVector = Icons.Default.Build,
                contentDescription = "maintenance",
                tint = Color(0xFF4B9BFF),
                modifier = Modifier.size(56.dp)
            )
        }
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Tạo tài khoản",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Nhập thông tin để đăng ký",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(Modifier.height(32.dp))

        if (authState.errorMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(authState.errorMessage ?: "", color = Color(0xFFC62828), modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(16.dp))
        }
        if (authState.successMessage != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Text(authState.successMessage ?: "", color = Color(0xFF2E7D32), modifier = Modifier.padding(12.dp))
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("Email", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF4B9BFF)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = { Text("example@mail.com") },
            enabled = !authState.isLoading
        )
        Spacer(Modifier.height(16.dp))

        Text("Số điện thoại", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null, tint = Color(0xFF4B9BFF)) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = { Text("+8490xxxxxxx") },
            enabled = !authState.isLoading
        )
        Spacer(Modifier.height(16.dp))

        Text("Mật khẩu", fontWeight = FontWeight.SemiBold, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF4B9BFF)) },
            trailingIcon = {
                TextButton(onClick = { passwordVisible = !passwordVisible }, enabled = !authState.isLoading) {
                    Text(if (passwordVisible) "Ẩn" else "Hiện")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp),
            placeholder = { Text("Nhập mật khẩu") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !authState.isLoading
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel?.register(email.trim(), phone.trim(), password) },
            enabled = email.isNotBlank() && phone.isNotBlank() && password.isNotBlank() && !authState.isLoading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
            } else {
                Text("Đăng ký", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onNavigateLogin, enabled = !authState.isLoading) {
            Text("Đã có tài khoản? Đăng nhập", color = Color(0xFF4B9BFF), fontWeight = FontWeight.SemiBold)
        }
    }
}
