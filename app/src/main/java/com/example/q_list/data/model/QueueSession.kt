package com.example.q_list.data.model


data class QueueSession(
    val id: String = "",
    val operatorId: String = "",
    val organizationName: String = "",
    val location: String = "",
    val bookingCode: String = "",
    val sessionDate: String = "",
    val status: String = "CLOSED",
    val lastTokenNumber: Long = 0L,
    val currentTokenId: String? = null,
    val waitingTokenIds: List<String> = emptyList(),
    val averageMinutesPerUnit: Int = 10
)
