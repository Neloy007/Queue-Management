package com.example.q_list.data.model


data class OperatorService(
    val id: String = "",
    val operatorId: String = "",
    val organizationName: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val estimatedMinutes: Int = 1,
    val active: Boolean = true
)
