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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel? = null,
    onLoginSuccess: () -> Unit = {},
    onNavigateRegister: () -> Unit = {}
) {
    val authState by viewModel?.authState?.collectAsState() ?: remember { mutableStateOf(AuthState()) }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(authState.successMessage) {
        if (authState.successMessage != null && authState.isLoggedIn) {
            onLoginSuccess()
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

        // Icon maintenance trong vòng tròn
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

        // Tiêu đề
        Text(
            text = "Car Maintenance Services",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Đăng nhập để tiếp tục",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(Modifier.height(32.dp))

        // Error Message
        if (authState.errorMessage != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Text(
                    text = authState.errorMessage ?: "",
                    color = Color(0xFFC62828),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Success Message
        if (authState.successMessage != null && !authState.isLoggedIn) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
            ) {
                Text(
                    text = authState.successMessage ?: "",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.height(16.dp))
        }

        // Email
        Text(
            text = "Email",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "email",
                    tint = Color(0xFF4B9BFF)
                )
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text(text = "example@mail.com") },
            enabled = !authState.isLoading
        )

        Spacer(Modifier.height(16.dp))

        // Password
        Text(
            text = "Mật khẩu",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            modifier = Modifier.fillMaxWidth(),
            fontSize = 14.sp
        )
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "password",
                    tint = Color(0xFF4B9BFF)
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }, enabled = !authState.isLoading) {
                    Text(
                        text = if (passwordVisible) "Ẩn" else "Hiện",
                        fontSize = 12.sp,
                        color = Color(0xFF4B9BFF),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            placeholder = { Text(text = "Nhập mật khẩu") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            enabled = !authState.isLoading
        )

        Spacer(Modifier.height(28.dp))

        // Login button
        Button(
            onClick = {
                viewModel?.login(email.trim(), password)
            },
            enabled = email.isNotBlank() && password.isNotBlank() && !authState.isLoading,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (authState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(text = "Đăng nhập", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(Modifier.height(16.dp))

        TextButton(onClick = onNavigateRegister, enabled = !authState.isLoading) {
            Text("Chưa có tài khoản? Đăng ký", color = Color(0xFF4B9BFF), fontWeight = FontWeight.SemiBold)
        }

        Spacer(Modifier.height(16.dp))

        // Điều khoản
        val annotated = buildAnnotatedString {
            append("Bằng cách tiếp tục, bạn đồng ý với ")
            withStyle(style = SpanStyle(color = Color(0xFF4B9BFF), fontWeight = FontWeight.SemiBold)) {
                append("Điều khoản sử dụng")
            }
            append(" và ")
            withStyle(style = SpanStyle(color = Color(0xFF4B9BFF), fontWeight = FontWeight.SemiBold)) {
                append("Chính sách bảo mật")
            }
        }
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            fontSize = 12.sp
        )
    }
}
