package com.example.komasaparceltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TrackingAdapter(
    private var trackingItems: MutableList<String>,
    private val listener: OnItemClickListener,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<TrackingAdapter.TrackingViewHolder>() {

    private var filteredList: MutableList<String> = trackingItems.toMutableList()
    private val enabledStatusMap = mutableMapOf<Int, Boolean>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_number, parent, false)
        return TrackingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackingViewHolder, position: Int) {
        val trackingItem = filteredList[position]
        holder.bind(trackingItem)
        holder.itemView.findViewById<Button>(R.id.arrivedButton).setOnClickListener {
            listener.onArrivedButtonClick(trackingItem.substringAfter("\n")) // Pass only the tracking number
        }
        holder.itemView.findViewById<Button>(R.id.notArrivedButton).setOnClickListener {
            listener.onNotArrivedButtonClick(trackingItem.substringAfter("\n")) // Pass only the tracking number
        }

    }

    override fun getItemCount(): Int {
        return filteredList.size
    }

    inner class TrackingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackingInfoTextView: TextView = itemView.findViewById(R.id.trackingInfoTextView)
        private val arrivedButton: Button = itemView.findViewById(R.id.arrivedButton)
        private val notArrivedButton: Button = itemView.findViewById(R.id.notArrivedButton)

        fun bind(trackingItem: String) {
            trackingInfoTextView.text = trackingItem
        }

    }

    interface OnItemClickListener {
        fun onArrivedButtonClick(trackingNumber: String)
        fun onNotArrivedButtonClick(trackingNumber: String)
    }

    fun filterByName(name: String) {
        filteredList = if (name.isEmpty()) {
            trackingItems.toMutableList() // Convert to MutableList
        } else {
            trackingItems.filter { trackingItem ->
                val userName = trackingItem.substringBefore("\n")
                userName.contains(name, ignoreCase = true)
            }.toMutableList() // Convert to MutableList
        }
        notifyDataSetChanged()
    }

    fun updateData(newTrackingItems: List<String>) {
        trackingItems.clear()
        trackingItems.addAll(newTrackingItems)
        filteredList.clear()
        filteredList.addAll(newTrackingItems)
        notifyDataSetChanged()
    }

    fun updateItemStatus(position: Int, newStatus: String) {
        if (position != RecyclerView.NO_POSITION) {
            val trackingItem = trackingItems[position]
            trackingItems[position] = trackingItem.replaceAfter("\n", newStatus)
            notifyItemChanged(position)
        }
    }

    fun getTrackingItemPosition(trackingNumber: String): Int {
        return trackingItems.indexOfFirst { it.substringAfter("\n") == trackingNumber }
    }

    fun removeTrackingItem(position: Int): String {
        val removedTrackingItem = filteredList.removeAt(position)
        val originalPosition = trackingItems.indexOf(removedTrackingItem)
        if (originalPosition != -1) {
            trackingItems.removeAt(originalPosition)
        }
        notifyItemRemoved(position)
        return removedTrackingItem
    }

}