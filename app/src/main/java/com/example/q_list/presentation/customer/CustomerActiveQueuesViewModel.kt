package com.example.q_list.presentation.customer

import androidx.lifecycle.ViewModel
import com.example.q_list.data.model.CustomerQueueInfo
import com.example.q_list.data.repository.CustomerQueueRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class CustomerActiveQueuesUiState(
    val loading: Boolean = true,
    val activeQueues: List<CustomerQueueInfo> = emptyList(),
    val error: String? = null
)

class CustomerActiveQueuesViewModel(
    private val repository: CustomerQueueRepository = CustomerQueueRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerActiveQueuesUiState())
    val uiState: StateFlow<CustomerActiveQueuesUiState> = _uiState.asStateFlow()

    private var listener: ListenerRegistration? = null

    init {
        listenToActiveQueues()
    }

    private fun listenToActiveQueues() {
        try {
            listener = repository.listenToMyActiveQueues(
                onUpdate = { queues ->
                    _uiState.value = CustomerActiveQueuesUiState(
                        loading = false,
                        activeQueues = queues
                    )
                },
                onError = { message ->
                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        error = message
                    )
                }
            )
        } catch (e: Exception) {
            _uiState.value = CustomerActiveQueuesUiState(
                loading = false,
                error = e.message ?: "Could not load your active bookings."
            )
        }
    }

    override fun onCleared() {
        listener?.remove()
        listener = null
        super.onCleared()
    }
}