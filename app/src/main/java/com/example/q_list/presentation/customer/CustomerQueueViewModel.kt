package com.example.q_list.presentation.customer


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.q_list.data.model.CustomerQueueInfo
import com.example.q_list.data.repository.CustomerQueueRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerQueueUiState(
    val loading: Boolean = true,
    val queue: CustomerQueueInfo? = null,
    val error: String? = null
)

class CustomerQueueViewModel(
    private val repository: CustomerQueueRepository =
        CustomerQueueRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerQueueUiState())
    val uiState: StateFlow<CustomerQueueUiState> =
        _uiState.asStateFlow()

    private var listeners: List<ListenerRegistration> = emptyList()

    fun startListening(tokenId: String, queueId: String) {
        if (tokenId.isBlank() || queueId.isBlank()) {
            _uiState.value = CustomerQueueUiState(
                loading = false,
                error = "Invalid queue information."
            )
            return
        }

        listeners.forEach { it.remove() }
        listeners = emptyList()

        _uiState.value = CustomerQueueUiState(loading = true)

        listeners = repository.listenToMyQueue(
            tokenId = tokenId,
            queueId = queueId,
            onUpdate = { queueInfo ->
                _uiState.value = CustomerQueueUiState(
                    loading = false,
                    queue = queueInfo
                )
            },
            onError = { error ->
                _uiState.value = CustomerQueueUiState(
                    loading = false,
                    error = error
                )
            }
        )
    }

    override fun onCleared() {
        listeners.forEach { it.remove() }
        super.onCleared()
    }
}
