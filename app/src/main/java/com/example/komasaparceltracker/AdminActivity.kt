package com.example.komasaparceltracker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin)

        // Find the TextView for today's date
        val todaysDateTextView: TextView = findViewById(R.id.todaysDate)

        // Get the current date and format it
        val currentDate = getCurrentDate()

        // Set the formatted date to the TextView
        todaysDateTextView.text = currentDate

        // Find the CardView by its ID
        val trackCardAdmin: CardView = findViewById(R.id.trackCardAdmin)
        val usersCard: CardView = findViewById(R.id.usersCard)
        val statusCard: CardView = findViewById(R.id.statusCard)
        val settingsCard: CardView = findViewById(R.id.settingsCard)
        val logoutCard: CardView = findViewById(R.id.logoutCard)


        trackCardAdmin.setOnClickListener {
            // Create an intent to launch the TrackActivity
            val intent = Intent(this@AdminActivity, TrackAdminActivity::class.java)
            startActivity(intent)
        }

        usersCard.setOnClickListener {
            // Create an intent to launch the TrackActivity
            val intent = Intent(this@AdminActivity, UsersActivity::class.java)
            startActivity(intent)
        }

        statusCard.setOnClickListener {
            // Create an intent to launch the TrackActivity
            val intent = Intent(this@AdminActivity, AdminParcelStatusActivity::class.java)
            startActivity(intent)
        }

        settingsCard.setOnClickListener {
            // Create an intent to launch the TrackActivity
            val intent = Intent(this@AdminActivity, SettingsAdminActivity::class.java)
            startActivity(intent)
        }

        logoutCard.setOnClickListener {
            // Create an intent to navigate back to the login screen
            val intent = Intent(this@AdminActivity, LoginActivity::class.java)

            // Add flags to clear the activity stack so that user cannot navigate back to MainActivity2 using back button
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Start the LoginActivity and finish the current activity
            startActivity(intent)
            finish()
        }
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
    }
}