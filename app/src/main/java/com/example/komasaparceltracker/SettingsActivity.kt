package com.example.komasaparceltracker

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.TextView
import android.content.DialogInterface
import android.widget.EditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider

class SettingsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var passwordEditText : EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance()

        // Retrieve the passed user ID from the intent
        val userId = intent.getStringExtra("userId")

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }

        // Find the "Contact Us" TextView
        val contactUsTextView = findViewById<TextView>(R.id.contactUsTextView)

        // Set OnClickListener for the "Contact Us" TextView
        contactUsTextView.setOnClickListener {
            // Show the pop-up box with details
            showContactUsDialog()
        }

        // Find the "Contact Us" TextView
        val FeedbacksTextView = findViewById<TextView>(R.id.FeedbacksTextView)

        // Set OnClickListener for the "Contact Us" TextView
        FeedbacksTextView.setOnClickListener {
            // Show the pop-up box with details
            showFeedbackDialog()
        }
    }

    private fun showContactUsDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setTitle("Contact Us")
        dialogBuilder.setMessage("Please contact us at email 'komasaberhad@gmail.com' for assistance.")
        dialogBuilder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


    private fun showFeedbackDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_feedback, null)
        val feedbackEditText = dialogView.findViewById<EditText>(R.id.feedbackEditText)
        dialogBuilder.setView(dialogView)
        dialogBuilder.setTitle("Send Feedback")
        dialogBuilder.setPositiveButton("Submit") { dialog, _ ->
            val feedback = feedbackEditText.text.toString().trim()
            if (feedback.isNotEmpty()) {
                // Save the feedback to Firebase Realtime Database
                saveFeedbackToDatabase(feedback)
                showToast("Feedback submitted successfully.")
            } else {
                showToast("Please enter your feedback.")
            }
            dialog.dismiss()
        }
        dialogBuilder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()
        }
        val dialog = dialogBuilder.create()
        dialog.show()
    }

    private fun saveFeedbackToDatabase(feedback: String) {
        // Get the current user ID from Firebase Authentication
        val userId = intent.getStringExtra("userId")

        if (userId != null) {
            // Get a reference to the "feedbacks" node in Firebase Realtime Database
            val feedbackRef = database.reference.child("feedbacks")

            // Generate a unique key for the feedback
            val feedbackId = feedbackRef.push().key ?: ""

            // Create a map to store the feedback data
            val feedbackData = mapOf(
                "userId" to userId,
                "feedback" to feedback
            )

            // Save the feedback data to the database using the generated key
            feedbackRef.child(feedbackId).setValue(feedbackData)
                .addOnSuccessListener {
                    // Feedback saved successfully
                    // You can perform any additional actions here
                }
                .addOnFailureListener { exception ->
                    // Handle the failure to save feedback
                    showToast("Failed to submit feedback: ${exception.message}")
                }
        } else {
            showToast("User ID not available. Please sign in again.")
        }
    }

}