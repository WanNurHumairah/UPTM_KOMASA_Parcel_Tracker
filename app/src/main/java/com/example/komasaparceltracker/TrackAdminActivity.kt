package com.example.komasaparceltracker

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import android.content.Intent


class TrackAdminActivity : AppCompatActivity(), TrackingAdapter.OnItemClickListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TrackingAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_track_admin)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        searchView = findViewById(R.id.searchView)

        dbRef = FirebaseDatabase.getInstance().reference.child("users")

        fetchTrackingNumbers()

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            onBackPressed()
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filterByName(newText.orEmpty())
                return true
            }
        })
    }

    private fun fetchTrackingNumbers() {
        dbRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val trackingNumbers = mutableListOf<String>()
                for (userSnapshot in dataSnapshot.children) {
                    val userId = userSnapshot.key
                    val userName = userSnapshot.child("name").getValue(String::class.java)
                    val trackingNumbersSnapshot = userSnapshot.child("trackingNumbers")
                    for (trackingSnapshot in trackingNumbersSnapshot.children) {
                        val trackingNumber = trackingSnapshot.child("trackingNumber").getValue(String::class.java)
                        trackingNumber?.let {
                            val ownerAndTracking = "$userName\n$it"
                            trackingNumbers.add(ownerAndTracking)
                        }
                    }
                }
                if (::adapter.isInitialized) {
                    adapter.updateData(trackingNumbers)
                } else {
                    adapter = TrackingAdapter(trackingNumbers, this@TrackAdminActivity, recyclerView)
                    recyclerView.adapter = adapter
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("FetchTrackingNumbers", "Error fetching tracking numbers: ${databaseError.message}")
            }
        })
    }

    override fun onArrivedButtonClick(trackingNumber: String) {
        showConfirmationDialog(trackingNumber, "Arrived")
    }

    private fun showConfirmationDialog(trackingNumber: String, newStatus: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Confirm Status Update")
            .setMessage("Are you sure this parcel has $newStatus ?")
            .setPositiveButton("Yes") { dialog, which ->
                val position = adapter.getTrackingItemPosition(trackingNumber)
                if (position != RecyclerView.NO_POSITION) {
                    val removedTrackingItem = adapter.removeTrackingItem(position)
                    // After removing, you can pass the data to AdminParcelStatusActivity
                    passDataToAdminParcelStatusActivity(removedTrackingItem)
                }
                updateParcelStatus(trackingNumber, newStatus)
            }
            .setNegativeButton("Cancel") { dialog, which ->
                // User cancelled, do nothing
            }
            .show()
    }

    private fun passDataToAdminParcelStatusActivity(arrivedData: String) {
        // Here, you should pass the arrived data to AdminParcelStatusActivity
        // You can use Intent to pass data
        val intent = Intent(this, AdminParcelStatusActivity::class.java)
        intent.putExtra("arrivedData", arrivedData)
    }

    private fun updateParcelStatus(trackingNumber: String, newStatus: String) {
        val arrivalConfirmationTimestamp = System.currentTimeMillis()

        dbRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (userSnapshot in dataSnapshot.children) {
                    val trackingNumbersSnapshot = userSnapshot.child("trackingNumbers")
                    for (trackingSnapshot in trackingNumbersSnapshot.children) {
                        val currentTrackingNumber = trackingSnapshot.child("trackingNumber").getValue(String::class.java)
                        if (currentTrackingNumber == trackingNumber) {
                            // Update the status of the parcel
                            trackingSnapshot.ref.child("status").setValue(newStatus)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        val position = adapter.getTrackingItemPosition(trackingNumber)
                                        if (position != RecyclerView.NO_POSITION) {
                                            adapter.updateItemStatus(position, newStatus)
                                        }
                                        Toast.makeText(
                                            this@TrackAdminActivity,
                                            "$trackingNumber updated to $newStatus",
                                            Toast.LENGTH_SHORT
                                        ).show()

                                        // Check if the status is "Arrived"
                                        if (newStatus == "Arrived") {
                                            // If yes, update the timestamp to the current time
                                            trackingSnapshot.ref.child("arrivalConfirmationTimestamp").setValue(arrivalConfirmationTimestamp)
                                                .addOnCompleteListener { timestampTask ->
                                                    if (timestampTask.isSuccessful) {
                                                        // Timestamp updated successfully
                                                        // Now, store the arrived parcel in a new node
                                                        val arrivedParcelsRef = userSnapshot.child("arrivedParcels").ref.push()
                                                        val parcelId = arrivedParcelsRef.key
                                                        parcelId?.let {
                                                            val arrivedParcelData = hashMapOf(
                                                                "trackingNumber" to trackingNumber,
                                                                "status" to newStatus,
                                                                "arrivalConfirmationTimestamp" to arrivalConfirmationTimestamp
                                                            )
                                                            arrivedParcelsRef.setValue(arrivedParcelData)

                                                            // Delete the old data from the trackingNumbers node
                                                            trackingSnapshot.ref.removeValue()
                                                        }
                                                    } else {
                                                        Toast.makeText(
                                                            this@TrackAdminActivity,
                                                            "Failed to update timestamp for arrived parcel",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                        }
                                    } else {
                                        Toast.makeText(
                                            this@TrackAdminActivity,
                                            "Failed to update status",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.e("UpdateParcelStatus", "Error updating parcel status: ${databaseError.message}")
            }
        })
    }

    override fun onNotArrivedButtonClick(trackingNumber: String) {
        updateParcelStatus(trackingNumber, "Not Arrived")
    }
}