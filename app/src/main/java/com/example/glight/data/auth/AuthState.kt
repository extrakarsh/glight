package com.example.glight.data.auth

sealed interface AuthState {
    data object Loading : AuthState
    data class Authenticated(
        val userId: String,
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val isOfflineSession: Boolean = false
    ) : AuthState
    data object Unauthenticated : AuthState
    data class Error(val message: String) : AuthState
}
