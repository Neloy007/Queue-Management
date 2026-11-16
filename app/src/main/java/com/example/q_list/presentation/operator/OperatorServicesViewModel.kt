package com.example.q_list.presentation.operator


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.q_list.data.model.OperatorService
import com.example.q_list.data.repository.OperatorServiceRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OperatorServicesUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val services: List<OperatorService> = emptyList(),
    val error: String? = null,
    val message: String? = null
)

class OperatorServicesViewModel(
    private val repository: OperatorServiceRepository =
        OperatorServiceRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorServicesUiState())
    val uiState: StateFlow<OperatorServicesUiState> =
        _uiState.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        startListening()
    }

    private fun startListening() {
        listener?.remove()

        try {
            listener = repository.listenToServices(
                onUpdate = { services ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        services = services,
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
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                loading = false,
                error = e.message ?: "Could not load services."
            )
        }
    }

    fun addService(
        name: String,
        price: Double,
        estimatedMinutes: Int
    ) {
        performAction {
            repository.addService(name, price, estimatedMinutes)
            "Service added successfully."
        }
    }

    fun updateService(
        service: OperatorService,
        name: String,
        price: Double,
        estimatedMinutes: Int
    ) {
        performAction {
            repository.updateService(
                service,
                name,
                price,
                estimatedMinutes
            )
            "Service updated successfully."
        }
    }

    fun deleteService(service: OperatorService) {
        performAction {
            repository.deleteService(service)
            "Service deleted successfully."
        }
    }

    private fun performAction(action: suspend () -> String) {
        if (_uiState.value.saving) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                saving = true,
                error = null,
                message = null
            )

            try {
                val message = action()

                _uiState.value = _uiState.value.copy(
                    saving = false,
                    message = message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    saving = false,
                    error = e.message ?: "Something went wrong."
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
        listener?.remove()
        listener = null
        super.onCleared()
    }
}
