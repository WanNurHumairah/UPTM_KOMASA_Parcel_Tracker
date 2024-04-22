package com.example.komasaparceltracker

import android.content.Intent
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.method.PasswordTransformationMethod
import android.text.style.ForegroundColorSpan
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import java.security.MessageDigest
import java.util.Locale
import android.util.Log

class SignupActivity : AppCompatActivity() {

    private lateinit var signupName: EditText
    private lateinit var signupID: EditText
    private lateinit var signupPhone: EditText
    private lateinit var signupEmail: EditText
    private lateinit var signupPassword: EditText
    private lateinit var loginRedirectText: TextView
    private lateinit var signupButton: Button
    private lateinit var database: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private var isPasswordVisible = false
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

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
        spannableString.setSpan(
            ForegroundColorSpan(blueColor),
            0,
            blueText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the color for the red part
        val redTextStart = fullText.indexOf(redText)
        val redTextEnd = redTextStart + redText.length
        spannableString.setSpan(
            ForegroundColorSpan(redColor),
            redTextStart,
            redTextEnd,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the color for the remaining text (assuming it's blue)
        spannableString.setSpan(
            ForegroundColorSpan(blueColor),
            redTextEnd,
            fullText.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        // Set the SpannableString to the TextView
        textView.text = spannableString

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Initialize Firebase Database reference
        database = FirebaseDatabase.getInstance()
        reference = database.getReference("users")

        signupName = findViewById(R.id.signup_name)
        signupPhone = findViewById(R.id.signup_phone)
        signupEmail = findViewById(R.id.signup_email)
        signupID = findViewById(R.id.signup_idNum)
        signupPassword = findViewById(R.id.signup_password)
        loginRedirectText = findViewById(R.id.loginRedirectText)
        signupButton = findViewById(R.id.signup_button)

        signupButton.setOnClickListener {
            val email = signupEmail.text.toString().trim()
            val password = signupPassword.text.toString().trim()
            val hashedPassword = hashPassword(password) // Hash the password

            if (email.isNotEmpty() && password.isNotEmpty()) {
                auth.createUserWithEmailAndPassword(email, hashedPassword)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val uid = user?.uid

                            // Get other user details
                            val name = capitalizeEachWord(signupName.text.toString().trim())
                            val phone = signupPhone.text.toString()
                            val idNumber = signupID.text.toString()

                            if (name.isNotEmpty() && phone.isNotEmpty() && idNumber.isNotEmpty()) {
                                // Ensure the ID is unique before adding to the database
                                reference.child(uid!!)
                                    .addListenerForSingleValueEvent(object : ValueEventListener {
                                        override fun onDataChange(snapshot: DataSnapshot) {
                                            if (!snapshot.exists()) {
                                                // UID is unique, proceed with user registration
                                                val helperClass = HelperClass(name, phone, idNumber, null, hashedPassword, uid) // Pass hashedPassword to HelperClass
                                                reference.child(uid).setValue(helperClass) // Store data with UID as key
                                                Toast.makeText(
                                                    this@SignupActivity,
                                                    "You have signed up successfully!",
                                                    Toast.LENGTH_LONG
                                                ).show()

                                                // Store FCM token
                                                registerUser(uid)

                                                // Launch the LoginActivity with user data
                                                val profileIntent = Intent(
                                                    this@SignupActivity,
                                                    LoginActivity::class.java
                                                )
                                                profileIntent.putExtra("name", name)
                                                profileIntent.putExtra("phone", phone)
                                                profileIntent.putExtra("idNumber", idNumber)
                                                profileIntent.putExtra("uid", uid) // Pass UID to LoginActivity
                                                startActivity(profileIntent)
                                            } else {
                                                signupID.error = "ID already exists"
                                                signupID.requestFocus()
                                            }
                                        }

                                        override fun onCancelled(error: DatabaseError) {
                                            // Handle onCancelled if needed
                                        }
                                    })
                            } else {
                                Toast.makeText(
                                    this@SignupActivity,
                                    "Please fill in all the fields",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Handle registration failure
                            Toast.makeText(
                                this@SignupActivity,
                                "Registration failed: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                // Email or password is empty
                Toast.makeText(
                    this@SignupActivity,
                    "Please enter email and password",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Set up password visibility toggle
        signupPassword.setOnTouchListener { _, event ->
            val drawableEnd = 2 // Index of the end drawable
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (signupPassword.right - signupPassword.compoundDrawables[drawableEnd].bounds.width())) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        loginRedirectText.setOnClickListener {
            val intent = Intent(this@SignupActivity, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    private fun registerUser(uid: String) {
        // Get the FCM token
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token ->
                // Store the FCM token in the database along with the user's UID
                storeFCMToken(uid, token)
            }
            .addOnFailureListener { exception ->
                // Handle failure to get FCM token
                Log.e(TAG, "Error getting FCM token: ${exception.message}")
            }
    }

    private fun storeFCMToken(uid: String, token: String) {
        // Store the FCM token in the database under the "users" node
        val userRef = database.getReference("users").child(uid)
        userRef.child("fcmToken").setValue(token)
            .addOnSuccessListener {
                Log.d(TAG, "FCM token stored successfully for user: $uid")
            }
            .addOnFailureListener { exception ->
                // Handle failure to store FCM token
                Log.e(TAG, "Error storing FCM token for user $uid: ${exception.message}")
            }
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible
        val drawableLock = ContextCompat.getDrawable(this, R.drawable.baseline_lock_24)
        val drawableVisibility =
            if (isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24

        // Set the lock drawable at the left
        signupPassword.setCompoundDrawablesWithIntrinsicBounds(
            drawableLock,
            null,
            ContextCompat.getDrawable(this, drawableVisibility),
            null
        )

        // Set the transformation method for password visibility
        signupPassword.transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
    }

    private fun hashPassword(password: String): String {
        val salt = "YourSaltStringHere"
        val saltedPassword = password + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    private fun capitalizeEachWord(str: String): String {
        val words = str.split(" ").toMutableList()
        for (i in words.indices) {
            if (words[i].isNotEmpty()) {
                words[i] = words[i][0].uppercaseChar() + words[i].substring(1)
                    .lowercase(Locale.getDefault())
            }
        }
        return words.joinToString(" ")
    }

    companion object {
        private const val TAG = "SignupActivity"
    }
}
