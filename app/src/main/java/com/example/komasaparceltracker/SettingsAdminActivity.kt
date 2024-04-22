package com.example.komasaparceltracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout

class SettingsAdminActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings_admin)

        // Find the LinearLayout for feedback and set OnClickListener
        val feedbackLayout = findViewById<LinearLayout>(R.id.FeedbackView)
        feedbackLayout.setOnClickListener {
            // Navigate to UsersFeedbacksActivity
            startActivity(Intent(this@SettingsAdminActivity, UsersFeedbacksActivity::class.java))
        }

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }
    }
}
