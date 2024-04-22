package com.example.komasaparceltracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import androidx.appcompat.widget.SearchView



class UsersActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var userAdapter: UserAdapter
    private lateinit var userList: MutableList<User>
    private lateinit var searchView: SearchView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        // Find the SearchView
        searchView = findViewById(R.id.searchView)

        // Collapse the SearchView programmatically
        searchView.isIconified = true

        // Find the back button by its ID
        val backButton = findViewById<ImageButton>(R.id.backButton)

        // Set OnClickListener for the backButton
        backButton.setOnClickListener {
            onBackPressed() // This will mimic the back button press action
        }

        // Initialize RecyclerView and adapter
        recyclerView = findViewById(R.id.recyclerView)
        userList = mutableListOf()
        userAdapter = UserAdapter()

        // Set up RecyclerView with LinearLayoutManager
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        // Retrieve user data from Firebase Realtime Database
        retrieveAllUsersFromDatabase()

        // Set up listener for SearchView query text change
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Perform filtering when user submits the query (optional)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter the list based on the search query
                filter(newText ?: "")
                return true
            }
        })
    }

    private fun retrieveAllUsersFromDatabase() {
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        // Attach a listener to retrieve the data
        usersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Clear the existing user list
                userList.clear()

                // Iterate through all users in the dataSnapshot
                for (userSnapshot in dataSnapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java)
                    val idNumber = userSnapshot.child("idNumber").getValue(String::class.java)
                    val phone = userSnapshot.child("phone").getValue(String::class.java)

                    // Create a User object and add it to the list
                    val user = User(name, idNumber, phone)
                    userList.add(user)
                }

                // Notify the adapter that the data set has changed
                userAdapter.submitList(userList)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Handle errors here
            }
        })
    }

    private fun filter(query: String) {
        val filteredList = mutableListOf<User>()
        for (user in userList) {
            if (user.name?.contains(query, true) == true ||
                user.idNumber?.contains(query, true) == true ||
                user.phone?.contains(query, true) == true
            ) {
                filteredList.add(user)
            }
        }
        userAdapter.submitList(filteredList)
    }
}