package com.example.prm_assignment.data

import android.content.Context
import com.example.prm_assignment.data.model.ProfileResponse
import com.example.prm_assignment.data.remote.ProfileRetrofitClient
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
}
