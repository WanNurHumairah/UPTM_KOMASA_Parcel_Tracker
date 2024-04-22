package com.example.komasaparceltracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.text.method.PasswordTransformationMethod
import android.view.MotionEvent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.database.*
import java.security.MessageDigest
import com.google.firebase.messaging.FirebaseMessaging
import android.util.Log
import android.net.Uri
import android.view.View

class LoginActivity : AppCompatActivity() {

    private lateinit var loginID: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupRedirectText: TextView
    private lateinit var adminRedirectText: TextView
    private var isPasswordVisible = false


    companion object {
        private const val TAG = "LoginActivity"
    }

    private fun hashPassword(password: String): String {
        val salt = "YourSaltStringHere" // Add a salt for extra security
        val saltedPassword = password + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // In your onCreate() method or wherever appropriate
        val youtubeLink = findViewById<TextView>(R.id.youtubeLink)

        youtubeLink.setOnClickListener {
            // Define the YouTube link
            val youtubeUrl = "https://youtu.be/srQzS2a7WwI?si=sh1Z6gulcQv4Swle"

            try {
                // Create an Intent with the YouTube link
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(youtubeUrl))

                // Start the Intent
                startActivity(intent)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening YouTube link: ${e.message}")
            }
        }


        // Retrieve UID from shared preferences
        val sharedPref = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("UID", "") ?: ""

        val textView: TextView = findViewById(R.id.textView)
        val fullText: String = textView.text.toString()

        // Define the text parts and their colors
        val blueText = "UNIVERSITI MALAYSIA"
        val redText = "POLY-TECH"

        // Assuming you have the colors defined in your resources
        val blueColor = ContextCompat.getColor(this, R.color.blueuptm)
        val redColor = ContextCompat.getColor(this, R.color.reduptm)

        // Create a SpannableString
        val spannableString = SpannableString(fullText)

        // Set the color for the blue part
        spannableString.setSpan(ForegroundColorSpan(blueColor), 0, blueText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the color for the red part
        val redTextStart = fullText.indexOf(redText)
        val redTextEnd = redTextStart + redText.length
        spannableString.setSpan(ForegroundColorSpan(redColor), redTextStart, redTextEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the color for the remaining text (assuming it's blue)
        spannableString.setSpan(ForegroundColorSpan(blueColor), redTextEnd, fullText.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

        // Set the SpannableString to the TextView
        textView.text = spannableString

        //login
        loginID = findViewById(R.id.login_idNum)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupRedirectText = findViewById(R.id.signupRedirectText)
        adminRedirectText = findViewById(R.id.adminRedirectText)

        loginButton.setOnClickListener {
            if (!validateID() || !validatePassword()) {
                // Handle invalid input
            } else {
                checkUser()
            }
        }

        signupRedirectText.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignupActivity::class.java)
            startActivity(intent)
        }

        adminRedirectText.setOnClickListener {
            val intent = Intent(this@LoginActivity, AdminLoginActivity::class.java)
           startActivity(intent)
        }

        // Set up password visibility toggle
        loginPassword.setOnTouchListener { _, event ->
            val drawableEnd = 2 // Index of the end drawable
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (loginPassword.right - loginPassword.compoundDrawables[drawableEnd].bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val drawableLock = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24)
        val drawableVisibility = if (isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24

        // Set the lock drawable at the left
        loginPassword.setCompoundDrawablesWithIntrinsicBounds(drawableLock, null, ContextCompat.getDrawable(this, drawableVisibility), null)

        // Set the transformation method for password visibility
        loginPassword.transformationMethod = if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
    }


    private fun validateID(): Boolean {
        val idNumber = loginID.text.toString().trim()
        return if (idNumber.isEmpty()) {
            loginID.error = "Please enter your ID number"
            false
        } else {
            loginID.error = null
            true
        }
    }

    private fun validatePassword(): Boolean {
        val password = loginPassword.text.toString().trim()
        return if (password.isEmpty()) {
            loginPassword.error = "Please enter your password"
            false
        } else {
            loginPassword.error = null
            true
        }
    }

    // Function to save UID to SharedPreferences
    private fun saveUidToSharedPreferences(uid: String?) {
        val sharedPref = getSharedPreferences("MySharedPref", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("UID", uid ?: "") // Provide a default empty string if uid is null
        editor.apply()
    }

    // Function to store FCM token in the database
    private fun storeFCMToken(uid: String, token: String) {
        val databaseReference = FirebaseDatabase.getInstance().getReference("users").child(uid)
        databaseReference.child("fcmToken").setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token stored successfully for user: $uid")
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Error storing FCM token for user $uid: ${exception.message}")
            }
    }

    private fun checkUser() {
        val userID = loginID.text.toString().trim()
        val userPassword = loginPassword.text.toString().trim()

        // Hash the entered password
        val hashedPassword = hashPassword(userPassword)

        // Proceed with the regular user check
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
        val checkUserDatabase: Query = reference.orderByChild("idNumber").equalTo(userID)

        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val passwordFromDB = userSnapshot.child("password").getValue(String::class.java)
                        val uid = userSnapshot.key // Retrieve UID of the user

                        if (passwordFromDB == hashedPassword) {
                            // Get the FCM token
                            FirebaseMessaging.getInstance().token
                                .addOnSuccessListener { token ->
                                    // Store the FCM token in the database along with the user's UID
                                    storeFCMToken(uid!!, token)

                                    saveUidToSharedPreferences(uid)

                                    // Login successful
                                    val nameFromDB = userSnapshot.child("name").getValue(String::class.java)

                                    // Navigate to regular user page
                                    val userIntent = Intent(this@LoginActivity, MainActivity2::class.java)
                                    userIntent.putExtra("name", nameFromDB) // Pass the user's name to MainActivity2
                                    userIntent.putExtra("uid", uid) // Pass UID to the next activity
                                    startActivity(userIntent)
                                }
                                .addOnFailureListener { exception ->
                                    // Handle failure to get FCM token
                                    Log.e(TAG, "Error getting FCM token: ${exception.message}")
                                }
                            return // Exit the loop after finding a matching user
                        }
                    }
                    // If the loop completes without finding a match, show invalid password error
                    loginPassword.error = "Invalid Password"
                    loginPassword.requestFocus()
                } else {
                    // User does not exist
                    loginID.error = "User does not exist"
                    loginID.requestFocus()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
            }
        })
    }
}
