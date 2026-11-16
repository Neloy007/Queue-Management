package com.example.q_list.presentation.operator


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.q_list.data.model.QueueSession
import com.example.q_list.data.repository.OperatorDashboardRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OperatorDashboardUiState(
    val loading: Boolean = true,
    val actionLoading: Boolean = false,
    val operatorName: String = "",
    val organizationName: String = "",
    val location: String = "",
    val activeSession: QueueSession? = null,
    val error: String? = null,
    val message: String? = null
)

class OperatorDashboardViewModel(
    private val repository: OperatorDashboardRepository =
        OperatorDashboardRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorDashboardUiState())
    val uiState: StateFlow<OperatorDashboardUiState> =
        _uiState.asStateFlow()

    private var listeners: List<ListenerRegistration> = emptyList()
    private var started = false

    fun startListening() {
        if (started) return
        started = true

        try {
            listeners = repository.listenToDashboard(
                onUpdate = { name, organization, location, session ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        operatorName = name,
                        organizationName = organization,
                        location = location,
                        activeSession = session,
                        error = null
                    )
                },
                onError = { error ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = error
                    )
                }
            )
        } catch (exception: Exception) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = exception.message ?: "Could not load dashboard."
            )
        }
    }

    fun openShop() {
        if (_uiState.value.actionLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                actionLoading = true,
                error = null,
                message = null
            )

            try {
                repository.openShop()

                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    message = "Your shop is now open. Booking is available."
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    error = exception.message ?: "Could not open shop."
                )
            }
        }
    }

    fun closeShop() {
        val session = _uiState.value.activeSession ?: return
        if (_uiState.value.actionLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                actionLoading = true,
                error = null,
                message = null
            )

            try {
                repository.closeShop(session.id)

                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    message = "Your shop has been closed."
                )
            } catch (exception: Exception) {
                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    error = exception.message ?: "Could not close shop."
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            error = null,
            message = null
        )
    }

    override fun onCleared() {
        listeners.forEach { it.remove() }
        listeners = emptyList()
        super.onCleared()
    }
}
