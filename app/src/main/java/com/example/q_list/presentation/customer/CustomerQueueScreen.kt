package com.example.q_list.presentation.customer


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.q_list.data.model.CustomerQueueInfo
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerQueueScreen(
    tokenId: String,
    queueId: String,
    onBack: () -> Unit,
    viewModel: CustomerQueueViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    val context = LocalContext.current

    var previousStatus by remember(tokenId) {
        mutableStateOf<String?>(null)
    }

    LaunchedEffect(state.queue?.status, tokenId) {
        val currentStatus = state.queue?.status

        if (
            previousStatus == "WAITING" &&
            currentStatus == "SERVING"
        ) {
            QueueTurnAlarm.play(context)
        }

        if (currentStatus != null) {
            previousStatus = currentStatus
        }
    }

    LaunchedEffect(tokenId, queueId) {
        viewModel.startListening(tokenId, queueId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Queue") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Outlined.ArrowBack,
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            when {
                state.loading -> {
                    Spacer(Modifier.height(80.dp))
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Loading your live queue...")
                }

                state.error != null -> {
                    Text(
                        text = state.error.orEmpty(),
                        color = MaterialTheme.colorScheme.error
                    )

                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onBack) {
                        Text("Go Back")
                    }
                }

                state.queue != null -> {
                    QueueDetails(
                        queue = state.queue!!,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun QueueDetails(
    queue: CustomerQueueInfo,
    onBack: () -> Unit
) {
    val statusColor = when (queue.status) {
        "SERVING" -> MaterialTheme.colorScheme.primary
        "COMPLETED" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.secondary
    }

    Text(
        text = "Your booking is confirmed",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )

    Spacer(Modifier.height(8.dp))

    Text(
        text = queue.organizationName,
        style = MaterialTheme.typography.titleMedium
    )

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Outlined.LocationOn,
            contentDescription = null
        )
        Text(
            text = queue.location,
            modifier = Modifier.padding(start = 4.dp),
            style = MaterialTheme.typography.bodySmall
        )
    }

    Spacer(Modifier.height(24.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "YOUR TOKEN NUMBER",
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "#${queue.tokenNumber}",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = queue.status,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = statusColor
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            QueueInfoRow(
                label = "Your position",
                value = when (queue.status) {
                    "SERVING" -> "Now serving"
                    "COMPLETED" -> "Completed"
                    else -> "${queue.position}"
                }
            )

            QueueInfoRow(
                label = "Estimated waiting time",
                value = when (queue.status) {
                    "SERVING" -> "Your turn"
                    "COMPLETED" -> "Finished"
                    else -> "${queue.estimatedWaitMinutes} minutes"
                }
            )

            QueueInfoRow(
                label = "Booking total",
                value = "৳${"%.2f".format(queue.totalPrice)}"
            )
        }
    }

    Spacer(Modifier.height(20.dp))

    Text(
        text = when (queue.status) {
            "SERVING" ->
                "It's your turn! Please proceed to the service counter."

            "COMPLETED" ->
                "Your service has been completed. Thank you for using Q-List!"

            else ->
                "Your queue updates automatically. You can stay on this screen to follow your turn."
        },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(Modifier.height(20.dp))

    Button(
        onClick = onBack,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Back to Home")
    }
}

@Composable
private fun QueueInfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value,
            fontWeight = FontWeight.Bold
        )
    }
}
