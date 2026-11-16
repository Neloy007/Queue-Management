package com.example.q_list.presentation.customer


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Business
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Remove
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.q_list.data.model.CustomerOrganization
import com.example.q_list.data.model.OperatorService
import java.util.Locale
import androidx.compose.runtime.remember

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerExploreScreen(
    onBack: () -> Unit,
    onBookingCreated: (queueId: String, tokenId: String) -> Unit,
    viewModel: CustomerExploreViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val selectedOrganization = state.selectedOrganization

    val searchQuery = state.searchQuery.trim()

    val filteredOrganizations = remember(
        state.organizations,
        state.searchableServices,
        searchQuery
    ) {
        if (searchQuery.isBlank()) {
            state.organizations
        } else {
            state.organizations.filter { organization ->

                val matchesShopName =
                    organization.organizationName.contains(
                        searchQuery,
                        ignoreCase = true
                    )

                val matchesLocation =
                    organization.location.contains(
                        searchQuery,
                        ignoreCase = true
                    )

                val matchesService = state.searchableServices.any { service ->
                    service.operatorId == organization.operatorId &&
                            service.name.contains(
                                searchQuery,
                                ignoreCase = true
                            )
                }

                matchesShopName || matchesLocation || matchesService
            }
        }
    }

    val matchingServiceNames: (CustomerOrganization) -> List<String> = { organization ->
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            state.searchableServices
                .filter { service ->
                    service.operatorId == organization.operatorId &&
                            service.name.contains(searchQuery, ignoreCase = true)
                }
                .map { it.name }
                .distinct()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        selectedOrganization?.organizationName
                            ?: "Explore Organizations"
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (selectedOrganization != null) {
                                viewModel.clearSelection()
                            } else {
                                onBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        bottomBar = {
            if (selectedOrganization != null &&
                state.selectedQuantities.isNotEmpty()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    shape = RoundedCornerShape(18.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "${viewModel.getSelectedServiceCount()} items",
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = "৳${String.format(Locale.US, "%.2f", viewModel.getSelectedTotal())}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = {
                                viewModel.bookSelectedServices { tokenId ->

                                    selectedOrganization?.let { organization ->
                                        onBookingCreated(
                                            organization.queueId,
                                            tokenId
                                        )
                                    }
                                }
                            },
                            enabled = !state.bookingLoading &&
                                    state.selectedQuantities.isNotEmpty()
                        ) {
                            if (state.bookingLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("Continue")
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {

            Spacer(modifier = Modifier.height(12.dp))

            if (selectedOrganization == null) {

                Text(
                    text = "Find your service",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Choose an organization to see its available services.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                CustomerSearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.loading -> {
                        LoadingContent("Finding organizations...")
                    }

                    filteredOrganizations.isEmpty() -> {
                        EmptyContent(
                            title = if (searchQuery.isBlank()) {
                                "No open organizations"
                            } else {
                                "No matching results"
                            },
                            description = if (searchQuery.isBlank()) {
                                "There are no active queues right now. Please check again later."
                            } else {
                                "Try another shop name, location or service name."
                            }
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = filteredOrganizations,
                                key = { it.queueId }
                            ) { organization ->

                                OrganizationCard(
                                    organization = organization,
                                    matchingServices = matchingServiceNames(organization),
                                    onClick = {
                                        viewModel.selectOrganization(organization)
                                    }
                                )
                            }
                        }
                    }
                }

            } else {

                Text(
                    text = "Available Services",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Select the services you want and adjust their quantities.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    state.servicesLoading -> {
                        LoadingContent("Loading company services...")
                    }

                    state.services.isEmpty() -> {
                        EmptyContent(
                            title = "No services available",
                            description = "This organization has not added any active services yet."
                        )
                    }

                    else -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = state.services,
                                key = { it.id }
                            ) { service ->

                                CustomerServiceCard(
                                    service = service,
                                    quantity = state.selectedQuantities[service.id] ?: 0,
                                    onIncrease = {
                                        viewModel.increaseQuantity(service.id)
                                    },
                                    onDecrease = {
                                        viewModel.decreaseQuantity(service.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            state.error?.let { error ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun OrganizationCard(
    organization: CustomerOrganization,
    matchingServices: List<String> = emptyList(),
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Business,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )

                Spacer(modifier = Modifier.size(12.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = organization.organizationName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Queue currently open",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (matchingServices.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Matching services: ${matchingServices.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.LocationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = organization.location.ifBlank {
                        "Location not provided"
                    },
                    modifier = Modifier.padding(start = 6.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Divider()

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "View Services →",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun CustomerServiceCard(
    service: OperatorService,
    quantity: Int,
    onIncrease: () -> Unit,
    onDecrease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = service.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(5.dp))

                Text(
                    text = "৳${String.format(Locale.US, "%.2f", service.price)}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${service.estimatedMinutes} minutes estimated",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (quantity == 0) {
                OutlinedButton(onClick = onIncrease) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null
                    )
                    Text("Add")
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(onClick = onDecrease) {
                        Icon(
                            imageVector = Icons.Outlined.Remove,
                            contentDescription = "Decrease quantity"
                        )
                    }

                    Text(
                        text = quantity.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(onClick = onIncrease) {
                        Icon(
                            imageVector = Icons.Outlined.Add,
                            contentDescription = "Increase quantity"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(modifier = Modifier.height(12.dp))
        Text(message)
    }
}

@Composable
private fun EmptyContent(
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
