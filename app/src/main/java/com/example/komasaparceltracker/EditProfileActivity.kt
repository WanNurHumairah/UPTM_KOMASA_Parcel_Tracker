package com.example.komasaparceltracker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class EditProfileActivity : AppCompatActivity() {

    private lateinit var databaseReference: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Retrieve the UID from the intent
        val userId = intent.getStringExtra("userId") ?: ""

        // Initialize Firebase components
        databaseReference = FirebaseDatabase.getInstance().reference

        // Find EditText fields
        val editProfileName = findViewById<EditText>(R.id.editprofileName)
        val editProfilePhone = findViewById<EditText>(R.id.editprofilePhone)
        val editProfileidNum = findViewById<EditText>(R.id.editprofileidNum)

        // Retrieve current user data from Firebase
        retrieveCurrentUserData(userId, editProfileName, editProfilePhone, editProfileidNum)

        // Set up Save button
        val saveButton = findViewById<Button>(R.id.saveButton)
        saveButton.setOnClickListener {
            val name = editProfileName.text.toString().trim()
            val phone = editProfilePhone.text.toString().trim()
            val idNumber = editProfileidNum.text.toString().trim()
            updateProfileData(userId, name, phone, idNumber)
        }

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }
    }

    private fun retrieveCurrentUserData(
        userId: String,
        editProfileName: EditText,
        editProfilePhone: EditText,
        editProfileidNum: EditText
    ) {
        val userRef = databaseReference.child("users").child(userId)
        userRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val name = dataSnapshot.child("name").getValue(String::class.java)
                val phone = dataSnapshot.child("phone").getValue(String::class.java)
                val idNumber = dataSnapshot.child("idNumber").getValue(String::class.java)

                editProfileName.setText(name)
                editProfilePhone.setText(phone)
                editProfileidNum.setText(idNumber)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors
            }
        })
    }

    private fun updateProfileData(userId: String, name: String, phone: String, idNumber: String) {
        val userRef = databaseReference.child("users").child(userId)
        val userData = HashMap<String, Any>()
        userData["name"] = name
        userData["phone"] = phone
        userData["idNumber"] = idNumber

        userRef.updateChildren(userData).addOnCompleteListener { task ->
            if (task.isSuccessful) {

                Toast.makeText(
                    this@EditProfileActivity,
                    "Profile updated successfully",
                    Toast.LENGTH_SHORT
                ).show()

// Create an Intent to navigate back to the ProfileActivity
                val intent = Intent(this@EditProfileActivity, ProfileActivity::class.java)
// Pass the UID to the ProfileActivity
                intent.putExtra("userId", userId)
// Start the ProfileActivity
                startActivity(intent)
// Finish the EditProfileActivity to prevent navigating back to it using the back button
                finish()
            } else {
                Toast.makeText(
                    this@EditProfileActivity,
                    "Failed to update profile",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}


