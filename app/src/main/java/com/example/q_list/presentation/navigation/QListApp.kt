
package com.example.q_list.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

import com.example.q_list.data.model.UserRole
import com.example.q_list.presentation.auth.AuthScreen
import com.example.q_list.presentation.auth.AuthViewModel
import com.example.q_list.presentation.customer.CustomerHomeScreen
import com.example.q_list.presentation.customer.CustomerProfileScreen
import com.example.q_list.presentation.operator.OperatorHomeScreen
import com.example.q_list.presentation.operator.OperatorQueueScreen
import com.example.q_list.ui.screens.SplashScreen
import com.example.q_list.ui.screens.WelcomeScreen
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import com.example.q_list.presentation.customer.CustomerActiveQueuesViewModel
import com.example.q_list.presentation.customer.CustomerExploreScreen
import com.example.q_list.presentation.customer.CustomerMyQueueScreen
import com.example.q_list.presentation.customer.CustomerQueueScreen
import com.example.q_list.presentation.operator.OperatorCountersScreen

@Composable
fun QListApp(
    navController: NavHostController = rememberNavController()
) {

    val authViewModel: AuthViewModel = viewModel()

    var sessionChecked = remember { mutableStateOf(false) }

    NavHost(
        navController = navController,
        startDestination = Routes.SPLASH
    ) {

        // SPLASH
        composable(Routes.SPLASH) {

            SplashScreen(
                onFinished = {

                    authViewModel.checkExistingSession { role ->

                        if (role == UserRole.CUSTOMER) {

                            navController.navigate(Routes.CUSTOMER_HOME) {
                                popUpTo(Routes.SPLASH) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }

                        } else if (role == UserRole.OPERATOR) {

                            navController.navigate(Routes.OPERATOR_HOME) {
                                popUpTo(Routes.SPLASH) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }

                        } else {

                            navController.navigate(Routes.WELCOME) {
                                popUpTo(Routes.SPLASH) {
                                    inclusive = true
                                }
                                launchSingleTop = true
                            }
                        }

                        sessionChecked.value = true
                    }
                }
            )
        }

        // WELCOME
        composable(Routes.WELCOME) {
            WelcomeScreen(
                onCustomerClick = {
                    authViewModel.clearMessages()
                    navController.navigate(Routes.CUSTOMER_AUTH)
                },
                onOperatorClick = {
                    authViewModel.clearMessages()
                    navController.navigate(Routes.OPERATOR_AUTH)
                }
            )
        }

        // CUSTOMER AUTH
        composable(Routes.CUSTOMER_AUTH) {
            AuthScreen(
                role = UserRole.CUSTOMER,
                viewModel = authViewModel,
                onBack = {
                    authViewModel.clearMessages()
                    navController.popBackStack()
                },
                onAuthenticated = {
                    navController.navigate(Routes.CUSTOMER_HOME) {
                        popUpTo(Routes.WELCOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // OPERATOR AUTH
        composable(Routes.OPERATOR_AUTH) {
            AuthScreen(
                role = UserRole.OPERATOR,
                viewModel = authViewModel,
                onBack = {
                    authViewModel.clearMessages()
                    navController.popBackStack()
                },
                onAuthenticated = {
                    navController.navigate(Routes.OPERATOR_HOME) {
                        popUpTo(Routes.WELCOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }

        // CUSTOMER HOME
        composable(Routes.CUSTOMER_HOME) {
            val activeQueuesViewModel: CustomerActiveQueuesViewModel = viewModel()
            val queueState by activeQueuesViewModel.uiState.collectAsState()

            CustomerHomeScreen(
                onExploreClick = {
                    navController.navigate(Routes.CUSTOMER_EXPLORE)
                },
                onMyQueueClick = {
                    navController.navigate(Routes.CUSTOMER_QUEUE)
                },
                onProfileClick = {
                    navController.navigate(Routes.CUSTOMER_PROFILE)
                },
                onNotificationsClick = {
                    navController.navigate(Routes.CUSTOMER_NOTIFICATIONS)
                },
                activeQueue = queueState.activeQueues.firstOrNull(),
                onActiveQueueClick = { queue ->
                    navController.navigate(
                        Routes.CUSTOMER_QUEUE_DETAILS
                            .replace("{queueId}", queue.queueId)
                            .replace("{tokenId}", queue.tokenId)
                    )
                }
            )
        }

        // CUSTOMER PROFILE
        composable(Routes.CUSTOMER_PROFILE) {
            CustomerProfileScreen(
                onBack = {
                    navController.popBackStack()
                },
                onLogout = {
                    authViewModel.logout()

                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.CUSTOMER_HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
            )
        }


        // OPERATOR HOME
        composable(Routes.OPERATOR_HOME) {
            OperatorHomeScreen(
                onLogout = {
                    authViewModel.logout()

                    navController.navigate(Routes.WELCOME) {
                        popUpTo(Routes.OPERATOR_HOME) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                },
                onManageQueue = { queueId ->
                    navController.navigate(
                        Routes.OPERATOR_QUEUE.replace(
                            "{queueId}",
                            queueId
                        )
                    )
                },
                onManageCounters = {
                    navController.navigate(Routes.OPERATOR_COUNTERS)
                }
            )
        }


        // OPERATOR QUEUE
        composable(
            route = Routes.OPERATOR_QUEUE,
            arguments = listOf(
                navArgument("queueId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val queueId = backStackEntry.arguments
                ?.getString("queueId")
                .orEmpty()

            OperatorQueueScreen(
                queueId = queueId,
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.OPERATOR_COUNTERS) {
            OperatorCountersScreen(
                onBack = {
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.CUSTOMER_EXPLORE) {
            CustomerExploreScreen(
                onBack = {
                    navController.popBackStack()
                },
                onBookingCreated = { queueId, tokenId ->
                    navController.navigate(
                        Routes.CUSTOMER_QUEUE_DETAILS
                            .replace("{queueId}", queueId)
                            .replace("{tokenId}", tokenId)
                    ) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.CUSTOMER_QUEUE_DETAILS,
            arguments = listOf(
                navArgument("queueId") {
                    type = NavType.StringType
                },
                navArgument("tokenId") {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val queueId =
                backStackEntry.arguments?.getString("queueId").orEmpty()

            val tokenId =
                backStackEntry.arguments?.getString("tokenId").orEmpty()

            CustomerQueueScreen(
                queueId = queueId,
                tokenId = tokenId,
                onBack = {
                    navController.popBackStack(
                        Routes.CUSTOMER_HOME,
                        inclusive = false
                    )
                }
            )
        }

        composable(Routes.CUSTOMER_QUEUE) {

            val activeQueuesViewModel: CustomerActiveQueuesViewModel = viewModel()
            val queueState by activeQueuesViewModel.uiState.collectAsState()

            CustomerMyQueueScreen(
                activeQueues = queueState.activeQueues,
                loading = queueState.loading,
                onBack = {
                    navController.popBackStack()
                },
                onQueueClick = { queue ->
                    navController.navigate(
                        Routes.CUSTOMER_QUEUE_DETAILS
                            .replace("{queueId}", queue.queueId)
                            .replace("{tokenId}", queue.tokenId)
                    )
                }
            )
        }
    }


}
