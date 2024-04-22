package com.example.komasaparceltracker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedbackAdapter(private val feedbackList: List<String>) :
    RecyclerView.Adapter<FeedbackAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feedback_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val feedback = feedbackList[position]
        holder.feedbackTextView.text = feedback
    }

    override fun getItemCount(): Int {
        return feedbackList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val feedbackTextView: TextView = itemView.findViewById(R.id.feedbackTextView)
    }
}
