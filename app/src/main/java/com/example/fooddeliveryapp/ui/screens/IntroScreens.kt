package com.example.fooddeliveryapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fooddeliveryapp.backend.BackendResult
import com.example.fooddeliveryapp.backend.YandexGeocodingApi
import com.example.fooddeliveryapp.ui.AddressLabelHome
import com.example.fooddeliveryapp.ui.AppLanguage
import com.example.fooddeliveryapp.ui.components.AppTextField
import com.example.fooddeliveryapp.ui.components.BrandHeaderChip
import com.example.fooddeliveryapp.ui.components.BrandMark
import com.example.fooddeliveryapp.ui.components.DecorativeBackground
import com.example.fooddeliveryapp.ui.components.PrimaryButton
import com.example.fooddeliveryapp.ui.components.SmallMetric
import com.example.fooddeliveryapp.ui.components.ToggleRow
import com.example.fooddeliveryapp.ui.EmailVerificationCodeResult
import com.example.fooddeliveryapp.ui.LocalAppLanguage
import com.example.fooddeliveryapp.ui.LocalAppStrings
import com.example.fooddeliveryapp.ui.OnboardingText
import com.example.fooddeliveryapp.ui.PasswordResetCodeResult
import com.example.fooddeliveryapp.ui.WELCOME_COUPON_CODE
import com.example.fooddeliveryapp.ui.yandexGeocoderLanguage
import com.example.fooddeliveryapp.ui.auth.GoogleAccountProfile
import com.example.fooddeliveryapp.ui.auth.GoogleAuthUiClient
import com.example.fooddeliveryapp.ui.auth.GoogleAuthUiResult
import com.example.fooddeliveryapp.ui.data.DeliveryAddress
import com.example.fooddeliveryapp.ui.data.GeoPoint
import com.example.fooddeliveryapp.ui.data.OnboardingSlide
import com.example.fooddeliveryapp.ui.data.SampleData
import com.example.fooddeliveryapp.ui.map.YandexMapPicker
import com.example.fooddeliveryapp.ui.theme.CardWhite
import com.example.fooddeliveryapp.ui.theme.Cream
import com.example.fooddeliveryapp.ui.theme.Ink
import com.example.fooddeliveryapp.ui.theme.InkSoft
import com.example.fooddeliveryapp.ui.theme.Night
import com.example.fooddeliveryapp.ui.theme.Orange
import com.example.fooddeliveryapp.ui.theme.OrangeSoft
import com.example.fooddeliveryapp.ui.theme.Gold
import com.example.fooddeliveryapp.ui.theme.Rose
import com.example.fooddeliveryapp.ui.theme.Sky
import com.example.fooddeliveryapp.ui.theme.Success
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    isReady: Boolean = true,
    returningUser: Boolean = false,
    onFinished: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val transition = rememberInfiniteTransition(label = "splashLogo")
    val logoScale by transition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 850),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "splashLogoScale",
    )

    LaunchedEffect(isReady) {
        if (!isReady) return@LaunchedEffect
        delay(if (returningUser) 1_450 else 900)
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Night),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(126.dp)
                    .scale(logoScale)
                    .clip(RoundedCornerShape(34.dp))
                    .background(Orange),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Foodly",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = CardWhite,
                )
            }
            Text(
                text = if (returningUser) "Добро пожаловать" else "Foodly",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = CardWhite,
            )
            Text(
                text = strings.appTagline,
                style = MaterialTheme.typography.bodyLarge,
                color = CardWhite.copy(alpha = 0.72f),
            )
        }
    }
}

@Composable
fun OnboardingScreen(
    slides: List<OnboardingSlide>,
    onFinish: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val pagerState = rememberPagerState(pageCount = { slides.size })
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onFinish) {
                Text(strings.skip, color = Orange)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
        ) { page ->
            val slide = slides[page]
            val copy = strings.onboarding.getOrElse(page) {
                OnboardingText(slide.title, slide.description)
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                EmojiIllustration(
                    emoji = slide.emoji,
                    accent = slide.accent,
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = copy.title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = copy.description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
        ) {
            repeat(slides.size) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = if (index == pagerState.currentPage) 26.dp else 8.dp, height = 8.dp)
                        .clip(CircleShape)
                        .background(if (index == pagerState.currentPage) Orange else Orange.copy(alpha = 0.18f)),
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        PrimaryButton(
            text = if (pagerState.currentPage == slides.lastIndex) strings.start else strings.next,
            onClick = {
                if (pagerState.currentPage == slides.lastIndex) {
                    onFinish()
                } else {
                    scope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            },
        )
    }
}

@Composable
fun AuthScreen(
    onLogin: suspend (email: String, password: String) -> String?,
    onRegister: suspend (name: String, email: String, password: String) -> String?,
    onRequestRegistrationVerification: suspend (email: String) -> EmailVerificationCodeResult,
    onVerifyRegistrationCode: suspend (email: String, code: String) -> String?,
    onAdminLogin: (username: String, password: String) -> String?,
    onRequestPasswordReset: suspend (email: String) -> PasswordResetCodeResult,
    onResetPassword: suspend (email: String, code: String, newPassword: String) -> String?,
    onGoogleSignIn: suspend (profile: GoogleAccountProfile) -> String?,
    onAuthenticated: () -> Unit,
    onAdminAuthenticated: () -> Unit,
    onOpenCourier: () -> Unit,
) {
    val context = LocalContext.current
    val googleClient = remember(context) { GoogleAuthUiClient(context) }
    val scope = rememberCoroutineScope()
    var mode by rememberSaveable { mutableStateOf(AuthMode.Login) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordRepeat by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var newPasswordRepeat by rememberSaveable { mutableStateOf("") }
    var enteredCode by rememberSaveable { mutableStateOf("") }
    var resetEmail by rememberSaveable { mutableStateOf("") }
    var resetCode by rememberSaveable { mutableStateOf("") }
    var verificationDebugCode by rememberSaveable { mutableStateOf<String?>(null) }
    var resendNonce by rememberSaveable { mutableStateOf(0) }
    var resendSeconds by rememberSaveable { mutableStateOf(50) }
    var rememberMe by rememberSaveable { mutableStateOf(false) }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isConfirmPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var authError by rememberSaveable { mutableStateOf<String?>(null) }
    var authSuccess by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(mode, resendNonce) {
        if (mode != AuthMode.Verification) return@LaunchedEffect
        resendSeconds = 50
        while (resendSeconds > 0) {
            delay(1_000)
            resendSeconds -= 1
        }
    }

    fun clearMessages() {
        authError = null
        authSuccess = null
    }

    fun goTo(nextMode: AuthMode) {
        mode = nextMode
        clearMessages()
        if (nextMode != AuthMode.Verification) {
            enteredCode = ""
            verificationDebugCode = null
        }
    }

    fun runGoogleSignIn() {
        scope.launch {
            isLoading = true
            clearMessages()
            when (val result = googleClient.signIn()) {
                is GoogleAuthUiResult.Success -> {
                    val error = onGoogleSignIn(result.profile)
                    if (error == null) onAuthenticated() else authError = error
                }
                is GoogleAuthUiResult.Error -> authError = result.message
            }
            isLoading = false
        }
    }

    fun submitLogin() {
        if (email.isBlank() || password.isBlank()) {
            authError = "Enter email and password"
            return
        }
        if (email.trim().equals("admin", ignoreCase = true)) {
            val adminError = onAdminLogin(email, password)
            if (adminError == null) {
                clearMessages()
                onAdminAuthenticated()
            } else {
                authError = adminError
            }
            return
        }
        scope.launch {
            isLoading = true
            clearMessages()
            val error = onLogin(email, password)
            isLoading = false
            if (error == null) onAuthenticated() else authError = error
        }
    }

    fun submitSignUp() {
        clearMessages()
        when {
            name.trim().length < 2 -> authError = "Enter your full name"
            !email.contains("@") -> authError = "Check your email"
            password.length < 8 -> authError = "Password must be at least 8 characters"
            password != passwordRepeat -> authError = "Passwords do not match"
            else -> scope.launch {
                isLoading = true
                val result = onRequestRegistrationVerification(email)
                isLoading = false
                if (result.challenge == null) {
                    authError = result.error ?: "Could not send verification code"
                    return@launch
                }
                enteredCode = ""
                verificationDebugCode = result.challenge.debugCode
                authSuccess = "Verification code sent to your email."
                mode = AuthMode.Verification
                resendNonce += 1
            }
        }
    }

    fun verifySignUp() {
        clearMessages()
        if (enteredCode.length != AuthCodeLength) {
            authError = "Enter the verification code"
            return
        }
        scope.launch {
            isLoading = true
            val verifyError = onVerifyRegistrationCode(email, enteredCode)
            val registerError = if (verifyError == null) onRegister(name, email, password) else verifyError
            val loginError = if (registerError == null) onLogin(email, password) else registerError
            isLoading = false
            if (loginError == null) onAuthenticated() else authError = loginError
        }
    }

    fun requestResetCode() {
        if (!email.contains("@")) {
            authError = "Check your email"
            return
        }
        scope.launch {
            isLoading = true
            clearMessages()
            val result = onRequestPasswordReset(email)
            isLoading = false
            val challenge = result.challenge
            if (challenge != null) {
                resetEmail = challenge.email
                resetCode = ""
                newPassword = ""
                newPasswordRepeat = ""
                authSuccess = "Reset code sent to your email."
                mode = AuthMode.ResetPassword
            } else {
                authError = result.error ?: "Could not send reset code"
            }
        }
    }

    fun changePassword() {
        clearMessages()
        when {
            newPassword.length < 8 -> authError = "Password must be at least 8 characters"
            newPassword != newPasswordRepeat -> authError = "Passwords do not match"
            resetEmail.isBlank() -> authError = "Request a reset code first"
            resetCode.length != AuthCodeLength -> authError = "Enter the reset code from email"
            else -> scope.launch {
                isLoading = true
                val error = onResetPassword(resetEmail, resetCode, newPassword)
                isLoading = false
                if (error == null) onAuthenticated() else authError = error
            }
        }
    }

    when (mode) {
        AuthMode.Login -> LoginContent(
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            passwordVisible = isPasswordVisible,
            onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
            rememberMe = rememberMe,
            onRememberMeChange = { rememberMe = it },
            isLoading = isLoading,
            authError = authError,
            authSuccess = authSuccess,
            onLogin = ::submitLogin,
            onForgotPassword = { goTo(AuthMode.ForgotPassword) },
            onSignUp = { goTo(AuthMode.SignUp) },
            onOpenCourier = onOpenCourier,
            onGoogleSignIn = ::runGoogleSignIn,
        )
        AuthMode.SignUp -> SignUpContent(
            name = name,
            onNameChange = { name = it },
            email = email,
            onEmailChange = { email = it },
            password = password,
            onPasswordChange = { password = it },
            passwordRepeat = passwordRepeat,
            onPasswordRepeatChange = { passwordRepeat = it },
            passwordVisible = isPasswordVisible,
            confirmPasswordVisible = isConfirmPasswordVisible,
            onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
            onConfirmPasswordVisibilityChange = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
            isLoading = isLoading,
            authError = authError,
            authSuccess = authSuccess,
            onSignUp = ::submitSignUp,
            onLogin = { goTo(AuthMode.Login) },
            onOpenCourier = onOpenCourier,
            onGoogleSignIn = ::runGoogleSignIn,
        )
        AuthMode.Verification -> VerificationContent(
            email = email,
            code = enteredCode,
            onCodeChange = { enteredCode = it },
            resendSeconds = resendSeconds,
            isLoading = isLoading,
            authError = authError,
            authSuccess = authSuccess,
            debugCode = verificationDebugCode,
            onVerify = ::verifySignUp,
            onBack = { goTo(AuthMode.SignUp) },
            onResend = {
                scope.launch {
                    isLoading = true
                    clearMessages()
                    val result = onRequestRegistrationVerification(email)
                    isLoading = false
                    if (result.challenge == null) {
                        authError = result.error ?: "Could not send verification code"
                        return@launch
                    }
                    enteredCode = ""
                    verificationDebugCode = result.challenge.debugCode
                    authSuccess = "A new code was sent to your email."
                    resendNonce += 1
                }
            },
        )
        AuthMode.ForgotPassword -> ForgotPasswordContent(
            email = email,
            onEmailChange = { email = it },
            isLoading = isLoading,
            authError = authError,
            authSuccess = authSuccess,
            onSendCode = ::requestResetCode,
            onBack = { goTo(AuthMode.Login) },
        )
        AuthMode.ResetPassword -> ResetPasswordContent(
            code = resetCode,
            onCodeChange = { resetCode = it.filter(Char::isDigit).take(AuthCodeLength) },
            password = newPassword,
            onPasswordChange = { newPassword = it },
            passwordRepeat = newPasswordRepeat,
            onPasswordRepeatChange = { newPasswordRepeat = it },
            passwordVisible = isPasswordVisible,
            confirmPasswordVisible = isConfirmPasswordVisible,
            onPasswordVisibilityChange = { isPasswordVisible = !isPasswordVisible },
            onConfirmPasswordVisibilityChange = { isConfirmPasswordVisible = !isConfirmPasswordVisible },
            isLoading = isLoading,
            authError = authError,
            authSuccess = authSuccess,
            onChangePassword = ::changePassword,
            onBack = { goTo(AuthMode.ForgotPassword) },
        )
    }
}

private enum class AuthMode {
    Login,
    SignUp,
    Verification,
    ForgotPassword,
    ResetPassword,
}

@Composable
private fun LoginContent(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    rememberMe: Boolean,
    onRememberMeChange: (Boolean) -> Unit,
    isLoading: Boolean,
    authError: String?,
    authSuccess: String?,
    onLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onSignUp: () -> Unit,
    onOpenCourier: () -> Unit,
    onGoogleSignIn: () -> Unit,
) {
    AuthFormShell(
        title = "Log In",
        subtitle = "Please sign in to your existing account",
        headerHeight = 170.dp,
    ) {
        AuthInput(
            label = "EMAIL",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "example@mail.com",
            keyboardType = KeyboardType.Email,
        )
        AuthInput(
            label = "PASSWORD",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "********",
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = onPasswordVisibilityChange,
        )
        RememberForgotRow(
            checked = rememberMe,
            onCheckedChange = onRememberMeChange,
            onForgotPassword = onForgotPassword,
        )
        AuthPrimaryButton(
            text = "LOG IN",
            isLoading = isLoading,
            enabled = !isLoading,
            onClick = onLogin,
        )
        AuthMessage(error = authError, success = authSuccess)
        AuthSwitchText(
            prefix = "Don't have an account?",
            action = "SIGN UP",
            onClick = onSignUp,
        )
        CourierAuthLink(onClick = onOpenCourier)
    }
}

@Composable
private fun SignUpContent(
    name: String,
    onNameChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordRepeat: String,
    onPasswordRepeatChange: (String) -> Unit,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onConfirmPasswordVisibilityChange: () -> Unit,
    isLoading: Boolean,
    authError: String?,
    authSuccess: String?,
    onSignUp: () -> Unit,
    onLogin: () -> Unit,
    onOpenCourier: () -> Unit,
    onGoogleSignIn: () -> Unit,
) {
    AuthFormShell(
        title = "Sign Up",
        subtitle = "Create your account to start ordering",
        headerHeight = 150.dp,
        onBack = onLogin,
    ) {
        AuthInput(
            label = "FULL NAME",
            value = name,
            onValueChange = onNameChange,
            placeholder = "Your name",
        )
        AuthInput(
            label = "EMAIL",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "example@mail.com",
            keyboardType = KeyboardType.Email,
        )
        AuthInput(
            label = "PASSWORD",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "********",
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = onPasswordVisibilityChange,
        )
        AuthInput(
            label = "REPEAT PASSWORD",
            value = passwordRepeat,
            onValueChange = onPasswordRepeatChange,
            placeholder = "********",
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePassword = onConfirmPasswordVisibilityChange,
        )
        AuthPrimaryButton(
            text = "SIGN UP",
            isLoading = isLoading,
            enabled = !isLoading,
            onClick = onSignUp,
        )
        AuthMessage(error = authError, success = authSuccess)
        CourierAuthLink(onClick = onOpenCourier)
    }
}

@Composable
private fun VerificationContent(
    email: String,
    code: String,
    onCodeChange: (String) -> Unit,
    resendSeconds: Int,
    isLoading: Boolean,
    authError: String?,
    authSuccess: String?,
    debugCode: String?,
    onVerify: () -> Unit,
    onBack: () -> Unit,
    onResend: () -> Unit,
) {
    AuthFormShell(
        title = "Verification",
        subtitle = "We have sent a code to your email\n$email",
        headerHeight = 126.dp,
        onBack = onBack,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "CODE",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                color = AuthMuted,
            )
            val canResend = resendSeconds <= 0
            Text(
                text = if (canResend) "Resend" else "Resend in.${resendSeconds}sec",
                modifier = Modifier.clickable(enabled = canResend, onClick = onResend),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (canResend) TextDecoration.Underline else TextDecoration.None,
                ),
                color = if (canResend) AuthOrange else AuthText,
            )
        }
        OtpInput(
            code = code,
            onCodeChange = onCodeChange,
        )
        AuthPrimaryButton(
            text = "VERIFY",
            isLoading = isLoading,
            enabled = !isLoading && code.length == AuthCodeLength,
            onClick = onVerify,
        )
        AuthMessage(error = authError, success = authSuccess)
        debugCode?.let { value ->
            Text(
                text = "Verification code: $value",
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.sp,
                ),
                color = AuthMuted,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ForgotPasswordContent(
    email: String,
    onEmailChange: (String) -> Unit,
    isLoading: Boolean,
    authError: String?,
    authSuccess: String?,
    onSendCode: () -> Unit,
    onBack: () -> Unit,
) {
    AuthFormShell(
        title = "Forgot Password",
        subtitle = "Please sign in to your existing account",
        headerHeight = 168.dp,
        onBack = onBack,
    ) {
        AuthInput(
            label = "EMAIL",
            value = email,
            onValueChange = onEmailChange,
            placeholder = "example@mail.com",
            keyboardType = KeyboardType.Email,
        )
        Spacer(modifier = Modifier.height(6.dp))
        AuthPrimaryButton(
            text = "SEND CODE",
            isLoading = isLoading,
            enabled = !isLoading,
            onClick = onSendCode,
        )
        AuthMessage(error = authError, success = authSuccess)
    }
}

@Composable
private fun ResetPasswordContent(
    code: String,
    onCodeChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordRepeat: String,
    onPasswordRepeatChange: (String) -> Unit,
    passwordVisible: Boolean,
    confirmPasswordVisible: Boolean,
    onPasswordVisibilityChange: () -> Unit,
    onConfirmPasswordVisibilityChange: () -> Unit,
    isLoading: Boolean,
    authError: String?,
    authSuccess: String?,
    onChangePassword: () -> Unit,
    onBack: () -> Unit,
) {
    AuthFormShell(
        title = "New Password",
        subtitle = "Create a new password for your account",
        headerHeight = 168.dp,
        onBack = onBack,
    ) {
        AuthInput(
            label = "EMAIL CODE",
            value = code,
            onValueChange = { value -> onCodeChange(value.filter(Char::isDigit).take(AuthCodeLength)) },
            placeholder = "000000",
            keyboardType = KeyboardType.NumberPassword,
        )
        AuthInput(
            label = "NEW PASSWORD",
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "********",
            isPassword = true,
            passwordVisible = passwordVisible,
            onTogglePassword = onPasswordVisibilityChange,
        )
        AuthInput(
            label = "REPEAT PASSWORD",
            value = passwordRepeat,
            onValueChange = onPasswordRepeatChange,
            placeholder = "********",
            isPassword = true,
            passwordVisible = confirmPasswordVisible,
            onTogglePassword = onConfirmPasswordVisibilityChange,
        )
        AuthPrimaryButton(
            text = "CHANGE PASSWORD",
            isLoading = isLoading,
            enabled = !isLoading,
            onClick = onChangePassword,
        )
        AuthMessage(error = authError, success = authSuccess)
    }
}

@Composable
private fun AuthFormShell(
    title: String,
    subtitle: String,
    headerHeight: androidx.compose.ui.unit.Dp,
    onBack: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthNight)
            .imePadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight),
        ) {
            AuthHeaderDecoration()
            onBack?.let { back ->
                Box(
                    modifier = Modifier
                        .padding(start = 18.dp, top = 28.dp)
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(CardWhite)
                        .clickable(onClick = back),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = AuthText,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.sp,
                    ),
                    color = CardWhite,
                    textAlign = TextAlign.Center,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = CardWhite.copy(alpha = 0.78f),
                    textAlign = TextAlign.Center,
                )
            }
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            color = CardWhite,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = content,
            )
        }
    }
}

@Composable
private fun AuthHeaderDecoration() {
    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
        val center = Offset(-18f, 0f)
        repeat(24) { index ->
            val angle = (index * 9f - 20f) * (kotlin.math.PI.toFloat() / 180f)
            val end = Offset(
                x = center.x + kotlin.math.cos(angle) * 160f,
                y = center.y + kotlin.math.sin(angle) * 160f,
            )
            drawLine(
                color = CardWhite.copy(alpha = 0.055f),
                start = center,
                end = end,
                strokeWidth = 3f,
                cap = StrokeCap.Round,
            )
        }
        drawArc(
            color = AuthOrange.copy(alpha = 0.28f),
            startAngle = -40f,
            sweepAngle = 64f,
            useCenter = false,
            topLeft = Offset(size.width * 0.92f, size.height * 0.52f),
            size = Size(size.width * 0.34f, size.width * 0.34f),
            style = Stroke(width = 4f, cap = StrokeCap.Round),
        )
        drawArc(
            color = CardWhite.copy(alpha = 0.06f),
            startAngle = 220f,
            sweepAngle = 80f,
            useCenter = false,
            topLeft = Offset(size.width * 0.62f, -size.width * 0.18f),
            size = Size(size.width * 0.55f, size.width * 0.55f),
            style = Stroke(width = 3f, cap = StrokeCap.Round),
        )
    }
}

@Composable
private fun AuthInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            ),
            color = AuthMuted,
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(8.dp),
            singleLine = true,
            textStyle = TextStyle(
                color = AuthText,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = ImeAction.Next,
            ),
            visualTransformation = if (isPassword && !passwordVisible) {
                PasswordVisualTransformation()
            } else {
                VisualTransformation.None
            },
            placeholder = {
                Text(
                    text = placeholder,
                    style = TextStyle(
                        color = AuthPlaceholder,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                    ),
                )
            },
            trailingIcon = if (isPassword && onTogglePassword != null) {
                {
                    IconButton(onClick = onTogglePassword) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = AuthPlaceholder,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            } else {
                null
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = AuthField,
                unfocusedContainerColor = AuthField,
                disabledContainerColor = AuthField,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedTextColor = AuthText,
                unfocusedTextColor = AuthText,
                cursorColor = AuthOrange,
            ),
        )
    }
}

@Composable
private fun RememberForgotRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.clickable { onCheckedChange(!checked) },
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(15.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (checked) AuthOrange else Color.Transparent)
                    .border(
                        width = 1.dp,
                        color = if (checked) AuthOrange else AuthBorder,
                        shape = RoundedCornerShape(3.dp),
                    ),
            )
            Text(
                text = "Remember me",
                style = MaterialTheme.typography.bodySmall,
                color = AuthMuted,
            )
        }
        Text(
            text = "Forgot Password",
            modifier = Modifier.clickable(onClick = onForgotPassword),
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
            color = AuthOrange,
        )
    }
}

@Composable
private fun AuthPrimaryButton(
    text: String,
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(58.dp),
        shape = RoundedCornerShape(7.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AuthOrange,
            contentColor = CardWhite,
            disabledContainerColor = AuthOrange.copy(alpha = 0.55f),
            disabledContentColor = CardWhite,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 2.dp,
                color = CardWhite,
            )
        } else {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.sp,
                ),
            )
        }
    }
}

@Composable
private fun AuthSwitchText(
    prefix: String,
    action: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "$prefix ",
            style = MaterialTheme.typography.bodyMedium,
            color = AuthMuted,
        )
        Text(
            text = action,
            modifier = Modifier.clickable(onClick = onClick),
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            ),
            color = AuthOrange,
        )
    }
}

@Composable
private fun CourierAuthLink(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Войти как курьер",
            modifier = Modifier.clickable(onClick = onClick),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.sp,
            ),
            color = AuthOrange,
        )
    }
}

@Composable
private fun SocialAuthRow(
    enabled: Boolean,
    onGoogleSignIn: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = "Or",
            style = MaterialTheme.typography.bodyMedium,
            color = AuthMuted,
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(22.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SocialCircle(label = "f", background = Color(0xFF395998), enabled = false, onClick = {})
            SocialCircle(label = "G", background = Color(0xFF1DA1F2), enabled = enabled, onClick = onGoogleSignIn)
            SocialCircle(label = "a", background = AuthDarkCircle, enabled = false, onClick = {})
        }
    }
}

@Composable
private fun SocialCircle(
    label: String,
    background: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = CardWhite,
        )
    }
}

@Composable
private fun OtpInput(
    code: String,
    onCodeChange: (String) -> Unit,
) {
    BasicTextField(
        value = code,
        onValueChange = { value -> onCodeChange(value.filter(Char::isDigit).take(AuthCodeLength)) },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        textStyle = TextStyle(color = Color.Transparent),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.NumberPassword,
            imeAction = ImeAction.Done,
        ),
        decorationBox = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                repeat(AuthCodeLength) { index ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 3.dp)
                            .height(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AuthField),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            text = code.getOrNull(index)?.toString().orEmpty(),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = AuthText,
                        )
                    }
                }
            }
        },
    )
}

@Composable
private fun AuthMessage(
    error: String?,
    success: String?,
) {
    val message = error ?: success ?: return
    Text(
        text = message,
        modifier = Modifier.fillMaxWidth(),
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = if (error != null) Rose else Success,
        textAlign = TextAlign.Center,
    )
}

private const val AuthCodeLength = 6

private val AuthNight = Color(0xFF111124)
private val AuthField = Color(0xFFF0F4F8)
private val AuthText = Color(0xFF32343E)
private val AuthMuted = Color(0xFF6B6E82)
private val AuthPlaceholder = Color(0xFFA0A5B2)
private val AuthBorder = Color(0xFFDCE3EC)
private val AuthDarkCircle = Color(0xFF171A22)
private val AuthOrange = Color(0xFFFF7622)

@Composable
private fun LegacyAuthScreen(
    onLogin: suspend (email: String, password: String) -> String?,
    onRegister: suspend (name: String, email: String, password: String) -> String?,
    onAuthenticated: () -> Unit,
) {
    val strings = LocalAppStrings.current
    var isSignUp by rememberSaveable { mutableStateOf(false) }
    var name by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("hello@foodly.app") }
    var password by rememberSaveable { mutableStateOf("12345678") }
    var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var isLoading by rememberSaveable { mutableStateOf(false) }
    var authError by rememberSaveable { mutableStateOf<String?>(null) }
    var authSuccess by rememberSaveable { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun submitAuth() {
        scope.launch {
            isLoading = true
            authError = null
            authSuccess = null
            val error = if (isSignUp) {
                onRegister(name, email, password)
            } else {
                onLogin(email, password)
            }
            isLoading = false
            if (error == null) {
                if (isSignUp) {
                    isSignUp = false
                    name = ""
                    authSuccess = strings.signUpSuccess(WELCOME_COUPON_CODE)
                } else {
                    onAuthenticated()
                }
            } else {
                authError = error
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Night)
            .imePadding(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            BrandHeaderChip()
            Text(
                text = if (isSignUp) strings.signUpTitle else strings.loginTitle,
                style = MaterialTheme.typography.headlineLarge,
                color = CardWhite,
            )
            Text(
                text = strings.authSubtitle,
                style = MaterialTheme.typography.bodyLarge,
                color = CardWhite.copy(alpha = 0.72f),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                SmallMetric("25 мин", strings.averageDelivery)
                SmallMetric("4.9", strings.rating)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(topStart = 36.dp, topEnd = 36.dp),
            colors = CardDefaults.cardColors(containerColor = CardWhite),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                ToggleRow(
                    isPrimary = isSignUp,
                    firstLabel = strings.loginTab,
                    secondLabel = strings.signUpTab,
                    onFirst = {
                        isSignUp = false
                        authError = null
                        authSuccess = null
                    },
                    onSecond = {
                        isSignUp = true
                        authError = null
                        authSuccess = null
                    },
                )
                if (isSignUp) {
                    AppTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = strings.nameLabel,
                        placeholder = strings.namePlaceholder,
                    )
                }
                AppTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = strings.emailLabel,
                    placeholder = strings.emailPlaceholder,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                )
                AppTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = strings.passwordLabel,
                    placeholder = strings.passwordPlaceholder,
                    isPassword = !isPasswordVisible,
                    trailing = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = InkSoft,
                            )
                        }
                    },
                )
                PrimaryButton(
                    text = when {
                        isLoading && isSignUp -> strings.creating
                        isLoading -> strings.signingIn
                        isSignUp -> strings.createAccount
                        else -> strings.signIn
                    },
                    onClick = ::submitAuth,
                    enabled = !isLoading,
                )
                authError?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Rose,
                        textAlign = TextAlign.Center,
                    )
                }
                authSuccess?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Success,
                        textAlign = TextAlign.Center,
                    )
                }
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    onClick = {
                        isSignUp = !isSignUp
                        authError = null
                        authSuccess = null
                    },
                ) {
                    Text(
                        text = if (isSignUp) strings.haveAccount else strings.needAccount,
                        color = Orange,
                    )
                }
                Text(
                    text = strings.demoAccount,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

@Composable
fun LocationIntroScreen(
    onLocationConfirmed: (DeliveryAddress) -> Unit,
    onPickManually: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val language = LocalAppLanguage.current
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var requestNonce by rememberSaveable { mutableStateOf(0) }
    var statusText by rememberSaveable(language) { mutableStateOf(strings.locationStatusRequest) }
    var previewAddress by remember { mutableStateOf<DeliveryAddress?>(null) }
    var isLocating by rememberSaveable { mutableStateOf(false) }
    var finished by rememberSaveable { mutableStateOf(false) }

    fun requestLocation() {
        if (finished.not()) {
            requestNonce += 1
        }
    }

    fun finishWithPoint(point: GeoPoint) {
        if (finished) return
        previewAddress = point.asCurrentLocationAddress(strings)
        statusText = strings.locationFoundSaving
        finished = true
        isLocating = true
        scope.launch {
            val resolvedAddress = when (val result = YandexGeocodingApi.reverseGeocode(point, language.yandexGeocoderLanguage())) {
                is BackendResult.Success -> result.data
                is BackendResult.Error -> null
            }
            val address = point.asCurrentLocationAddress(strings, resolvedAddress)
            previewAddress = address
            delay(850)
            isLocating = false
            onLocationConfirmed(address)
        }
    }

    fun handleLocationError(message: String) {
        if (finished) return
        isLocating = false
        statusText = message
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            requestLocation()
        } else {
            handleLocationError(language.locationPermissionDeniedMessage())
        }
    }

    val settingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
    ) {
        requestLocation()
    }

    LaunchedEffect(Unit) {
        requestLocation()
    }

    LaunchedEffect(requestNonce) {
        if (requestNonce == 0 || finished) return@LaunchedEffect
        when {
            hasLocationPermission(context).not() -> {
                statusText = language.locationPermissionHintMessage()
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION,
                    ),
                )
            }
            isLocationEnabled(context).not() -> {
                statusText = language.locationDisabledMessage()
                settingsLauncher.launch(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            else -> {
                isLocating = true
                statusText = language.locationDetectingMessage()
                requestCurrentGeoPoint(
                    context = context,
                    language = language,
                    onPoint = { point ->
                        finishWithPoint(point)
                    },
                    onError = ::handleLocationError,
                )
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        val address = previewAddress
        if (address == null) {
            LocationIllustration()
        } else {
            YandexMapPicker(
                selectedPoint = address.point,
                onPointSelected = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
            )
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = strings.locationTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        PrimaryButton(
            text = if (isLocating) strings.locating else strings.enableLocation,
            onClick = ::requestLocation,
            enabled = !isLocating && !finished,
        )
        TextButton(onClick = onPickManually, enabled = !finished) {
            Text(strings.pickOnMap, color = Orange)
        }
    }
}

@Composable
private fun LocationIllustration() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            drawRoundRect(
                color = OrangeSoft,
                size = Size(size.width * 0.88f, size.height * 0.88f),
                topLeft = Offset(size.width * 0.06f, size.height * 0.06f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(90f, 90f),
            )
            drawLine(
                color = Gold,
                strokeWidth = 18f,
                cap = StrokeCap.Round,
                start = Offset(size.width * 0.2f, size.height * 0.24f),
                end = Offset(size.width * 0.78f, size.height * 0.78f),
            )
            drawLine(
                color = Gold.copy(alpha = 0.7f),
                strokeWidth = 18f,
                cap = StrokeCap.Round,
                start = Offset(size.width * 0.28f, size.height * 0.8f),
                end = Offset(size.width * 0.7f, size.height * 0.18f),
            )
            drawCircle(
                color = Orange,
                radius = 54f,
                center = Offset(size.width * 0.5f, size.height * 0.5f),
            )
            drawCircle(
                color = CardWhite,
                radius = 18f,
                center = Offset(size.width * 0.5f, size.height * 0.5f),
            )
        }
    }
}

private fun GeoPoint.asCurrentLocationAddress(
    strings: com.example.fooddeliveryapp.ui.AppStrings,
    resolvedAddress: com.example.fooddeliveryapp.backend.YandexGeocodedAddress? = null,
): DeliveryAddress {
    val fallbackStreet = SampleData.streetNameForPoint(this, strings.currentLanguage)
    val title = resolvedAddress?.primaryAddressLine
        ?.takeIf { it.isNotBlank() && it != strings.selectedMapPoint }
        ?: fallbackStreet
    return DeliveryAddress(
        label = AddressLabelHome,
        title = title,
        subtitle = resolvedAddress?.formattedAddress?.ifBlank {
            "$fallbackStreet - ${strings.pointLabel(latitude, longitude)}"
        } ?: "$fallbackStreet - ${strings.pointLabel(latitude, longitude)}",
        point = this,
    )
}

private fun hasLocationPermission(context: Context): Boolean =
    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

private fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
}

private fun AppLanguage.locationPermissionDeniedMessage(): String = when (this) {
    AppLanguage.English -> "Location access was denied. You can allow it or choose the address manually on the map."
    AppLanguage.Russian -> "Доступ к геолокации не выдан. Можно включить его или выбрать адрес на карте вручную."
    AppLanguage.Uzbek -> "Geolokatsiyaga ruxsat berilmadi. Ruxsat bering yoki manzilni xaritada qo'lda tanlang."
}

private fun AppLanguage.locationPermissionHintMessage(): String = when (this) {
    AppLanguage.English -> "Allow location access so we can find your delivery address automatically."
    AppLanguage.Russian -> "Разреши доступ к локации, чтобы мы автоматически нашли адрес доставки."
    AppLanguage.Uzbek -> "Yetkazish manzilini avtomatik topishimiz uchun geolokatsiyaga ruxsat bering."
}

private fun AppLanguage.locationDisabledMessage(): String = when (this) {
    AppLanguage.English -> "Location is turned off. Open settings and enable it."
    AppLanguage.Russian -> "Геолокация выключена. Открой настройки и включи ее."
    AppLanguage.Uzbek -> "Geolokatsiya o'chirilgan. Sozlamalarni ochib, uni yoqing."
}

private fun AppLanguage.locationDetectingMessage(): String = when (this) {
    AppLanguage.English -> "Detecting your current location..."
    AppLanguage.Russian -> "Определяем текущую точку..."
    AppLanguage.Uzbek -> "Joriy joylashuv aniqlanmoqda..."
}

@SuppressLint("MissingPermission")
private fun requestCurrentGeoPoint(
    context: Context,
    language: AppLanguage,
    onPoint: (GeoPoint) -> Unit,
    onError: (String) -> Unit,
) {
    if (hasLocationPermission(context).not()) {
        onError(language.locationPermissionDeniedMessage())
        return
    }

    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val providers = listOf(
        LocationManager.GPS_PROVIDER,
        LocationManager.NETWORK_PROVIDER,
        LocationManager.PASSIVE_PROVIDER,
    ).filter { provider ->
        runCatching { locationManager.isProviderEnabled(provider) }.getOrDefault(false)
    }
    if (providers.isEmpty()) {
        onError(language.locationDisabledMessage())
        return
    }

    val lastKnown = providers
        .mapNotNull { provider -> runCatching { locationManager.getLastKnownLocation(provider) }.getOrNull() }
        .maxByOrNull(Location::getTime)
    val handler = Handler(Looper.getMainLooper())
    var delivered = false
    fun deliver(location: Location) {
        if (delivered) return
        delivered = true
        onPoint(GeoPoint(location.latitude, location.longitude))
    }

    val timeout = Runnable {
        if (delivered) return@Runnable
        val fallback = lastKnown
        if (fallback != null) {
            deliver(fallback)
        } else {
            delivered = true
            onError("Не удалось получить координаты. Попробуй еще раз или выбери адрес на карте.")
        }
    }
    handler.postDelayed(timeout, 4_000L)

    val provider = providers.firstOrNull { it == LocationManager.GPS_PROVIDER }
        ?: providers.first()
    val listener = LocationListener { location ->
        handler.removeCallbacks(timeout)
        deliver(location)
    }

    runCatching {
        locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
    }.onFailure {
        handler.removeCallbacks(timeout)
        if (lastKnown != null) {
            deliver(lastKnown)
        } else {
            delivered = true
            onError("Не удалось запустить поиск локации. Выбери адрес на карте вручную.")
        }
    }
}

@Composable
fun LocationIntroScreen(onContinue: () -> Unit) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Cream)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
            contentAlignment = Alignment.Center,
        ) {
            androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = OrangeSoft,
                    size = Size(size.width * 0.88f, size.height * 0.88f),
                    topLeft = Offset(size.width * 0.06f, size.height * 0.06f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(90f, 90f),
                )
                drawLine(
                    color = Gold,
                    strokeWidth = 18f,
                    cap = StrokeCap.Round,
                    start = Offset(size.width * 0.2f, size.height * 0.24f),
                    end = Offset(size.width * 0.78f, size.height * 0.78f),
                )
                drawLine(
                    color = Gold.copy(alpha = 0.7f),
                    strokeWidth = 18f,
                    cap = StrokeCap.Round,
                    start = Offset(size.width * 0.28f, size.height * 0.8f),
                    end = Offset(size.width * 0.7f, size.height * 0.18f),
                )
                drawCircle(
                    color = Orange,
                    radius = 54f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                )
                drawCircle(
                    color = CardWhite,
                    radius = 18f,
                    center = Offset(size.width * 0.5f, size.height * 0.5f),
                )
            }
        }
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = strings.mapTitle,
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = strings.mapDescription,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(28.dp))
        PrimaryButton(
            text = strings.pickOnMap,
            onClick = onContinue,
        )
    }
}

@Composable
private fun EmojiIllustration(
    emoji: String,
    accent: Color,
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center,
    ) {
        val size = maxWidth
        androidx.compose.foundation.Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(accent.copy(alpha = 0.26f), Color.Transparent),
                ),
                radius = size.toPx() * 0.38f,
            )
            drawArc(
                color = accent.copy(alpha = 0.24f),
                startAngle = -30f,
                sweepAngle = 120f,
                useCenter = false,
                topLeft = Offset(size.toPx() * 0.18f, size.toPx() * 0.16f),
                size = Size(size.toPx() * 0.62f, size.toPx() * 0.62f),
                style = Stroke(width = 26f, cap = StrokeCap.Round),
            )
        }
        Box(
            modifier = Modifier
                .size(size * 0.54f)
                .clip(RoundedCornerShape(36.dp))
                .background(
                    Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.24f), CardWhite),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = MaterialTheme.typography.displayLarge.fontSize * 2.2),
            )
        }
    }
}
