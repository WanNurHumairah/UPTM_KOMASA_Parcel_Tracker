package com.example.komasaparceltracker

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class UsersFeedbacksActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var feedbackAdapter: FeedbackAdapter
    private lateinit var feedbackList: MutableList<String>
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users_feedbacks)

        // Initialize Firebase Realtime Database
        database = FirebaseDatabase.getInstance().reference.child("feedbacks")

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        feedbackList = mutableListOf()
        feedbackAdapter = FeedbackAdapter(feedbackList)
        recyclerView.adapter = feedbackAdapter

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }

        // Fetch feedback data from Firebase
        fetchFeedbackData()
    }

    private fun fetchFeedbackData() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                feedbackList.clear()
                for (feedbackSnapshot in dataSnapshot.children) {
                    val feedback = feedbackSnapshot.child("feedback").getValue(String::class.java)
                    feedback?.let { feedbackList.add(it) }
                }
                feedbackAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle onCancelled
            }
        })
    }
}
