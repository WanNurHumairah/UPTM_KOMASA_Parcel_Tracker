package com.example.komasaparceltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class HistoryAdapter : ListAdapter<CollectedParcel, HistoryAdapter.HistoryViewHolder>(DiffCallback) {

    companion object DiffCallback : DiffUtil.ItemCallback<CollectedParcel>() {
        override fun areItemsTheSame(oldItem: CollectedParcel, newItem: CollectedParcel): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(oldItem: CollectedParcel, newItem: CollectedParcel): Boolean {
            return oldItem.trackingNumber == newItem.trackingNumber
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_parcel, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val parcel = getItem(position)
        holder.bind(parcel)
    }

    inner class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackingNumberTextView: TextView = itemView.findViewById(R.id.trackingNumberTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)

        fun bind(collectedParcel: CollectedParcel) {
            trackingNumberTextView.text = collectedParcel.trackingNumber
            statusTextView.text = collectedParcel.status
        }
    }
}
