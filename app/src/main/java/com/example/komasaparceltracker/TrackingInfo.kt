package com.example.komasaparceltracker

data class TrackingInfo(
    val trackingNumber: String,
    val status: String,
    val timestamp: Long = 0 ,
    val arrivalConfirmationTimestamp: Long = 0 // Default value
)




