package org.asv.looplink.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import org.asv.looplink.DatabaseManager
import org.asv.looplink.data.repository.UserRepository
import org.asv.looplink.errors.errorsLL
import org.asv.looplink.ui.MainScreen
import org.asv.looplink.webDriver.cuimsAPI
import org.koin.compose.koinInject

// ─────────────────────────────────────────────────────────────────────────────
// Palette (keeps magic numbers in one place)
// ─────────────────────────────────────────────────────────────────────────────
private val BgTop        = Color(0xFF0D0F1A)
private val BgBottom     = Color(0xFF1A1C24)
private val CardBg       = Color(0xFF1E2030)
private val Divider      = Color(0xFF2A2D3E)
private val Subtle       = Color(0xFF8B8FA8)
private val VerySubtle   = Color(0xFF4A4F68)
private val AccentBlue   = Color(0xFF6A88FF)

// ─────────────────────────────────────────────────────────────────────────────
// Screen
// ─────────────────────────────────────────────────────────────────────────────
class LoginFields(val onLoginSuccess: () -> Unit) : Screen {

    @Composable
    override fun Content() {
        val cuimsAPI: cuimsAPI       = koinInject()
        val userRepository: UserRepository = koinInject()
        val database: DatabaseManager      = koinInject()
        val navigator = LocalAppNavigator.currentOrThrow

        // ── State ──────────────────────────────────────────────────────────
        var uidField       by remember { mutableStateOf("") }
        var passField      by remember { mutableStateOf("") }
        var passVisible    by remember { mutableStateOf(false) }
        var captchaField   by remember { mutableStateOf("") }
        var captchaImage   by remember { mutableStateOf(ImageBitmap(110, 48)) }
        var showCaptcha    by remember { mutableStateOf(false) }

        var uidError       by remember { mutableStateOf(false) }
        var passError      by remember { mutableStateOf(false) }
        var captchaError   by remember { mutableStateOf(false) }
        var isLoading      by remember { mutableStateOf(false) }
        var errorMessage   by remember { mutableStateOf("") }

        var showGuestPrompt by remember { mutableStateOf(false) }
        var guestNameField  by remember { mutableStateOf("") }
        var guestNameError  by remember { mutableStateOf(false) }

        val scope = rememberCoroutineScope()

        // ── Helpers ────────────────────────────────────────────────────────
        fun resetErrors() {
            uidError = false; passError = false; captchaError = false; guestNameError = false; errorMessage = ""
        }

        fun navigateToMain() {
            navigator.navigator.replaceAll(MainScreen())
        }

        // ── CUIMS login flow ───────────────────────────────────────────────
        fun handleSignIn() {
            scope.launch {
                isLoading = true
                resetErrors()

                if (!showCaptcha) {
                    // Step 1 – validate credentials
                    if (uidField.isBlank())  { uidError  = true; isLoading = false; return@launch }
                    if (passField.isBlank()) { passError = true; isLoading = false; return@launch }

                    try {
                        val result = cuimsAPI.login(uidField, passField)
                        if (!result.success) {
                            errorMessage = result.message
                        } else {
                            val captchaResult = cuimsAPI.getCaptcha()
                            if (captchaResult.first.success) {
                                captchaImage = captchaResult.second!!
                                showCaptcha  = true
                            } else {
                                errorMessage = captchaResult.first.message
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = errorsLL.internet_error + e.message
                    }

                } else {
                    // Step 2 – verify captcha
                    if (captchaField.isBlank()) { captchaError = true; isLoading = false; return@launch }

                    try {
                        val result = cuimsAPI.fillCaptcha(captchaField)
                        if (!result.success) {
                            errorMessage = result.message
                            when (result.message) {
                                "Invalid Captcha" -> {
                                    captchaError = true
                                    val fresh = cuimsAPI.getCaptcha()
                                    if (fresh.first.success) captchaImage = fresh.second!!
                                }
                                "User Id or Password In Correct" -> {
                                    passError = true; uidError = true
                                    showCaptcha = false
                                    cuimsAPI.endSession()
                                }
                                else -> captchaError = true
                            }
                        } else {
                            val data = cuimsAPI.loadStudentData()
                            if (data.first.success) {
                                userRepository.insertAndLoadUser(data.second!!)
                                onLoginSuccess()
                                navigateToMain()
                                cuimsAPI.destroySession()
                            } else {
                                errorMessage = data.first.message
                            }
                        }
                    } catch (e: Exception) {
                        errorMessage = errorsLL.unknownError
                    }
                }
                isLoading = false
            }
        }

        // ── Guest login flow ───────────────────────────────────────────────
        // Inserts directly via DatabaseManager so pfpPath can safely be null,
        // then reloads the user from the database via UserRepository.loadUser().
        // ── Guest login flow ───────────────────────────────────────────────
        fun handleGuestLogin() {
            if (guestNameField.isBlank()) {
                guestNameError = true
                return
            }

            scope.launch {
                isLoading = true
                resetErrors()
                try {
                    val guestId = "guest_${System.currentTimeMillis()}"
                    // Use the input field instead of the hardcoded "Guest" string
                    database.insertUserData(name = guestNameField.trim(), uid = guestId)
                    userRepository.loadUser()
                    onLoginSuccess()
                    navigateToMain()
                } catch (e: Exception) {
                    errorMessage = "Could not create a guest session. Please try again."
                }
                isLoading = false
            }
        }

        // ── Layout ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.verticalGradient(listOf(BgTop, BgBottom))),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = 440.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
                border = BorderStroke(1.dp, Divider)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 28.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Brand
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(AccentBlue.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🔗", fontSize = 32.sp)
                    }
                    Text(
                        "LoopLink",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    )
                    Text(
                        "Sign in with your CUIMS account",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Subtle,
                        textAlign = TextAlign.Center
                    )

                    Spacer(Modifier.height(4.dp))

                    // UID
                    OutlinedTextField(
                        value = uidField,
                        onValueChange = { uidField = it; uidError = false },
                        label = { Text("University ID") },
                        leadingIcon = { Icon(Icons.Default.Person, null) },
                        singleLine = true,
                        isError = uidError,
                        supportingText = if (uidError) {{ Text("UID cannot be empty") }} else null,
                        enabled = !isLoading && !showCaptcha,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Password
                    OutlinedTextField(
                        value = passField,
                        onValueChange = { passField = it; passError = false },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null) },
                        trailingIcon = {
                            IconButton(onClick = { passVisible = !passVisible }) {
                                Icon(
                                    if (passVisible) Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    contentDescription = if (passVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        visualTransformation = if (passVisible) VisualTransformation.None
                        else PasswordVisualTransformation(),
                        singleLine = true,
                        isError = passError,
                        supportingText = if (passError) {{ Text("Password cannot be empty") }} else null,
                        enabled = !isLoading && !showCaptcha,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Captcha – animates in after first successful sign-in step
                    AnimatedVisibility(
                        visible = showCaptcha,
                        enter = fadeIn() + expandVertically(),
                        exit  = fadeOut() + shrinkVertically()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                "Complete the captcha to continue",
                                style = MaterialTheme.typography.labelMedium,
                                color = Subtle
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = captchaField,
                                    onValueChange = { captchaField = it; captchaError = false },
                                    label = { Text("Captcha") },
                                    singleLine = true,
                                    isError = captchaError,
                                    enabled = !isLoading,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .size(width = 110.dp, height = 52.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(bitmap = captchaImage, contentDescription = "Captcha")
                                }
                            }
                        }
                    }

                    // Error banner
                    AnimatedVisibility(visible = errorMessage.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.error.copy(alpha = 0.12f))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = errorMessage,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    Spacer(Modifier.height(4.dp))

                    // Sign In button
                    Button(
                        onClick = ::handleSignIn,
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.5.dp
                            )
                        } else {
                            Text(
                                text = if (showCaptcha) "Verify & Continue" else "Sign In",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // ── OR divider ─────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Divider)
                        Text("or", color = Subtle, fontSize = 12.sp)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = Divider)
                    }

// Guest Name Input (Animates in when prompt is triggered)
                    AnimatedVisibility(
                        visible = showGuestPrompt,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = guestNameField,
                            onValueChange = { guestNameField = it; guestNameError = false },
                            label = { Text("Enter your name") },
                            leadingIcon = { Icon(Icons.Default.Person, null) },
                            singleLine = true,
                            isError = guestNameError,
                            supportingText = if (guestNameError) {{ Text("Name cannot be empty") }} else null,
                            enabled = !isLoading,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }

// Guest button
                    OutlinedButton(
                        onClick = {
                            if (showGuestPrompt) {
                                handleGuestLogin()
                            } else {
                                showGuestPrompt = true
                                resetErrors()
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, Divider)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            tint = Subtle,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (showGuestPrompt) "Start Guest Session" else "Continue as Guest",
                            color = Subtle,
                            fontSize = 15.sp
                        )
                    }

                    Text(
                        "Guest accounts can't access CUIMS features",
                        style = MaterialTheme.typography.labelSmall,
                        color = VerySubtle,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose { cuimsAPI.endSession() }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Kept for potential use elsewhere in the project
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun TextFieldFooterErrorMsg(text: String = "Error") {
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = text,
        color = MaterialTheme.colorScheme.error,
        textAlign = TextAlign.Start,
        fontSize = 15.sp
    )
}