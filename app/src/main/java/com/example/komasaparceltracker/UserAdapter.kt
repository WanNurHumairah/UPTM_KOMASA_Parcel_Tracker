package com.example.komasaparceltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.content.Intent
import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter

class UserAdapter : ListAdapter<User, UserAdapter.UserViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.user_item, parent, false)
        return UserViewHolder(view)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = getItem(position)
        holder.bind(user)
    }

    inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val idNumberTextView: TextView = itemView.findViewById(R.id.idNumberTextView)
        private val phoneTextView: TextView = itemView.findViewById(R.id.phoneTextView)

        fun bind(user: User) {
            // Displaying user details in the format "Name: [name]"
            nameTextView.text = "\tName : ${user.name}"
            idNumberTextView.text = "\tID Number : ${user.idNumber!!.toUpperCase()}"
            phoneTextView.text = "\tPhone No. : ${user.phone}"

            // Set click listener for the phone number TextView
            phoneTextView.setOnClickListener {
                // Call the function to initiate a phone call
                initiatePhoneCall(user.phone!!)
            }
        }

        private fun initiatePhoneCall(phoneNumber: String) {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:$phoneNumber")
            itemView.context.startActivity(intent)
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem.idNumber == newItem.idNumber // Assuming idNumber is unique
            }

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
                return oldItem == newItem
            }
        }
    }

}
