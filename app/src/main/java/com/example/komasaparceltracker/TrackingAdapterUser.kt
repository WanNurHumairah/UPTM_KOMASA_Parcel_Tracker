package com.example.komasaparceltracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class TrackingAdapterUser(
    private val trackingList: List<TrackingInfo>,
    private val listener: OnDeleteButtonClickListener,
    private val context: Context
) : RecyclerView.Adapter<TrackingAdapterUser.TrackingViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackingViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_tracking_user, parent, false)
        return TrackingViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackingViewHolder, position: Int) {
        val trackingInfo = trackingList[position]
        holder.bind(trackingInfo)
    }

    override fun getItemCount(): Int {
        return trackingList.size
    }

    inner class TrackingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackingNumberTextView: TextView = itemView.findViewById(R.id.trackingNumberTextView)
        private val statusTextView: TextView = itemView.findViewById(R.id.statusTextView)
        private val timestampTextView: TextView = itemView.findViewById(R.id.timestampTextView)
        private val deleteButton: ImageButton = itemView.findViewById(R.id.deleteButton)

        init {
            deleteButton.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val trackingInfo = trackingList[position]
                    listener.onDeleteButtonClick(trackingInfo.trackingNumber)
                }
            }
        }

        fun bind(trackingInfo: TrackingInfo) {
            trackingNumberTextView.text = "\t${trackingInfo.trackingNumber}"
            val statusText = "Status: ${trackingInfo.status}"
            statusTextView.text = statusText

            val timestampText = if (trackingInfo.status == "Pending" || trackingInfo.status == "Not Arrived") {
                SimpleDateFormat("yyyy-MM-dd , HH:mm:ss").format(Date(trackingInfo.timestamp))
            } else {
                SimpleDateFormat("yyyy-MM-dd , HH:mm:ss").format(Date(trackingInfo.arrivalConfirmationTimestamp))
            }

            timestampTextView.text = if (trackingInfo.status == "Pending" || trackingInfo.status == "Not Arrived") {
                "Entered at: $timestampText"
            } else {
                "Arrival confirmed at: $timestampText"
            }

            statusTextView.setTextColor(getStatusColor(trackingInfo.status))
        }


        private fun getStatusColor(status: String): Int {
            return when (status) {
                "Pending" -> ContextCompat.getColor(context, R.color.grey)
                "Arrived" -> ContextCompat.getColor(context, R.color.green)
                "Not Arrived" -> ContextCompat.getColor(context, R.color.red)
                else -> ContextCompat.getColor(context, R.color.black)
            }
        }
    }

    interface OnDeleteButtonClickListener {
        fun onDeleteButtonClick(trackingNumber: String)
    }
}
