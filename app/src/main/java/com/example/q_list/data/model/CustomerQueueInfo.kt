package com.example.q_list.data.model


data class CustomerQueueInfo(
    val tokenId: String = "",
    val queueId: String = "",
    val tokenNumber: Long = 0L,
    val status: String = "WAITING",
    val organizationName: String = "",
    val location: String = "",
    val position: Int = 0,
    val estimatedWaitMinutes: Int = 0,
    val totalPrice: Double = 0.0,
    val customerName: String = ""
)
