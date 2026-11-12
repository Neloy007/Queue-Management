package com.example.q_list.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.SupportAgent
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WelcomeScreen(
    onCustomerClick: () -> Unit,
    onOperatorClick: () -> Unit
) {

    val background = Color(0xFF101827)
    val surface = Color(0xFF1E2B40)
    val primary = Color(0xFF2563EB)
    val secondaryText = Color(0xFF9CA3AF)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(35.dp))

        // App branding
        Text(
            text = "Q-List",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Your time matters.\nSkip the physical queue.",
            fontSize = 16.sp,
            lineHeight = 25.sp,
            textAlign = TextAlign.Center,
            color = secondaryText
        )

        Spacer(modifier = Modifier.height(65.dp))

        Text(
            text = "Welcome!",
            fontSize = 29.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Choose how you want to use Q-List",
            fontSize = 14.sp,
            color = secondaryText
        )

        Spacer(modifier = Modifier.height(35.dp))

        // Customer option
        RoleSelectionCard(
            title = "Customer",
            description = "Join queues, get digital tokens\nand track your waiting time.",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            },
            onClick = onCustomerClick
        )

        Spacer(modifier = Modifier.height(18.dp))

        // Operator option
        RoleSelectionCard(
            title = "Service Operator",
            description = "Manage counters, call customers\nand control your service queue.",
            icon = {
                Icon(
                    imageVector = Icons.Outlined.SupportAgent,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            },
            onClick = onOperatorClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = "SMART QUEUE MANAGEMENT SYSTEM",
            fontSize = 10.sp,
            letterSpacing = 2.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RoleSelectionCard(
    title: String,
    description: String,
    icon: @Composable () -> Unit,
    onClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(125.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E2B40)
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            Color(0xFF34445C)
        )
    ) {

        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(56.dp)
                    .background(
                        Color(0xFF2563EB),
                        RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                icon()
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(7.dp))

                Text(
                    text = description,
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFF9CA3AF)
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                contentDescription = "Select $title",
                tint = Color(0xFF60A5FA),
                modifier = Modifier.size(22.dp)
            )
        }
    }
}