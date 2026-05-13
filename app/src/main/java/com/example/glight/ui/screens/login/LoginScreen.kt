package com.example.glight.ui.screens.login

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.glight.BuildConfig
import com.example.glight.data.auth.AuthState
import com.example.glight.ui.components.GlassCard
import com.example.glight.ui.theme.PrimaryBlue
import com.example.glight.ui.theme.StatusGreen
import com.example.glight.ui.theme.StatusYellow
import com.example.glight.ui.theme.TextPrimary
import com.example.glight.ui.theme.TextSecondary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    darkAuditMode: Boolean,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val authState by viewModel.authState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var isSigningIn by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val webClientId = BuildConfig.GOOGLE_WEB_CLIENT_ID

    LaunchedEffect(authState) {
        when (val state = authState) {
            is AuthState.Authenticated -> onLoginSuccess()
            is AuthState.Error -> errorMessage = state.message
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.fillMaxWidth(),
            cornerRadius = 32.dp,
            contentPadding = 26.dp,
            darkAuditMode = darkAuditMode
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(color = PrimaryBlue.copy(alpha = 0.12f), shape = CircleShape) {
                    Icon(
                        Icons.Default.Lightbulb,
                        contentDescription = null,
                        tint = PrimaryBlue,
                        modifier = Modifier.padding(24.dp).size(48.dp)
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))
                Text(
                    "Grameen-Light",
                    fontSize = 34.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (darkAuditMode) Color.White else TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    "Citizen-led streetlight audit for safer, brighter villages.",
                    fontSize = 15.sp,
                    color = TextSecondary,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Spacer(modifier = Modifier.height(34.dp))

                Button(
                    onClick = {
                        scope.launch {
                            isSigningIn = true
                            errorMessage = null
                            try {
                                if (webClientId.isBlank()) {
                                    errorMessage = "Google Sign-In setup is incomplete. Add GOOGLE_WEB_CLIENT_ID, then rebuild."
                                    return@launch
                                }
                                
                                val activity = context.findActivity() ?: return@launch
                                val credentialManager = CredentialManager.create(context)
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId(webClientId)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                val result = credentialManager.getCredential(
                                    context = activity,
                                    request = request
                                )

                                val googleIdTokenCredential = GoogleIdTokenCredential
                                    .createFrom(result.credential.data)

                                viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
                            } catch (e: GetCredentialCancellationException) {
                                errorMessage = null
                            } catch (e: Exception) {
                                errorMessage = "Google Sign-In failed. Check Firebase Google provider, SHA keys, and web client ID."
                            } finally {
                                isSigningIn = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = !isSigningIn,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    AnimatedContent(targetState = isSigningIn, label = "loginButton") { loading ->
                        if (loading) {
                            CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp, modifier = Modifier.size(22.dp))
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                                Text(
                                    "Sign in with Google",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Continue Offline Button
                OutlinedButton(
                    onClick = {
                        viewModel.signInOffline()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    enabled = !isSigningIn
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Lightbulb,
                            contentDescription = null,
                            tint = if (darkAuditMode) StatusYellow else PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            "Try Demo Mode",
                            color = if (darkAuditMode) StatusYellow else PrimaryBlue,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                AnimatedVisibility(
                    visible = errorMessage != null,
                    enter = fadeIn() + slideInVertically()
                ) {
                    errorMessage?.let {
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 14.dp),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}
