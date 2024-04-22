package com.example.komasaparceltracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class HistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        // Retrieve user ID from intent
        userId = intent.getStringExtra("userId") ?: ""

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter()
        recyclerView.adapter = adapter

        // Initialize Firebase database reference
        dbRef = FirebaseDatabase.getInstance().reference

        // Fetch collected parcel data
        fetchCollectedParcels()
    }

    private fun fetchCollectedParcels() {
        // Reference to the "collectedParcels" node under the current user's node
        val collectedParcelsRef = dbRef.child("users").child(userId).child("collectedParcels")

        // Add a ValueEventListener to fetch data
        collectedParcelsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val collectedParcelsList = mutableListOf<CollectedParcel>()

                for (parcelSnapshot in dataSnapshot.children) {
                    val trackingNumber = parcelSnapshot.child("trackingNumber").getValue(String::class.java)
                    val status = parcelSnapshot.child("status").getValue(String::class.java)
                    val arrivalConfirmationTimestamp = parcelSnapshot.child("arrivalConfirmationTimestamp").getValue(Long::class.java)

                    trackingNumber?.let { tn ->
                        status?.let { st ->
                            arrivalConfirmationTimestamp?.let { ts ->
                                val parcel = CollectedParcel(tn, st, ts)
                                collectedParcelsList.add(parcel)
                            }
                        }
                    }
                }

                // Update RecyclerView with collected parcel data
                adapter.submitList(collectedParcelsList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle error
            }
        })
    }
}
