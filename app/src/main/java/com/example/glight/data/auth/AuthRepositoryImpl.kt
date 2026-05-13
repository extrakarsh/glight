package com.example.glight.data.auth

import com.example.glight.domain.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth?
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    override val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        val currentUser = firebaseAuth?.currentUser
        if (currentUser != null) {
            _authState.value = AuthState.Authenticated(
                userId = currentUser.uid,
                displayName = currentUser.displayName,
                email = currentUser.email,
                photoUrl = currentUser.photoUrl?.toString()
            )
        }
    }

    override suspend fun signInWithGoogle(idToken: String): Result<AuthState.Authenticated> {
        return try {
            if (firebaseAuth == null) {
                return signInOffline()
            }

            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return Result.failure(Exception("Authentication failed"))

            val auth = AuthState.Authenticated(
                userId = user.uid,
                displayName = user.displayName,
                email = user.email,
                photoUrl = user.photoUrl?.toString()
            )
            _authState.value = auth
            Result.success(auth)
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign-in failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun signInAnonymously(): Result<AuthState.Authenticated> {
        return try {
            val auth = firebaseAuth ?: return signInOffline()
            val result = auth.signInAnonymously().await()
            val user = result.user ?: return Result.failure(Exception("Anonymous sign-in failed"))

            val authenticated = AuthState.Authenticated(
                userId = user.uid,
                displayName = "Guest User",
                email = null,
                photoUrl = null
            )
            _authState.value = authenticated
            Result.success(authenticated)
        } catch (e: Exception) {
            _authState.value = AuthState.Error("Sign-in failed: ${e.localizedMessage}")
            Result.failure(e)
        }
    }

    override suspend fun signInOffline(): Result<AuthState.Authenticated> {
        val offlineAuth = AuthState.Authenticated(
            userId = "demo-${UUID.randomUUID().toString().take(8)}",
            displayName = "Demo User",
            email = null,
            photoUrl = null,
            isOfflineSession = true
        )
        _authState.value = offlineAuth
        return Result.success(offlineAuth)
    }

    override suspend fun signOut() {
        try {
            firebaseAuth?.signOut()
        } catch (_: Exception) { }
        _authState.value = AuthState.Unauthenticated
    }

    override fun getCurrentUserId(): String? {
        return when (val state = _authState.value) {
            is AuthState.Authenticated -> state.userId
            else -> null
        }
    }

    override fun isAuthenticated(): Boolean {
        return _authState.value is AuthState.Authenticated
    }
}
