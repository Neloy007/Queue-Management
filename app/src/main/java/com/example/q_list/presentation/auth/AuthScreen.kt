
package com.example.q_list.presentation.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.q_list.data.model.UserRole

@Composable
fun AuthScreen(
    role: UserRole,
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onAuthenticated: (UserRole) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    var isRegistering by rememberSaveable {
        mutableStateOf(false)
    }

    var isForgotPassword by rememberSaveable {
        mutableStateOf(false)
    }

    var name by rememberSaveable { mutableStateOf("") }
    var organizationName by rememberSaveable { mutableStateOf("") }
    var location by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    val isCustomer = role == UserRole.CUSTOMER

    LaunchedEffect(uiState.authenticatedRole) {
        uiState.authenticatedRole?.let {
            onAuthenticated(it)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Q-List",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                isForgotPassword -> "Reset your password"
                isRegistering && isCustomer -> "Create Customer Account"
                isRegistering -> "Register Your Organization"
                isCustomer -> "Customer Login"
                else -> "Operator Login"
            },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when {
                isForgotPassword ->
                    "Enter your email to receive a password reset link."

                isRegistering && isCustomer ->
                    "Create an account to join queues easily."

                isRegistering ->
                    "Register your company, shop, or service to manage queues."

                isCustomer ->
                    "Sign in to manage your queue and appointments."

                else ->
                    "Sign in to manage your organization's queues."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(28.dp))

        // FULL NAME
        if (isRegistering) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Full Name") },
                singleLine = true,
                enabled = !uiState.loading,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // ORGANIZATION NAME - OPERATOR ONLY
        if (!isCustomer && isRegistering) {
            OutlinedTextField(
                value = organizationName,
                onValueChange = { organizationName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Company / Shop / Service Name") },
                placeholder = { Text("Enter your organization name") },
                singleLine = true,
                enabled = !uiState.loading
            )

            Spacer(modifier = Modifier.height(16.dp))

            // LOCATION
            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Location") },
                placeholder = { Text("Example: Dhanmondi, Dhaka") },
                enabled = !uiState.loading,
                minLines = 2,
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // EMAIL
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email Address") },
            placeholder = { Text("example@email.com") },
            singleLine = true,
            enabled = !uiState.loading,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // PASSWORD
        if (!isForgotPassword) {
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Password") },
                singleLine = true,
                enabled = !uiState.loading,
                visualTransformation = if (passwordVisible) {
                    VisualTransformation.None
                } else {
                    PasswordVisualTransformation()
                },
                trailingIcon = {
                    TextButton(
                        onClick = {
                            passwordVisible = !passwordVisible
                        }
                    ) {
                        Text(if (passwordVisible) "Hide" else "Show")
                    }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // CONFIRM PASSWORD
        if (isRegistering && !isForgotPassword) {
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirm Password") },
                singleLine = true,
                enabled = !uiState.loading,
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password
                )
            )

            Spacer(modifier = Modifier.height(16.dp))
        }

        // FORGOT PASSWORD
        if (!isRegistering && !isForgotPassword) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = {
                        viewModel.clearMessages()
                        isForgotPassword = true
                    }
                ) {
                    Text("Forgot Password?")
                }
            }
        }

        // ERROR MESSAGE
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // SUCCESS MESSAGE
        uiState.message?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))
        }

        // MAIN BUTTON
        Button(
            onClick = {
                when {
                    isForgotPassword -> {
                        viewModel.resetPassword(email.trim())
                    }

                    isRegistering && isCustomer -> {
                        viewModel.registerCustomer(
                            name = name,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword
                        )
                    }

                    isRegistering -> {
                        viewModel.registerOperator(
                            name = name,
                            organizationName = organizationName,
                            email = email,
                            location = location,
                            password = password,
                            confirmPassword = confirmPassword
                        )
                    }

                    else -> {
                        viewModel.login(
                            email = email.trim(),
                            password = password,
                            expectedRole = role
                        )
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            enabled = !uiState.loading,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            if (uiState.loading) {
                CircularProgressIndicator(
                    modifier = Modifier.height(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = when {
                        isForgotPassword -> "Send Reset Link"
                        isRegistering -> "Create Account"
                        else -> "Login"
                    },
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // REGISTER / LOGIN SWITCH - BOTH ROLES
        if (!isForgotPassword) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isRegistering) {
                        "Already have an account?"
                    } else {
                        "Don't have an account?"
                    },
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                TextButton(
                    onClick = {
                        viewModel.clearMessages()
                        isRegistering = !isRegistering

                        name = ""
                        organizationName = ""
                        location = ""
                        email = ""
                        password = ""
                        confirmPassword = ""
                        passwordVisible = false
                    },
                    enabled = !uiState.loading
                ) {
                    Text(
                        if (isRegistering) "Login" else "Register"
                    )
                }
            }
        }

        // BACK TO LOGIN FROM FORGOT PASSWORD
        if (isForgotPassword) {
            TextButton(
                onClick = {
                    viewModel.clearMessages()
                    isForgotPassword = false
                }
            ) {
                Text("Back to Login")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = {
                viewModel.clearMessages()
                onBack()
            },
            enabled = !uiState.loading
        ) {
            Text("Back")
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
