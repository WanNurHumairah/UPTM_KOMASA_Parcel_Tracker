package com.example.komasaparceltracker

data class CollectedParcel(
    val trackingNumber: String,
    val status: String,
    val arrivalConfirmationTimestamp: Long? // Nullable
)
