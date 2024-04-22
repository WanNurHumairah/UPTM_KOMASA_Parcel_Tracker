package com.example.komasaparceltracker

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
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

class AdminLoginActivity : AppCompatActivity() {

    private lateinit var loginID: EditText
    private lateinit var loginPassword: EditText
    private lateinit var adminloginButton: Button
    private lateinit var userRedirectText: TextView
    private var isPasswordVisible = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_login)

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

        //login
        loginID = findViewById(R.id.login_idNum)
        loginPassword = findViewById(R.id.login_password)
        adminloginButton = findViewById(R.id.admin_login_button)
        userRedirectText = findViewById(R.id.userRedirectText)

        // Set click listener for admin login button
        adminloginButton.setOnClickListener {
            if (!validateID() || !validatePassword()) {
                // Handle invalid input
            } else {
                checkUser()
            }
        }

        userRedirectText.setOnClickListener {
            val intent = Intent(this@AdminLoginActivity, LoginActivity::class.java)
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
        val drawableVisibility =
            if (isPasswordVisible) R.drawable.baseline_visibility_24 else R.drawable.baseline_visibility_off_24

        // Set the lock drawable at the left
        loginPassword.setCompoundDrawablesWithIntrinsicBounds(
            drawableLock,
            null,
            ContextCompat.getDrawable(this, drawableVisibility),
            null
        )

        // Set the transformation method for password visibility
        loginPassword.transformationMethod =
            if (isPasswordVisible) null else PasswordTransformationMethod.getInstance()
    }

    private fun hashPassword(password: String): String {
        val salt = "YourSaltStringHere" // Add a salt for extra security
        val saltedPassword = password + salt
        val bytes = MessageDigest.getInstance("SHA-256").digest(saltedPassword.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
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

    private fun checkUser() {
        val enteredAdminID = loginID.text.toString().trim()
        val enteredAdminPassword = loginPassword.text.toString().trim()

        // Hash the entered password
        val hashedPassword = hashPassword(enteredAdminPassword)

        // Proceed with the regular user check
        val reference: DatabaseReference = FirebaseDatabase.getInstance().getReference("admins")
        val checkUserDatabase: Query = reference.orderByChild("adminID").equalTo(enteredAdminID)

        checkUserDatabase.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (adminSnapshot in snapshot.children) {
                        val adminIDFromDB = adminSnapshot.child("adminID").getValue(String::class.java)
                        val passwordFromDB = adminSnapshot.child("password").getValue(String::class.java)
                        val uidFromDB = adminSnapshot.child("uid").getValue(String::class.java)

                        // Check if entered admin ID matches the one stored in the database
                        if (enteredAdminID == adminIDFromDB) {
                            // Hash the password from database for comparison
                            val hashedPasswordFromDB = hashPassword(passwordFromDB ?: "")

                            // Check if entered password matches the one stored in the database
                            if (hashedPassword == hashedPasswordFromDB) {
                                // Check if UID is not null
                                if (uidFromDB != null) {
                                    // Navigate to admin section
                                    val adminIntent = Intent(this@AdminLoginActivity, AdminActivity::class.java)
                                    startActivity(adminIntent)
                                    return
                                }
                            } else {
                                // If password doesn't match, show invalid password error
                                loginPassword.error = "Invalid Password"
                                loginPassword.requestFocus()
                                return
                            }
                        }
                    }
                }
                // If the loop completes without finding a match, show invalid user error
                loginID.error = "Invalid User"
                loginID.requestFocus()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle onCancelled if needed
                Toast.makeText(applicationContext, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}