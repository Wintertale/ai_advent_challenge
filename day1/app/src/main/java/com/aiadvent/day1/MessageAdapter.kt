package com.aiadvent.day1

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter


class MessageAdapter : ListAdapter<String, MessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    class MessageViewHolder(val textView: TextView) : RecyclerView.ViewHolder(textView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view as TextView)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.textView.text = getItem(position)
    }
}

// DiffUtil для эффективного обновления списка
class MessageDiffCallback : DiffUtil.ItemCallback<String>() {
    override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
        return oldItem == newItem
    }
}
