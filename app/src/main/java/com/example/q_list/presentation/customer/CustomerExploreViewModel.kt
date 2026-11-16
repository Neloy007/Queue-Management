
package com.example.q_list.presentation.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.q_list.data.model.CustomerOrganization
import com.example.q_list.data.model.OperatorService
import com.example.q_list.data.repository.CustomerExploreRepository
import com.example.q_list.data.repository.CustomerQueueRepository
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class CustomerExploreUiState(
    val loading: Boolean = true,
    val organizations: List<CustomerOrganization> = emptyList(),
    val searchableServices: List<OperatorService> = emptyList(),
    val searchQuery: String = "",
    val selectedOrganization: CustomerOrganization? = null,
    val servicesLoading: Boolean = false,
    val services: List<OperatorService> = emptyList(),
    val selectedQuantities: Map<String, Int> = emptyMap(),
    val bookingLoading: Boolean = false,
    val error: String? = null
)

class CustomerExploreViewModel(
    private val repository: CustomerExploreRepository =
        CustomerExploreRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(CustomerExploreUiState())
    val uiState: StateFlow<CustomerExploreUiState> =
        _uiState.asStateFlow()

    private var organizationListener: ListenerRegistration? = null
    private var globalServiceListener: ListenerRegistration? = null
    private var serviceListener: ListenerRegistration? = null

    private val queueRepository = CustomerQueueRepository()

    init {
        listenToOrganizations()
        listenToAllActiveServices()
    }

    private fun listenToOrganizations() {
        organizationListener?.remove()

        try {
            organizationListener = repository.listenToOrganizations(
                onUpdate = { organizations ->

                    val currentSelection =
                        _uiState.value.selectedOrganization

                    val updatedSelection = currentSelection?.let { selected ->
                        organizations.firstOrNull {
                            it.queueId == selected.queueId
                        }
                    }

                    _uiState.value = _uiState.value.copy(
                        loading = false,
                        organizations = organizations,
                        selectedOrganization = updatedSelection,
                        error = null
                    )

                    if (currentSelection != null && updatedSelection == null) {
                        serviceListener?.remove()
                        serviceListener = null

                        _uiState.value = _uiState.value.copy(
                            services = emptyList(),
                            selectedQuantities = emptyMap(),
                            servicesLoading = false,
                            error = "This organization has closed its queue."
                        )
                    }
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
                error = e.message ?: "Could not load organizations."
            )
        }
    }

    private fun listenToAllActiveServices() {
        globalServiceListener?.remove()

        globalServiceListener = repository.listenToAllActiveServices(
            onUpdate = { services ->
                _uiState.value = _uiState.value.copy(
                    searchableServices = services
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    error = error
                )
            }
        )
    }

    fun updateSearchQuery(query: String) {
        _uiState.value = _uiState.value.copy(
            searchQuery = query
        )
    }

    fun clearSearch() {
        updateSearchQuery("")
    }

    fun selectOrganization(organization: CustomerOrganization) {
        serviceListener?.remove()

        _uiState.value = _uiState.value.copy(
            selectedOrganization = organization,
            services = emptyList(),
            servicesLoading = true,
            selectedQuantities = emptyMap(),
            error = null
        )

        serviceListener = repository.listenToServices(
            operatorId = organization.operatorId,
            onUpdate = { services ->
                _uiState.value = _uiState.value.copy(
                    services = services,
                    servicesLoading = false,
                    error = null
                )
            },
            onError = { error ->
                _uiState.value = _uiState.value.copy(
                    servicesLoading = false,
                    error = error
                )
            }
        )
    }

    fun clearSelection() {
        serviceListener?.remove()
        serviceListener = null

        _uiState.value = _uiState.value.copy(
            selectedOrganization = null,
            services = emptyList(),
            servicesLoading = false,
            selectedQuantities = emptyMap(),
            error = null
        )
    }

    fun increaseQuantity(serviceId: String) {
        val current = _uiState.value.selectedQuantities

        _uiState.value = _uiState.value.copy(
            selectedQuantities = current + (
                    serviceId to ((current[serviceId] ?: 0) + 1)
                    )
        )
    }

    fun decreaseQuantity(serviceId: String) {
        val current = _uiState.value.selectedQuantities
        val quantity = current[serviceId] ?: 0

        val updated = if (quantity <= 1) {
            current - serviceId
        } else {
            current + (serviceId to (quantity - 1))
        }

        _uiState.value = _uiState.value.copy(
            selectedQuantities = updated
        )
    }

    fun getSelectedTotal(): Double {
        val state = _uiState.value

        return state.services.sumOf { service ->
            service.price * (state.selectedQuantities[service.id] ?: 0)
        }
    }

    fun getSelectedServiceCount(): Int {
        return _uiState.value.selectedQuantities.values.sum()
    }

    fun bookSelectedServices(onSuccess: (String) -> Unit) {
        val organization = _uiState.value.selectedOrganization ?: return
        val quantities = _uiState.value.selectedQuantities

        if (quantities.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = "Please select at least one service."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                bookingLoading = true,
                error = null
            )

            try {
                val tokenId = queueRepository.createBooking(
                    organization = organization,
                    selectedQuantities = quantities
                )

                _uiState.value = _uiState.value.copy(
                    bookingLoading = false
                )

                onSuccess(tokenId)

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    bookingLoading = false,
                    error = e.message ?: "Booking failed. Please try again."
                )
            }
        }
    }

    override fun onCleared() {
        organizationListener?.remove()
        globalServiceListener?.remove()
        serviceListener?.remove()
        super.onCleared()
    }
}
