package com.example.komasaparceltracker

data class HelperClass(
    var name: String? = null,
    var phone: String? = null,
    var idNumber: String? = null,
    var trackingNumber: String? = null,
    var password: String? = null, // Store hashed password here
    var uid: String? = null, // Store UID here
    var status: String? = null // Status of the parcel
)




