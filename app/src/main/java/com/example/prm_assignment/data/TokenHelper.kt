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
    interface UserIdCallback {
        fun onUserIdRetrieved(userId: String?)
    }

    interface CustomerIdCallback {
        fun onCustomerIdRetrieved(customerId: String?)
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

    // Java-friendly version
    fun getUserIdFromProfile(callback: UserIdCallback) {
        getUserIdFromProfile { userId ->
            callback.onUserIdRetrieved(userId)
        }
    }

    // 🆕 Lấy customerId từ userId thông qua API /customers/user/{userId}
    fun getCustomerIdFromProfile(callback: CustomerIdCallback) {
        getTokenAndExecute(object : TokenCallback {
            override fun onTokenRetrieved(token: String?) {
                if (token.isNullOrEmpty()) {
                    callback.onCustomerIdRetrieved(null)
                    return
                }

                // First get userId from profile
                val authApi = ProfileRetrofitClient.getInstance().getProfileApi()
                authApi.getProfile("Bearer $token").enqueue(object : Callback<ProfileResponse> {
                    override fun onResponse(
                        call: Call<ProfileResponse>,
                        response: Response<ProfileResponse>
                    ) {
                        if (response.isSuccessful && response.body()?.isSuccess == true) {
                            val userId = response.body()?.data?.userId?.id
                            if (userId.isNullOrEmpty()) {
                                callback.onCustomerIdRetrieved(null)
                                return
                            }

                            // Then get customerId from userId
                            val customerApi = com.example.prm_assignment.data.remote.CustomerRetrofitClient.getInstance().getCustomerApi()
                            customerApi.getCustomerByUserId(userId, "Bearer $token").enqueue(object : Callback<com.example.prm_assignment.data.model.CustomerResponse> {
                                override fun onResponse(
                                    call: Call<com.example.prm_assignment.data.model.CustomerResponse>,
                                    response: Response<com.example.prm_assignment.data.model.CustomerResponse>
                                ) {
                                    if (response.isSuccessful && response.body()?.isSuccess == true) {
                                        val customerId = response.body()?.data?.id
                                        callback.onCustomerIdRetrieved(customerId)
                                    } else {
                                        callback.onCustomerIdRetrieved(null)
                                    }
                                }

                                override fun onFailure(call: Call<com.example.prm_assignment.data.model.CustomerResponse>, t: Throwable) {
                                    callback.onCustomerIdRetrieved(null)
                                }
                            })
                        } else {
                            callback.onCustomerIdRetrieved(null)
                        }
                    }

                    override fun onFailure(call: Call<ProfileResponse>, t: Throwable) {
                        callback.onCustomerIdRetrieved(null)
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
