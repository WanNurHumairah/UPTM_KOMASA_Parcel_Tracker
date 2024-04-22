package com.example.komasaparceltracker

import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import android.app.AlertDialog


class ParcelStatusActivity : AppCompatActivity(), TrackingAdapterUser.OnDeleteButtonClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackingAdapterUser
    private val trackingInfoList = mutableListOf<TrackingInfo>() // Initialize with empty list
    private lateinit var dbRef: DatabaseReference // Firebase Database reference
    private lateinit var userId: String // Add userId variable here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_parcel_status)

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }

        userId = intent.getStringExtra("userId") ?: ""

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list and listener
        adapter = TrackingAdapterUser(trackingInfoList, this, this)

        // Set the adapter to the RecyclerView
        recyclerView.adapter = adapter

        // Get a reference to the user's tracking numbers in the database
        dbRef = FirebaseDatabase.getInstance().reference.child("users").child(userId)

        // Fetch the tracking numbers for the current user
        fetchTrackingNumbers()
    }

    private fun fetchTrackingNumbers() {
        // Clear the list before adding new data
        trackingInfoList.clear()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Fetch data from trackingNumbers node
                for (trackingSnapshot in dataSnapshot.child("trackingNumbers").children) {
                    val trackingNumber =
                        trackingSnapshot.child("trackingNumber").getValue(String::class.java)
                    val status = trackingSnapshot.child("status").getValue(String::class.java)
                    val timestamp = trackingSnapshot.child("timestamp").getValue(Long::class.java)

                    // Create a TrackingInfo object and add it to the list
                    trackingNumber?.let {
                        val trackingInfo = TrackingInfo(it, status ?: "Pending", timestamp ?: 0)
                        trackingInfoList.add(trackingInfo)
                    }
                }

// Fetch data from arrivedParcels node
                for (arrivedParcelSnapshot in dataSnapshot.child("arrivedParcels").children) {
                    val trackingNumber =
                        arrivedParcelSnapshot.child("trackingNumber").getValue(String::class.java)
                    val status = arrivedParcelSnapshot.child("status").getValue(String::class.java)
                    val timestamp = arrivedParcelSnapshot.child("timestamp").getValue(Long::class.java)
                    val arrivalConfirmationTimestamp = arrivedParcelSnapshot.child("arrivalConfirmationTimestamp").getValue(Long::class.java)

                    // Create a TrackingInfo object and add it to the list
                    trackingNumber?.let {
                        val trackingInfo = TrackingInfo(it, status ?: "Arrived", timestamp ?: 0, arrivalConfirmationTimestamp ?: 0)
                        trackingInfoList.add(trackingInfo)
                    }
                }


                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "FetchTrackingNumbers",
                    "Error fetching tracking numbers: ${databaseError.message}"
                )
            }
        })
    }

    override fun onDeleteButtonClick(trackingNumber: String) {
        // Show confirmation dialog
        showDeleteConfirmationDialog(trackingNumber)
    }

    private fun showDeleteConfirmationDialog(trackingNumber: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Deletion")
            .setMessage("Are you sure you want to delete this tracking number?")
            .setPositiveButton("Delete") { dialog, which ->
                // User confirmed deletion, proceed with deleting the tracking number
                deleteTrackingNumber(trackingNumber)
                deleteArrivedParcel(trackingNumber) // Call deleteArrivedParcel here
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User cancelled, do nothing
            }
            .show()
    }

    private fun deleteTrackingNumber(trackingNumber: String) {
        val trackingNumbersRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("trackingNumbers")

        trackingNumbersRef.orderByChild("trackingNumber").equalTo(trackingNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                // Remove the item from the list and notify the adapter
                                val index = trackingInfoList.indexOfFirst { it.trackingNumber == trackingNumber }
                                if (index != -1) {
                                    trackingInfoList.removeAt(index)
                                    adapter.notifyItemRemoved(index)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("onDeleteButtonClick", "Error deleting tracking number: $trackingNumber, ${e.message}")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("onDeleteButtonClick", "Query cancelled: ${databaseError.message}")
                }
            })
    }

    private fun deleteArrivedParcel(trackingNumber: String) {
        val arrivedParcelsRef = FirebaseDatabase.getInstance().reference
            .child("users")
            .child(userId)
            .child("arrivedParcels")

        arrivedParcelsRef.orderByChild("trackingNumber").equalTo(trackingNumber)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    for (snapshot in dataSnapshot.children) {
                        snapshot.ref.removeValue()
                            .addOnSuccessListener {
                                // Remove the item from the list and notify the adapter
                                val index = trackingInfoList.indexOfFirst { it.trackingNumber == trackingNumber }
                                if (index != -1) {
                                    trackingInfoList.removeAt(index)
                                    adapter.notifyItemRemoved(index)
                                }
                            }
                            .addOnFailureListener { e ->
                                Log.e("onDeleteButtonClick", "Error deleting arrived parcel: $trackingNumber, ${e.message}")
                            }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.e("onDeleteButtonClick", "Query cancelled: ${databaseError.message}")
                }
            })
    }

}
