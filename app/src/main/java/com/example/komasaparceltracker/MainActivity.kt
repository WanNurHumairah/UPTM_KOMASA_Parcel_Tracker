package com.example.komasaparceltracker

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Remove the Thread.sleep(3000) to avoid blocking the main thread

        installSplashScreen()

        // Set the content view to the correct layout or remove this line if not needed
        setContentView(R.layout.activity_main)

        // Navigate to the SignupActivity after the splash screen
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)

        // Finish the current activity to prevent the user from going back to the splash screen
        finish()
    }
}
