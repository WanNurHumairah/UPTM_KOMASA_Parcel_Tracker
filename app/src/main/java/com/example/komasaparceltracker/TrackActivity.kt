package com.example.komasaparceltracker

import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*

class TrackActivity : AppCompatActivity() {

    private lateinit var trackingNumberEditText: EditText
    private lateinit var dbRef: DatabaseReference
    private lateinit var trackButton: Button
    private lateinit var viewButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track)

        trackingNumberEditText = findViewById(R.id.trackingNumberEditText)
        dbRef = FirebaseDatabase.getInstance().reference.child("users")
        trackButton = findViewById(R.id.trackButton)
        viewButton = findViewById(R.id.viewButton)

        // Automatically copy text from clipboard when activity starts
        copyTextFromClipboard()

        trackButton.setOnClickListener {
            // Get the entered tracking number
            val trackingNumber = trackingNumberEditText.text.toString()

            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("userId")

            // Call function to save tracking number
            saveData(userId!!, trackingNumber)
        }

        viewButton.setOnClickListener {
            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("userId")
            // Create an intent to launch the ParcelStatusActivity
            val intent = Intent(this@TrackActivity, ParcelStatusActivity::class.java)
            intent.putExtra("userId", userId)
            startActivity(intent)
        }

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }
    }

    private fun copyTextFromClipboard() {
        try {
            val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = clipboardManager.primaryClip
            if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text.toString()
                trackingNumberEditText.setText(text)
                Toast.makeText(this, "Text copied from clipboard", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to copy text from clipboard", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveData(userId: String?, trackingNumber: String) {
        if (trackingNumber.isBlank()) {
            // If tracking number is empty, show error message
            trackingNumberEditText.error = "Please enter a valid tracking number"
            trackingNumberEditText.requestFocus()

            // Use Handler to delay clearing the error message after a few seconds
            val handler = Handler()
            handler.postDelayed({
                trackingNumberEditText.error = null // Clear the error message
            }, 3000) // Delay in milliseconds (e.g., 3000 for 3 seconds)

            return
        }

        // Check if the userId is not null or empty
        if (!userId.isNullOrEmpty()) {
            // Get a reference to the user's node in the database
            val userRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

            // Reference the 'trackingNumbers' node under the user's node
            val trackingNumbersRef = userRef.child("trackingNumbers")

            // Retrieve the number of tracking numbers currently stored under the user
            trackingNumbersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    var isExisting = false
                    // Check if the tracking number already exists
                    for (trackingSnapshot in dataSnapshot.children) {
                        val existingTrackingNumber = trackingSnapshot.child("trackingNumber").getValue(String::class.java)
                        if (existingTrackingNumber == trackingNumber) {
                            isExisting = true
                            break
                        }
                    }

                    if (isExisting) {
                        // Tracking number already exists for this user
                        Toast.makeText(
                            this@TrackActivity,
                            "Tracking number already exists for this user",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // Save the new tracking number
                        val count = dataSnapshot.childrenCount.toInt()
                        val key = "trackingNumber${count + 1}"
                        val parcelStatus = "Pending"
                        val timestamp = System.currentTimeMillis()
                        val parcelData = mapOf(
                            "trackingNumber" to trackingNumber,
                            "status" to parcelStatus,
                            "timestamp" to timestamp
                        )

                        trackingNumbersRef.child(key).setValue(parcelData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    // Tracking number saved successfully
                                    Toast.makeText(
                                        this@TrackActivity,
                                        "Tracking number saved successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Reset the trackingNumberEditText
                                    trackingNumberEditText.text.clear()
                                } else {
                                    // Failed to save tracking number
                                    Toast.makeText(
                                        this@TrackActivity,
                                        "Failed to save tracking number",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("SaveData", "Error saving tracking number: ${databaseError.message}")
                }
            })
        } else {
            // Handle the case where userId is null or empty
            Toast.makeText(this@TrackActivity, "User ID not found", Toast.LENGTH_SHORT).show()
        }
    }
}
