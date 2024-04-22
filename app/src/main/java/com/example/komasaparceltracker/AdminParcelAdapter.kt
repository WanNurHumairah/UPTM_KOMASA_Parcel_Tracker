package com.example.komasaparceltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AdminParcelAdapter(
    private var collectedParcels: MutableList<String>
) : RecyclerView.Adapter<AdminParcelAdapter.AdminParcelViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminParcelViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_admin, parent, false)
        return AdminParcelViewHolder(view)
    }

    override fun onBindViewHolder(holder: AdminParcelViewHolder, position: Int) {
        val parcelInfo = collectedParcels[position]
        holder.bind(parcelInfo)
    }

    override fun getItemCount(): Int {
        return collectedParcels.size
    }

    fun updateData(newCollectedParcels: List<String>) {
        collectedParcels.clear()
        collectedParcels.addAll(newCollectedParcels)
        notifyDataSetChanged()
    }

    inner class AdminParcelViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        private val trackingNumberTextView: TextView = itemView.findViewById(R.id.trackingNumberTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(parcelInfo: String) {
            val infoParts = parcelInfo.split("\n")

            // Assuming parcelInfo format is "Name\nTracking Number: ID Number\nStatus: Phone Number"
            val name = infoParts[0]
            val trackingNumber = infoParts[1].substringAfter(":").trim()
            val status = infoParts[2].substringAfter(":").trim()

            nameTextView.text = "\tName: $name"
            trackingNumberTextView.text = "\tTracking Number: $trackingNumber"
            statusTextView.text = "\tStatus: $status"
        }
    }
}
