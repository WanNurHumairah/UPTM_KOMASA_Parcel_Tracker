package com.example.komasaparceltracker

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    // Reference to your Firebase Realtime Database
    private lateinit var databaseReference: DatabaseReference
    private lateinit var editButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().reference

// Retrieve user's ID from the intent
        val userId = intent.getStringExtra("userId")

// Retrieve user's details from Firebase Realtime Database using the retrieved user ID
        retrieveUserDetailsFromDatabase(userId)

// Set up the Edit button
        editButton = findViewById(R.id.editButton)
        editButton.setOnClickListener {
            // Create an Intent to launch the EditProfileActivity
            val intent = Intent(this@ProfileActivity, EditProfileActivity::class.java)
            // Pass the UID to EditProfileActivity
            intent.putExtra("userId", userId)
            // Start the EditProfileActivity
            startActivity(intent)
        }

// Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

// Set OnClickListener for the backButton
        backButton.setOnClickListener {
            // Create an intent to navigate back to MainActivity2
            val intent = Intent(this@ProfileActivity, MainActivity2::class.java)
            // Add flags to clear the activity stack
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            // Finish the ProfileActivity to prevent navigating back to it using the back button
            finish()
        }
    }


    private fun retrieveUserDetailsFromDatabase(userId: String?) {
        if (userId.isNullOrEmpty()) {
            // Handle the case where user ID is not provided
            return
        }

        // Get a reference to the "users" node in your database
        val usersRef = databaseReference.child("users")

        // Attach a listener to retrieve the data
        usersRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get the user's details from the dataSnapshot
                val userName = dataSnapshot.child("name").getValue(String::class.java)
                val userPhone = dataSnapshot.child("phone").getValue(String::class.java)
                val userIdNumber = dataSnapshot.child("idNumber").getValue(String::class.java)

                // Update UI with the new data
                displayUserDetails(userName, userPhone, userIdNumber)
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun displayUserDetails(name: String?, phone: String?, idNumber: String?) {
        // Find the TextViews for user's details
        val nameTextView: TextView = findViewById(R.id.profileName)
        val phoneTextView: TextView = findViewById(R.id.profilePhone)
        val idNumberTextView: TextView = findViewById(R.id.profileidNum)

        // Set the user's details to the respective TextViews
        nameTextView.text = name
        phoneTextView.text = phone
        idNumberTextView.text = idNumber

        // Set the user's name to the TextView with the first letter of each word capitalized
        nameTextView.text = capitalizeEachWord(name)
    }


    // Function to capitalize the first letter of each word in a string
    private fun capitalizeEachWord(input: String?): String {
        if (input.isNullOrBlank()) return ""

        val words = input.split(" ")
        val capitalizedWords = words.map {
            it.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }

        return capitalizedWords.joinToString(" ")
    }
}
