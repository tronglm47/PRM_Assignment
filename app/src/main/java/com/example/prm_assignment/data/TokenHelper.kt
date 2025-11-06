package com.example.prm_assignment.data

import android.content.Context
import com.example.prm_assignment.data.model.ProfileResponse
import com.example.prm_assignment.data.remote.ProfileRetrofitClient
import com.example.prm_assignment.data.remote.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class TokenHelper(private val context: Context) {
    private val tokenManager = TokenManager(context)

    fun getTokenAndExecute(callback: TokenCallback) {
        CoroutineScope(Dispatchers.Main).launch {
            val token = tokenManager.getAccessToken()
            callback.onTokenRetrieved(token)
        }
    }

    interface TokenCallback {
        fun onTokenRetrieved(token: String?)
    }

    // Callback for async operations
    interface TokenAsyncCallback {
        fun onResult(token: String?)
    }

    interface ClearTokensCallback {
        fun onComplete()
    }

    interface ProfileCallback {
        fun onSuccess(profile: ProfileResponse)
        fun onError(message: String)
    }

    // 🆕 Lấy userId (trong data.userId.id) từ API /auth/profile
    fun getUserIdFromProfile(callback: (String?) -> Unit) {
        getTokenAndExecute(object : TokenCallback {
            override fun onTokenRetrieved(token: String?) {
                if (token.isNullOrEmpty()) {
                    callback(null)
                    return
                }

                val api = ProfileRetrofitClient.getInstance().getProfileApi()
                api.getProfile("Bearer $token").enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.isSuccess == true) {
                            // ✅ Lấy userId chính xác
                            val userId = response.body()?.data?.userId?.id
                            callback(userId)
                        } else {
                            callback(null)
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        callback(null)
                    }
                })
            }
        })
    }

    companion object {
        @JvmStatic
        suspend fun getAccessToken(context: Context): String? {
            return TokenManager(context).getAccessToken()
        }

        @JvmStatic
        suspend fun clearTokens(context: Context) {
            TokenManager(context).clearTokens()
        }

        // Java-friendly methods that handle coroutines internally
        @JvmStatic
        fun getAccessTokenAsync(context: Context, callback: TokenAsyncCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                val token = TokenManager(context).getAccessToken()
                callback.onResult(token)
            }
        }

        @JvmStatic
        fun clearTokensAsync(context: Context, callback: ClearTokensCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                TokenManager(context).clearTokens()
                callback.onComplete()
            }
        }

        @JvmStatic
        fun loadProfileAsync(context: Context, callback: ProfileCallback) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val token = TokenManager(context).getAccessToken()
                    if (token.isNullOrEmpty()) {
                        callback.onError("Token not found")
                        return@launch
                    }

                    val response = RetrofitClient.getAuthApi().getProfile("Bearer $token")
                    callback.onSuccess(response)
                } catch (e: Exception) {
                    callback.onError(e.message ?: "Unknown error")
                }
            }
        }
    }
}
