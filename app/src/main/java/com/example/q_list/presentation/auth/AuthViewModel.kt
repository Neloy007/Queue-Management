
package com.example.q_list.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.q_list.data.model.UserRole
import com.example.q_list.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val authenticatedRole: UserRole? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    // LOGIN
    fun login(
        email: String,
        password: String,
        expectedRole: UserRole
    ) {
        if (email.isBlank() || password.isBlank()) {
            _uiState.value = AuthUiState(
                error = "Please enter your email and password."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)

            try {
                val role = repository.login(
                    email.trim(),
                    password,
                    expectedRole
                )

                _uiState.value = AuthUiState(
                    authenticatedRole = role,
                    message = "Login successful."
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    error = e.message ?: "Login failed."
                )
            }
        }
    }

    // CUSTOMER REGISTRATION
    fun registerCustomer(
        name: String,
        email: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            name.isBlank() ->
                return showError("Please enter your full name.")

            !isValidEmail(email) ->
                return showError("Please enter a valid email address.")

            password.length < 6 ->
                return showError("Password must contain at least 6 characters.")

            password != confirmPassword ->
                return showError("Passwords do not match.")
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)

            try {
                repository.registerCustomer(
                    name.trim(),
                    email.trim(),
                    password
                )

                _uiState.value = AuthUiState(
                    authenticatedRole = UserRole.CUSTOMER,
                    message = "Account created successfully."
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    error = e.message ?: "Registration failed."
                )
            }
        }
    }

    // OPERATOR REGISTRATION
    fun registerOperator(
        name: String,
        organizationName: String,
        email: String,
        location: String,
        password: String,
        confirmPassword: String
    ) {
        when {
            name.isBlank() ->
                return showError("Please enter your full name.")

            organizationName.isBlank() ->
                return showError("Please enter your company or service name.")

            !isValidEmail(email) ->
                return showError("Please enter a valid email address.")

            location.isBlank() ->
                return showError("Please enter your location.")

            password.length < 6 ->
                return showError("Password must contain at least 6 characters.")

            password != confirmPassword ->
                return showError("Passwords do not match.")
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)

            try {
                repository.registerOperator(
                    name = name.trim(),
                    organizationName = organizationName.trim(),
                    email = email.trim(),
                    location = location.trim(),
                    password = password
                )

                _uiState.value = AuthUiState(
                    message = "Registration submitted successfully. Your operator account is awaiting approval."
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    error = e.message ?: "Operator registration failed."
                )
            }
        }
    }

    // FORGOT PASSWORD
    fun resetPassword(email: String) {
        if (!isValidEmail(email)) {
            showError("Please enter a valid email address.")
            return
        }

        viewModelScope.launch {
            _uiState.value = AuthUiState(loading = true)

            try {
                repository.resetPassword(email.trim())

                _uiState.value = AuthUiState(
                    message = "Password reset email sent. Check your inbox."
                )

            } catch (e: Exception) {
                _uiState.value = AuthUiState(
                    error = e.message ?: "Could not send reset email."
                )
            }
        }
    }

    // EXISTING SESSION
    fun checkExistingSession(
        onResult: (UserRole?) -> Unit
    ) {
        repository.getCurrentUserRole(onResult)
    }

    // LOGOUT
    fun logout() {
        repository.logout()
        _uiState.value = AuthUiState()
    }

    // CLEAR MESSAGES
    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            message = null
        )
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS
            .matcher(email.trim())
            .matches()
    }

    private fun showError(message: String) {
        _uiState.value = AuthUiState(error = message)
    }
}
