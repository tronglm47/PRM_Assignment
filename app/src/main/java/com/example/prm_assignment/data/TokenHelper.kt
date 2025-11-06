package com.example.prm_assignment.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
}

