package com.example.q_list.data.model


data class QueueToken(
    val id: String = "",
    val queueId: String = "",
    val tokenNumber: Long = 0L,
    val customerName: String = "Customer",
    val status: String = "WAITING",
    val operatorId: String = "",
    val counterName: String = ""
)
