package com.example.komasaparceltracker

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.content.Context
import com.google.firebase.database.*

class MainActivity2 : AppCompatActivity() {

    private lateinit var nameTextView: TextView
    private lateinit var databaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        // Initialize the database reference
        databaseReference = FirebaseDatabase.getInstance().reference

        // Retrieve user's ID and name from the intent
        val userId = intent.getStringExtra("uid")
        var userName = intent.getStringExtra("name")

        // If userName is null (coming back from ProfileActivity), retrieve it from SharedPreferences
        if (userName == null) {
            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            userName = sharedPref.getString("userName", "")
        } else {
            // Save the userName to SharedPreferences if it's not null
            val sharedPref = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putString("userName", userName)
            editor.apply()
        }

        // Initialize nameTextView
        nameTextView = findViewById(R.id.nameuser)

        // Set the user's name to the TextView
        nameTextView.text = userName

        // Call method to retrieve and set name to TextView
        retrieveNameFromFirebase(userId)

        // Find the TextView for today's date
        val todaysDateTextView: TextView = findViewById(R.id.todaysDate)

        // Get the current date and format it
        val currentDate = getCurrentDate()

        // Set the formatted date to the TextView
        todaysDateTextView.text = currentDate



        // Find the CardView by its ID
        val profileCard: CardView = findViewById(R.id.profileCard)
        val trackCard: CardView = findViewById(R.id.trackCard)

        val historyCard: CardView = findViewById(R.id.historyCard)
        val settingsCard: CardView = findViewById(R.id.settingsCard)
        val confirmCard: CardView = findViewById(R.id.confirmCard)
        val logoutCard: CardView = findViewById(R.id.logoutCard)

        profileCard.setOnClickListener {
            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("uid")
            // Create an intent to launch the TrackActivity
            val intent = Intent(this@MainActivity2, ProfileActivity::class.java)
            intent.putExtra("userId", userId) // Pass the user ID to TrackActivity
            startActivity(intent)
        }

        trackCard.setOnClickListener {
            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("uid")
            // Create an intent to launch the TrackActivity
            val intent = Intent(this@MainActivity2, TrackActivity::class.java)
            intent.putExtra("userId", userId) // Pass the user ID to TrackActivity
            startActivity(intent)
        }

        historyCard.setOnClickListener {
            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("uid")
            // Create an intent to launch the HistoryActivity
            val intent = Intent(this@MainActivity2, HistoryActivity::class.java)
            intent.putExtra("userId", userId) // Pass the user ID to TrackActivity
            startActivity(intent)
        }

        confirmCard.setOnClickListener {
            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("uid")
            // Create an intent to launch the ConfirmParcelActivity
            val intent = Intent(this@MainActivity2, ConfirmParcelActivity::class.java)
            intent.putExtra("userId", userId) // Pass the user ID to TrackActivity
            startActivity(intent)
        }

        settingsCard.setOnClickListener {
            // Retrieve the user ID passed from LoginActivity
            val userId = intent.getStringExtra("uid")
            // Create an intent to launch the SettingsActivity
            val intent = Intent(this@MainActivity2, SettingsActivity::class.java)
            intent.putExtra("userId", userId) // Pass the user ID to TrackActivity
            startActivity(intent)
        }


        logoutCard.setOnClickListener {
            // Create an intent to navigate back to the login screen
            val intent = Intent(this@MainActivity2, LoginActivity::class.java)

            // Add flags to clear the activity stack so that user cannot navigate back to MainActivity2 using back button
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            // Start the LoginActivity and finish the current activity
            startActivity(intent)
            finish()
        }

    }

    private fun retrieveNameFromFirebase(userId: String?) {
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

                // Update UI with the new data
                displayUserName(userName)
            }


            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun displayUserName(name: String?) {
        // Find the TextViews for user's details
        val nameTextView: TextView = findViewById(R.id.nameuser)


        // Set the user's details to the respective TextViews
        nameTextView.text = name


        // Set the user's name to the TextView with the first letter of each word capitalized
        nameTextView.text = capitalizeEachWord(name)
    }


    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault())
        return dateFormat.format(Date())
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
