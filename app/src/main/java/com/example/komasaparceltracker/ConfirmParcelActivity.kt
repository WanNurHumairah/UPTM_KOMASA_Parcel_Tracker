package com.example.komasaparceltracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import com.google.firebase.database.*
import android.app.AlertDialog
import android.widget.Toast
import android.widget.Button

class ConfirmParcelActivity : AppCompatActivity() {

    private lateinit var trackingInfoList: MutableList<TrackingInfo>
    private lateinit var adapter: ConfirmAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var userId: String // Add userId variable here

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_confirm_parcel)

        // Retrieve the user ID from the intent
        userId = intent.getStringExtra("userId") ?: ""

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }

        // Initialize trackingInfoList
        trackingInfoList = mutableListOf()

        // Initialize adapter
        adapter = ConfirmAdapter(this, trackingInfoList)

        // Initialize RecyclerView
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Initialize dbRef
        dbRef = FirebaseDatabase.getInstance().reference

        // Fetch arrived parcels data
        fetchArrivedParcels()

        // Set click listener for collect button in adapter
        adapter.setOnCollectButtonClickListener(object : ConfirmAdapter.OnCollectButtonClickListener {
            override fun onCollectButtonClick(trackingNumber: String) {
                showConfirmationDialog(trackingNumber)
            }
        })
    }

    private fun fetchArrivedParcels() {
        // Query the "arrivedParcels" node under the current user's node
        val arrivedParcelsRef = dbRef.child("users").child(userId).child("arrivedParcels")

        // Add a ValueEventListener to fetch data
        arrivedParcelsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the list before adding new data
                trackingInfoList.clear()

                // Iterate through the children of the "arrivedParcels" node
                for (arrivedParcelSnapshot in dataSnapshot.children) {
                    // Retrieve parcel data
                    val trackingNumber = arrivedParcelSnapshot.child("trackingNumber").getValue(String::class.java)

                    // Create a TrackingInfo object and add it to the list
                    trackingNumber?.let {
                        val trackingInfo = TrackingInfo(it, "Arrived")
                        trackingInfoList.add(trackingInfo)
                    }
                }

                // Notify the adapter that the data set has changed
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "FetchArrivedParcels",
                    "Error fetching arrived parcels: ${databaseError.message}"
                )
            }
        })
    }

    // Function to show a confirmation dialog
    private fun showConfirmationDialog(trackingNumber: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Collect Parcel")
            .setMessage("Are you sure you want to collect this parcel?")
            .setPositiveButton("Yes") { dialog, which ->
                // User clicked Yes, proceed to collect the parcel
                collectParcel(trackingNumber)
            }
            .setNegativeButton("No") { dialog, which ->
                // User clicked No, do nothing
            }
            .show()
    }

    // Function to collect the parcel
    private fun collectParcel(trackingNumber: String) {
        // Get a reference to the "arrivedParcels" node under the current user's node
        val arrivedParcelsRef = dbRef.child("users").child(userId).child("arrivedParcels")

        // Query the specific parcel by its tracking number
        val query = arrivedParcelsRef.orderByChild("trackingNumber").equalTo(trackingNumber)

        // Add a ValueEventListener to fetch data
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Iterate through the children (should be only one)
                for (parcelSnapshot in dataSnapshot.children) {
                    // Get the key of the parcel
                    val parcelKey = parcelSnapshot.key

                    // Get the parcel data
                    val parcelData = parcelSnapshot.value as Map<*, *>

                    // Modify the parcel data to update the status
                    val modifiedParcelData = parcelData.toMutableMap()
                    modifiedParcelData["status"] = "Collected"

                    // Store the modified parcel data in the "collectedParcels" node
                    dbRef.child("users").child(userId).child("collectedParcels").push().setValue(modifiedParcelData)

                    // Remove the parcel from the "arrivedParcels" node
                    parcelKey?.let {
                        arrivedParcelsRef.child(it).removeValue()
                    }

                    // Remove the item from the list
                    val index = trackingInfoList.indexOfFirst { it.trackingNumber == trackingNumber }
                    if (index != -1) {
                        trackingInfoList.removeAt(index)
                        adapter.notifyItemRemoved(index)
                    }

                    // Inform the user that the parcel has been collected
                    Toast.makeText(this@ConfirmParcelActivity, "Parcel collected", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e(
                    "CollectParcel",
                    "Error collecting parcel: ${databaseError.message}"
                )
            }
        })
    }

}