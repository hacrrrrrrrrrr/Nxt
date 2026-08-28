package com.example.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import android.media.MediaPlayer
import com.example.network.SupabaseClient
import com.example.ui.theme.*
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _authMessage = MutableStateFlow<String?>(null)
    val authMessage: StateFlow<String?> = _authMessage.asStateFlow()

    private val _isSuccess = MutableStateFlow(false)
    val isSuccess: StateFlow<Boolean> = _isSuccess.asStateFlow()

    fun clearMessage() {
        _authMessage.value = null
    }
    
    fun setErrorMessage(msg: String) {
        _authMessage.value = msg
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authMessage.value = "Email and Password cannot be empty."
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                SupabaseClient.client.auth.signInWith(Email) {
                    this.email = email
                    this.password = pass
                }
                _isSuccess.value = true
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _authMessage.value = when {
                    msg.contains("Invalid login credentials") -> "Incorrect email or password."
                    msg.contains("Email not confirmed") -> "Please verify your email address first."
                    else -> "Login failed: $msg"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun signup(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            _authMessage.value = "Email and Password cannot be empty."
            return
        }
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                SupabaseClient.client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = pass
                }
                _isSuccess.value = true
            } catch (e: Exception) {
                val msg = e.message ?: ""
                _authMessage.value = when {
                    msg.contains("already registered") -> "An account with this email already exists."
                    msg.contains("Password should be") -> "Password is too weak. Please use a stronger password."
                    msg.contains("valid email") -> "Please enter a valid email address."
                    else -> "Sign Up failed: $msg"
                }
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}

@Composable
fun AuthScreen(onAuthSuccess: () -> Unit, viewModel: AuthViewModel = viewModel()) {
    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val authMessage by viewModel.authMessage.collectAsState()
    val isSuccess by viewModel.isSuccess.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current

    val bulletX = remember { Animatable(0f) }
    val recoil = remember { Animatable(0f) }
    val flashAlpha = remember { Animatable(0f) }
    val coroutineScope = rememberCoroutineScope()

    fun fireGun() {
        try {
            val mediaPlayer = MediaPlayer.create(context, com.example.R.raw.sound)
            mediaPlayer?.start()
            mediaPlayer?.setOnCompletionListener { it.release() }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        coroutineScope.launch {
            bulletX.snapTo(0f)
            flashAlpha.snapTo(0f)
            recoil.snapTo(0f)

            launch {
                flashAlpha.animateTo(1f, tween(50))
                flashAlpha.animateTo(0f, tween(100))
            }
            launch {
                recoil.animateTo(-20f, tween(50, easing = FastOutSlowInEasing))
                recoil.animateTo(0f, tween(200, easing = LinearOutSlowInEasing))
            }
            bulletX.animateTo(3000f, tween(400, easing = LinearEasing))
        }
    }

    LaunchedEffect(isSuccess) {
        if (isSuccess) {
            onAuthSuccess()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        // Background Gun & Bullet Animation
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height
            
            val gunScale = 3.5f
            withTransform({
                translate(width * 0.05f + recoil.value, height * 0.15f)
                scale(gunScale, gunScale, pivot = Offset.Zero)
                rotate(recoil.value * -0.5f, pivot = Offset(10f, 40f))
            }) {
                val gunAlpha = 0.08f
                
                // barrel
                drawRoundRect(
                    color = Color.Black.copy(alpha = gunAlpha),
                    topLeft = Offset(0f, 0f),
                    size = Size(140f, 25f),
                    cornerRadius = CornerRadius(4f)
                )
                // handle
                drawRoundRect(
                    color = Color.Black.copy(alpha = gunAlpha),
                    topLeft = Offset(10f, 25f),
                    size = Size(35f, 65f),
                    cornerRadius = CornerRadius(4f)
                )
                // trigger guard
                drawRoundRect(
                    color = Color.Black.copy(alpha = gunAlpha),
                    topLeft = Offset(45f, 25f),
                    size = Size(25f, 15f),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4f)
                )
                // details
                drawLine(
                    color = Color.Black.copy(alpha = gunAlpha),
                    start = Offset(25f, 30f),
                    end = Offset(25f, 80f),
                    strokeWidth = 2f
                )
                
                // Muzzle Flash
                if (flashAlpha.value > 0f) {
                    drawCircle(
                        color = Color(0xFFFFB300).copy(alpha = flashAlpha.value * 0.8f),
                        radius = 40f * flashAlpha.value,
                        center = Offset(150f, 12.5f)
                    )
                }
            }
            
            // Bullet
            if (bulletX.value > 0f) {
                val startX = width * 0.05f + (140f * gunScale) 
                val bulletY = height * 0.15f + (12.5f * gunScale)
                
                drawRoundRect(
                    color = Color(0xFFFF7A00),
                    topLeft = Offset(startX + bulletX.value, bulletY - 6f),
                    size = Size(60f, 12f),
                    cornerRadius = CornerRadius(6f)
                )
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(Color.Transparent, Color(0xFFFF7A00).copy(alpha = 0.5f)),
                        startX = startX + bulletX.value - 300f,
                        endX = startX + bulletX.value
                    ),
                    start = Offset(startX + bulletX.value - 300f, bulletY),
                    end = Offset(startX + bulletX.value, bulletY),
                    strokeWidth = 8f
                )
            }
        }

        // Form Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Text(
            text = if (isLoginMode) "Welcome Back" else "Create Account",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter your details to continue",
            fontSize = 14.sp,
            color = TextGray
        )

        Spacer(modifier = Modifier.height(24.dp))
        
        androidx.compose.animation.AnimatedVisibility(visible = authMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(androidx.compose.ui.graphics.Color(0xFFFFEBEE), RoundedCornerShape(8.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = authMessage ?: "",
                    color = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { 
                email = it
                viewModel.clearMessage()
            },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { 
                password = it
                viewModel.clearMessage()
            },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                fireGun()
                if (isLoginMode) {
                    viewModel.login(email, password)
                } else {
                    viewModel.signup(email, password)
                }
            },
            enabled = !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryOrange),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = SurfaceWhite, modifier = Modifier.size(24.dp))
            } else {
                Text(
                    text = if (isLoginMode) "LOG IN" else "SIGN UP",
                    fontWeight = FontWeight.Bold,
                    color = SurfaceWhite
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = { isLoginMode = !isLoginMode }) {
            Text(
                text = if (isLoginMode) "Don't have an account? Sign Up" else "Already have an account? Log In",
                color = PrimaryOrange
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(text = "OR", color = TextGray, fontSize = 12.sp)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        SupabaseClient.client.auth.signInWith(io.github.jan.supabase.auth.providers.Google)
                    } catch (e: Exception) {
                        viewModel.setErrorMessage("Google Sign-In Error: ${e.message}")
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = TextDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.ic_google_logo),
                contentDescription = "Google Logo",
                modifier = Modifier.size(24.dp),
                tint = Color.Unspecified
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "CONTINUE WITH GOOGLE",
                fontWeight = FontWeight.Bold,
                color = SurfaceWhite
            )
        }
    }
    }
}
