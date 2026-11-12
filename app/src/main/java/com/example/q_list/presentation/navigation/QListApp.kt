package com.example.q_list.presentation.navigation

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import com.example.q_list.ui.screens.SplashScreen
import com.example.q_list.ui.screens.WelcomeScreen

@Composable
fun QListApp() {

    var showSplash by rememberSaveable {
        mutableStateOf(true)
    }

    if (showSplash) {

        SplashScreen(
            onFinished = {
                showSplash = false
            }
        )

    } else {

        WelcomeScreen(
            onCustomerClick = {
                // Customer authentication will be connected next.
            },
            onOperatorClick = {
                // Operator authentication will be connected next.
            }
        )
    }
}