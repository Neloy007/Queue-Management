
package com.example.q_list.presentation.navigation

object Routes {
    const val SPLASH = "splash"
    const val WELCOME = "welcome"

    const val CUSTOMER_AUTH = "customer_auth"
    const val OPERATOR_AUTH = "operator_auth"

    const val CUSTOMER_HOME = "customer_home"
    const val OPERATOR_HOME = "operator_home"

    const val CUSTOMER_EXPLORE = "customer_explore"
    const val CUSTOMER_QUEUE = "customer_queue"
    const val CUSTOMER_PROFILE = "customer_profile"
    const val CUSTOMER_NOTIFICATIONS = "customer_notifications"

    const val OPERATOR_QUEUE = "operator_queue/{queueId}"
    const val OPERATOR_COUNTERS = "operator_counters"

    const val CUSTOMER_QUEUE_DETAILS = "customer_queue_details/{queueId}/{tokenId}"
}
