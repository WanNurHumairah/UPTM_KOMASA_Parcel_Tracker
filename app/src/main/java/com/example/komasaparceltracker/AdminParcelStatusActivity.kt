package com.example.komasaparceltracker

import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*

class AdminParcelStatusActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: AdminParcelAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_parcel_status)

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        dbRef = FirebaseDatabase.getInstance().reference.child("users")

        fetchCollectedParcels()
    }

    private fun fetchCollectedParcels() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val collectedParcels = mutableListOf<String>()
                for (userSnapshot in dataSnapshot.children) {
                    val userName = userSnapshot.child("name").getValue(String::class.java)
                    val collectedParcelsSnapshot = userSnapshot.child("collectedParcels")
                    for (parcelSnapshot in collectedParcelsSnapshot.children) {
                        val trackingNumber = parcelSnapshot.child("trackingNumber").getValue(String::class.java)
                        val status = parcelSnapshot.child("status").getValue(String::class.java)
                        val parcelInfo = "$userName\nTracking Number: $trackingNumber\nStatus: $status"
                        collectedParcels.add(parcelInfo)
                    }
                }
                if (::adapter.isInitialized) {
                    adapter.updateData(collectedParcels)
                } else {
                    adapter = AdminParcelAdapter(collectedParcels)
                    recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle database error
            }
        })
    }
}
