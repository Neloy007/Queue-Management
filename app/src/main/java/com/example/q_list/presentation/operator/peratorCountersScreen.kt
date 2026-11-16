
package com.example.q_list.presentation.operator

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.q_list.data.model.OperatorService
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OperatorCountersScreen(
    onBack: () -> Unit,
    viewModel: OperatorServicesViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    var showServiceDialog by remember { mutableStateOf(false) }
    var editingService by remember {
        mutableStateOf<OperatorService?>(null)
    }
    var deletingService by remember {
        mutableStateOf<OperatorService?>(null)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Manage Services")
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {

            Text(
                text = "Company Service Catalogue",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage the services and prices your customers can book.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    editingService = null
                    viewModel.clearMessages()
                    showServiceDialog = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
                Text(
                    text = "Add New Service",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            uiState.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            uiState.message?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Your Services",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "${uiState.services.size} services",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when {
                uiState.loading -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Loading your services...")
                    }
                }

                uiState.services.isEmpty() -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No services added yet",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your first company service to get started.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(
                            items = uiState.services,
                            key = { it.id }
                        ) { service ->

                            ServiceCard(
                                service = service,
                                onEdit = {
                                    viewModel.clearMessages()
                                    editingService = service
                                    showServiceDialog = true
                                },
                                onDelete = {
                                    viewModel.clearMessages()
                                    deletingService = service
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showServiceDialog) {
        ServiceEditorDialog(
            service = editingService,
            saving = uiState.saving,
            onDismiss = {
                if (!uiState.saving) {
                    showServiceDialog = false
                    editingService = null
                    viewModel.clearMessages()
                }
            },
            onSave = { name, price, minutes ->
                if (editingService == null) {
                    viewModel.addService(name, price, minutes)
                } else {
                    viewModel.updateService(
                        service = editingService!!,
                        name = name,
                        price = price,
                        estimatedMinutes = minutes
                    )
                }
                showServiceDialog = false
                editingService = null
            }
        )
    }

    deletingService?.let { service ->
        AlertDialog(
            onDismissRequest = {
                if (!uiState.saving) deletingService = null
            },
            title = {
                Text("Delete Service?")
            },
            text = {
                Text("Are you sure you want to delete ${service.name}?")
            },
            confirmButton = {
                TextButton(
                    enabled = !uiState.saving,
                    onClick = {
                        viewModel.deleteService(service)
                        deletingService = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { deletingService = null },
                    enabled = !uiState.saving
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun ServiceCard(
    service: OperatorService,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
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

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "৳${String.format(Locale.US, "%.2f", service.price)}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "Estimated time: ${service.estimatedMinutes} minutes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onEdit) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit service"
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete service",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ServiceEditorDialog(
    service: OperatorService?,
    saving: Boolean,
    onDismiss: () -> Unit,
    onSave: (String, Double, Int) -> Unit
) {
    var name by remember(service?.id) {
        mutableStateOf(service?.name.orEmpty())
    }

    var price by remember(service?.id) {
        mutableStateOf(
            service?.price?.let {
                if (it % 1.0 == 0.0) it.toLong().toString()
                else it.toString()
            }.orEmpty()
        )
    }

    var minutes by remember(service?.id) {
        mutableStateOf(service?.estimatedMinutes?.toString().orEmpty())
    }

    var validationError by remember(service?.id) {
        mutableStateOf<String?>(null)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (service == null) "Add New Service"
                else "Update Service"
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(
                    rememberScrollState()
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        validationError = null
                    },
                    label = { Text("Service / Item Name") },
                    placeholder = {
                        Text("e.g. Haircut, Consultation")
                    },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = price,
                    onValueChange = {
                        price = it
                        validationError = null
                    },
                    label = { Text("Price (৳)") },
                    placeholder = { Text("Enter service price") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = minutes,
                    onValueChange = {
                        minutes = it
                        validationError = null
                    },
                    label = { Text("Estimated Time (Minutes)") },
                    placeholder = { Text("e.g. 15") },
                    singleLine = true,
                    enabled = !saving,
                    modifier = Modifier.fillMaxWidth()
                )

                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = !saving,
                onClick = {
                    val cleanName = name.trim()
                    val parsedPrice = price.toDoubleOrNull()
                    val parsedMinutes = minutes.toIntOrNull()

                    when {
                        cleanName.isBlank() -> {
                            validationError = "Please enter a service name."
                        }

                        cleanName.length > 100 -> {
                            validationError = "Service name must be 100 characters or fewer."
                        }

                        parsedPrice == null ||
                                !parsedPrice.isFinite() ||
                                parsedPrice < 0 -> {
                            validationError = "Enter a valid price."
                        }

                        parsedMinutes == null ||
                                parsedMinutes !in 1..1440 -> {
                            validationError = "Enter a time between 1 and 1440 minutes."
                        }

                        else -> {
                            onSave(
                                cleanName,
                                parsedPrice,
                                parsedMinutes
                            )
                        }
                    }
                }
            ) {
                if (saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (service == null) "Add Service" else "Save Changes")
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !saving
            ) {
                Text("Cancel")
            }
        }
    )
}
