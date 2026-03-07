package com.example.halo.domain.model

data class Alarm(
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val radius: Double,
    val name: String,
    val isEnabled: Boolean = true,
    val createdDate: Long = System.currentTimeMillis(),
    val soundUri: String? = null,
    val soundTitle: String? = null,
    val category: String = "General"
)
