package com.example.prm_assignment.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prm_assignment.data.TokenManager
import com.example.prm_assignment.data.model.LoginRequest
import com.example.prm_assignment.data.model.UserProfile
import com.example.prm_assignment.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userProfile: UserProfile? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application.applicationContext)
    private val authApi = RetrofitClient.getAuthApi()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

                val request = LoginRequest(
                    identifier = email,
                    password = password
                )

                val response = authApi.login(request)

                if (response.success && response.data != null) {
                    // Lưu token
                    tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)

                    // Lấy thông tin user
                    fetchUserProfile("Bearer ${response.data.accessToken}")

                    _authState.value = _authState.value.copy(
                        isLoggedIn = true,
                        isLoading = false,
                        successMessage = "Đăng nhập thành công!"
                    )
                } else {
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = response.message ?: "Đăng nhập thất bại"
                    )
                }
            } catch (e: java.net.ConnectException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra:\n1. Backend đã chạy chưa?\n2. Port đúng chưa? (hiện tại: 8080)"
                )
            } catch (e: java.net.SocketTimeoutException) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Kết nối timeout. Server không phản hồi."
                )
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi: ${e.localizedMessage ?: e.message}"
                )
            }
        }
    }

    private fun fetchUserProfile(token: String) {
        viewModelScope.launch {
            try {
                val response = authApi.getProfile(token)
                if (response.success && response.data != null) {
                    _authState.value = _authState.value.copy(
                        userProfile = response.data
                    )
                }
            } catch (e: Exception) {
                // Xử lý lỗi lấy profile
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                _authState.value = _authState.value.copy(isLoading = true)

                val token = tokenManager.getAccessToken()
                if (token != null) {
                    authApi.logout("Bearer $token")
                }

                tokenManager.clearTokens()
                _authState.value = AuthState(successMessage = "Đã đăng xuất")
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi đăng xuất: ${e.message}"
                )
            }
        }
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            val token = tokenManager.getAccessToken()
            if (token != null) {
                fetchUserProfile("Bearer $token")
            }
        }
    }

    fun clearMessages() {
        _authState.value = _authState.value.copy(errorMessage = null, successMessage = null)
    }
}
