package com.example.komasaparceltracker

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.widget.Button

class ConfirmAdapter(private val context: Context, private val trackingInfoList: List<TrackingInfo>) :
    RecyclerView.Adapter<ConfirmAdapter.ConfirmViewHolder>() {

    private var collectButtonClickListener: OnCollectButtonClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfirmViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_arrived_parcel, parent, false)
        return ConfirmViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfirmViewHolder, position: Int) {
        val trackingInfo = trackingInfoList[position]
        holder.bind(trackingInfo)
    }

    override fun getItemCount(): Int {
        return trackingInfoList.size
    }

    inner class ConfirmViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val trackingNumberTextView: TextView = itemView.findViewById(R.id.trackingNumberTextView)

        fun bind(trackingInfo: TrackingInfo) {
            trackingNumberTextView.text = "\t${trackingInfo.trackingNumber}"

            // Set click listener for the "Collect Parcel" button
            itemView.findViewById<Button>(R.id.collectButton).setOnClickListener {
                collectButtonClickListener?.onCollectButtonClick(trackingInfo.trackingNumber)
            }
        }
    }

    interface OnCollectButtonClickListener {
        fun onCollectButtonClick(trackingNumber: String)
    }

    fun setOnCollectButtonClickListener(listener: OnCollectButtonClickListener) {
        collectButtonClickListener = listener
    }
}