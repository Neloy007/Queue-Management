package com.example.q_list.presentation.operator


import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.q_list.data.model.QueueToken
import com.example.q_list.data.repository.OperatorQueueRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OperatorQueueUiState(
    val loading: Boolean = true,
    val waitingTokens: List<QueueToken> = emptyList(),
    val servingToken: QueueToken? = null,
    val error: String? = null,
    val message: String? = null,
    val actionLoading: Boolean = false
)

class OperatorQueueViewModel(
    private val repository: OperatorQueueRepository = OperatorQueueRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(OperatorQueueUiState())
    val uiState: StateFlow<OperatorQueueUiState> = _uiState.asStateFlow()

    private var queueListener: ListenerRegistration? = null

    fun observeQueue(queueId: String) {
        if (queueId.isBlank()) {
            _uiState.value = OperatorQueueUiState(
                loading = false,
                error = "Queue ID is missing."
            )
            return
        }

        queueListener?.remove()
        _uiState.value = OperatorQueueUiState(loading = true)

        queueListener = repository.listenToQueue(
            queueId = queueId,
            onUpdate = { waiting, serving ->
                _uiState.value = _uiState.value.copy(
                    loading = false,
                    waitingTokens = waiting,
                    servingToken = serving,
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
    }

    fun callNextCustomer(queueId: String) {
        performAction {
            repository.callNextCustomer(queueId)
            "Next customer called successfully."
        }
    }

    fun completeService(queueId: String) {
        performAction {
            repository.completeService(queueId)
            "Service completed successfully."
        }
    }

    private fun performAction(
        action: suspend () -> String
    ) {
        if (_uiState.value.actionLoading) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                actionLoading = true,
                error = null,
                message = null
            )

            try {
                val result = action()

                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
                    message = result
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    actionLoading = false,
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
        queueListener?.remove()
        queueListener = null
        super.onCleared()
    }
}
