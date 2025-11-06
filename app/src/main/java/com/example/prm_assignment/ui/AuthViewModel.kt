package com.example.prm_assignment.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.prm_assignment.data.TokenManager
import com.example.prm_assignment.data.model.LoginRequest
import com.example.prm_assignment.data.model.ProfileData
import com.example.prm_assignment.data.remote.RetrofitClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val userProfile: ProfileData? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val tokenManager = TokenManager(application.applicationContext)
    private val authApi = RetrofitClient.getAuthApi()

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    companion object {
        private const val TAG = "AuthViewModel"
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting login for email: $email")
                _authState.value = _authState.value.copy(isLoading = true, errorMessage = null)

                val request = LoginRequest(
                    identifier = email,
                    password = password
                )

                Log.d(TAG, "Calling login API...")
                val response = authApi.login(request)
                Log.d(TAG, "Login API response: success=${response.success}, message=${response.message}")

                if (response.success && response.data != null) {
                    // Lưu token
                    Log.d(TAG, "Saving tokens...")
                    tokenManager.saveTokens(response.data.accessToken, response.data.refreshToken)
                    Log.d(TAG, "Tokens saved successfully")

                    // Lấy thông tin user trước khi set isLoggedIn = true
                    try {
                        Log.d(TAG, "Fetching user profile...")
                        val profileResponse = authApi.getProfile("Bearer ${response.data.accessToken}")
                        Log.d(TAG, "Profile API response: success=${profileResponse.success}")

                        val userProfile = if (profileResponse.success && profileResponse.data != null) {
                            Log.d(TAG, "User profile: ${profileResponse.data}")
                            profileResponse.data
                        } else {
                            Log.w(TAG, "Profile response failed or data is null")
                            null
                        }

                        // Chỉ set isLoggedIn = true sau khi đã có thông tin user
                        Log.d(TAG, "Setting isLoggedIn = true")
                        _authState.value = _authState.value.copy(
                            isLoggedIn = true,
                            isLoading = false,
                            userProfile = userProfile,
                            successMessage = "Đăng nhập thành công!"
                        )
                        Log.d(TAG, "Login completed successfully")
                    } catch (profileException: Exception) {
                        Log.e(TAG, "Error fetching profile", profileException)
                        // Vẫn cho đăng nhập thành công nhưng không có profile
                        _authState.value = _authState.value.copy(
                            isLoggedIn = true,
                            isLoading = false,
                            userProfile = null,
                            successMessage = "Đăng nhập thành công!"
                        )
                    }
                } else {
                    Log.w(TAG, "Login failed: ${response.message}")
                    _authState.value = _authState.value.copy(
                        isLoading = false,
                        errorMessage = response.message ?: "Đăng nhập thất bại"
                    )
                }
            } catch (e: java.net.ConnectException) {
                Log.e(TAG, "Connection exception", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Không thể kết nối đến server. Vui lòng kiểm tra:\n1. Backend đã chạy chưa?\n2. Port đúng chưa? (hiện tại: 8080)"
                )
            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Socket timeout exception", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Kết nối timeout. Server không phản hồi."
                )
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected exception during login", e)
                _authState.value = _authState.value.copy(
                    isLoading = false,
                    errorMessage = "Lỗi: ${e.localizedMessage ?: e.message}\n${e.stackTraceToString()}"
                )
            }
        }
    }

    private suspend fun fetchUserProfile(token: String) {
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
