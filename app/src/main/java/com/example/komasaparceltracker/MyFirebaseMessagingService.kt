package com.example.komasaparceltracker

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // Handle incoming messages here
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a notification payload
        remoteMessage.notification?.let {
            Log.d(TAG, "Notification Message Body: ${it.body}")
            // Customize notification handling here
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Handle new FCM token generation here
        Log.d(TAG, "Refreshed token: $token")
        // Send the token to your server for targeting notifications
    }

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }
}
