package com.ninety5.habitate.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.rounded.Facebook
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ninety5.habitate.R
import com.ninety5.habitate.ui.components.HabitateLogo
import com.ninety5.habitate.ui.theme.*

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToOnboarding: () -> Unit,
    onNavigateToVerifyEmail: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusManager = LocalFocusManager.current
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }

    val credentialManager = remember { CredentialManager.create(context) }

    fun signInWithGoogle() {
        coroutineScope.launch {
            try {
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(com.ninety5.habitate.BuildConfig.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(true)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
                    viewModel.handleGoogleSignIn(credential.idToken)
                } else {
                    // Handle other credential types if needed
                }
            } catch (e: Exception) {
                // Handle error
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(uiState.isLoggedIn, uiState.isOnboarded, uiState.isEmailVerified) {
        if (uiState.isLoggedIn) {
            if (!uiState.isEmailVerified) {
                onNavigateToVerifyEmail()
            } else if (uiState.isOnboarded) {
                onLoginSuccess()
            } else {
                onNavigateToOnboarding()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = ReferenceColors.lightGradient
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(RefScreenPadding.dp),
            shape = RoundedCornerShape(RefRadiusLG.dp),
            colors = CardDefaults.cardColors(
                containerColor = ReferenceColors.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = RefCardElevation.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(RefCardPadding.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.ic_habitate_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(RefLogoSize.dp)
                )
                
                Spacer(modifier = Modifier.height(RefSpacingMD.dp))

                Text(
                    text = "Login",
                    fontSize = RefTextSizeXL.sp,
                    fontWeight = FontWeight.Bold,
                    color = ReferenceColors.textPrimary
                )

                Text(
                    text = "Enter your email and password to log in",
                    fontSize = RefTextSizeMD.sp,
                    color = ReferenceColors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = RefSpacingSM.dp, bottom = RefSpacingLG.dp)
                )

                // Email Field
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = { Text("your@email.com", color = ReferenceColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(RefRadiusSM.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ReferenceColors.surface,
                        unfocusedContainerColor = ReferenceColors.surface,
                        focusedBorderColor = ReferenceColors.border,
                        unfocusedBorderColor = ReferenceColors.border,
                        focusedPlaceholderColor = ReferenceColors.textSecondary,
                        unfocusedPlaceholderColor = ReferenceColors.textSecondary
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    )
                )

                Spacer(modifier = Modifier.height(RefSpacingMD.dp))

                // Password Field
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = { Text("*******", color = ReferenceColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(RefRadiusSM.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = ReferenceColors.surface,
                        unfocusedContainerColor = ReferenceColors.surface,
                        focusedBorderColor = ReferenceColors.border,
                        unfocusedBorderColor = ReferenceColors.border,
                        focusedPlaceholderColor = ReferenceColors.textSecondary,
                        unfocusedPlaceholderColor = ReferenceColors.textSecondary
                    ),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password",
                                tint = ReferenceColors.textSecondary
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.login(email, password)
                        }
                    )
                )

                // Remember Me & Forgot Password
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = RefSpacingMD.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = ReferenceColors.accent,
                                uncheckedColor = ReferenceColors.textSecondary
                            )
                        )
                        Text(
                            text = "Remember me",
                            fontSize = RefTextSizeSM.sp,
                            color = ReferenceColors.textSecondary
                        )
                    }

                    Text(
                        text = "Forgot Password ?",
                        fontSize = RefTextSizeSM.sp,
                        color = ReferenceColors.accent,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable { onNavigateToForgotPassword() }
                    )
                }

                // Login Button
                Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(RefButtonHeight.dp),
                    shape = RoundedCornerShape(RefRadiusSM.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ReferenceColors.accent
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = RefButtonElevation.dp
                    )
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = ReferenceColors.onAccent
                        )
                    } else {
                        Text(
                            text = "Log In",
                            fontSize = RefTextSizeLG.sp,
                            fontWeight = FontWeight.Bold,
                            color = ReferenceColors.onAccent
                        )
                    }
                }

                // Or login with
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = RefSpacingLG.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier = Modifier.weight(1f), 
                        color = ReferenceColors.divider
                    )
                    Text(
                        text = "Or login with",
                        fontSize = RefTextSizeSM.sp,
                        color = ReferenceColors.textSecondary,
                        modifier = Modifier.padding(horizontal = RefSpacingMD.dp)
                    )
                    HorizontalDivider(
                        modifier = Modifier.weight(1f), 
                        color = ReferenceColors.divider
                    )
                }

                // Social Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    SocialButton(
                        icon = { 
                            // Google Icon placeholder - using text G for now or a colored box
                            Text("G", fontWeight = FontWeight.Bold, color = ReferenceColors.googleRed)
                        },
                        onClick = { signInWithGoogle() }
                    )
                }

                Spacer(modifier = Modifier.height(RefSpacingLG.dp))

                // Sign Up
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Don't have an account? ",
                        fontSize = RefTextSizeSM.sp,
                        color = ReferenceColors.textSecondary
                    )
                    Text(
                        text = "Sign Up",
                        fontSize = RefTextSizeSM.sp,
                        color = ReferenceColors.accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToRegister() }
                    )
                }
            }
        }
    }
}

@Composable
fun SocialButton(
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(RefRadiusMD.dp),
        color = ReferenceColors.socialButtonBg,
        modifier = Modifier.size(RefSocialButtonSize.dp),
        shadowElevation = RefSocialButtonElevation.dp
    ) {
        Box(contentAlignment = Alignment.Center) {
            icon()
        }
    }
}

