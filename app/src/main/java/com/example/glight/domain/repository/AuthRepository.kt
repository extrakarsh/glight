package com.example.glight.domain.repository

import com.example.glight.data.auth.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthRepository {
    val authState: StateFlow<AuthState>
    suspend fun signInWithGoogle(idToken: String): Result<AuthState.Authenticated>
    suspend fun signInAnonymously(): Result<AuthState.Authenticated>
    suspend fun signInOffline(): Result<AuthState.Authenticated>
    suspend fun signOut()
    fun getCurrentUserId(): String?
    fun isAuthenticated(): Boolean
}
