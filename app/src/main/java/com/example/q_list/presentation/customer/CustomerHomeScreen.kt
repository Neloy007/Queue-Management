package com.example.q_list.presentation.customer


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.q_list.data.model.CustomerQueueInfo

@Composable
fun CustomerHomeScreen(
    onExploreClick: () -> Unit,
    onMyQueueClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationsClick: () -> Unit,
    activeQueue: CustomerQueueInfo? = null,
    onActiveQueueClick: (CustomerQueueInfo) -> Unit = {}
) {
    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = true,
                    onClick = {},
                    icon = {
                        Icon(Icons.Outlined.Home, contentDescription = "Home")
                    },
                    label = { Text("Home") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onExploreClick,
                    icon = {
                        Icon(Icons.Outlined.Search, contentDescription = "Explore")
                    },
                    label = { Text("Explore") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onMyQueueClick,
                    icon = {
                        Icon(
                            Icons.Outlined.ConfirmationNumber,
                            contentDescription = "My Queue"
                        )
                    },
                    label = { Text("My Queue") }
                )

                NavigationBarItem(
                    selected = false,
                    onClick = onProfileClick,
                    icon = {
                        Icon(Icons.Outlined.Person, contentDescription = "Profile")
                    },
                    label = { Text("Profile") }
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Q-List",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Text(
                        text = "SMART QUEUE MANAGEMENT",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onNotificationsClick) {
                    Icon(
                        Icons.Outlined.Notifications,
                        contentDescription = "Notifications"
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "Hello, Customer!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage your queues without waiting in line.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ==========================================
// ACTIVE QUEUE CARD
// ==========================================

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.ConfirmationNumber,
                            contentDescription = null
                        )

                        Spacer(Modifier.width(8.dp))

                        Text(
                            text = "YOUR ACTIVE QUEUE",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(18.dp))

                    if (activeQueue != null) {

                        Text(
                            text = "Token #${activeQueue.tokenNumber}",
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(6.dp))

                        Text(
                            text = activeQueue.organizationName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    "STATUS",
                                    style = MaterialTheme.typography.labelSmall
                                )

                                Text(
                                    activeQueue.status,
                                    fontWeight = FontWeight.Bold,
                                    color = when (activeQueue.status) {
                                        "SERVING" -> MaterialTheme.colorScheme.primary
                                        "WAITING" -> MaterialTheme.colorScheme.tertiary
                                        else -> MaterialTheme.colorScheme.onSurface
                                    }
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    "POSITION",
                                    style = MaterialTheme.typography.labelSmall
                                )

                                Text(
                                    if (activeQueue.status == "SERVING") {
                                        "Your turn"
                                    } else {
                                        "#${activeQueue.position}"
                                    },
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))

                        Button(
                            onClick = {
                                onActiveQueueClick(activeQueue)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("View My Token")

                            Spacer(Modifier.width(8.dp))

                            Icon(
                                Icons.Outlined.ArrowForward,
                                contentDescription = null
                            )
                        }

                    } else {

                        Text(
                            text = "--",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = "You don't have an active token.",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(Modifier.height(18.dp))

                        Button(
                            onClick = onExploreClick,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Find a Service")

                            Spacer(Modifier.width(8.dp))

                            Icon(
                                Icons.Outlined.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Search
            Text(
                text = "Find a Service",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onExploreClick() },
                enabled = false,
                placeholder = {
                    Text("Search organizations or services")
                },
                leadingIcon = {
                    Icon(Icons.Outlined.Search, contentDescription = null)
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Quick Actions
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickActionCard(
                    title = "Explore",
                    description = "Find organizations",
                    icon = {
                        Icon(
                            Icons.Outlined.Business,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f),
                    onClick = onExploreClick
                )

                QuickActionCard(
                    title = "My Queue",
                    description = "Track your token",
                    icon = {
                        Icon(
                            Icons.Outlined.AccessTime,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.weight(1f),
                    onClick = onMyQueueClick
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "How Q-List Works",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "1. Find an organization and select a service.\n\n" +
                        "2. Join the queue and receive your token.\n\n" +
                        "3. Track your position and estimated waiting time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            icon()

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
