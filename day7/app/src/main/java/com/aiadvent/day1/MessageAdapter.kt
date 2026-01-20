package com.aiadvent.day1

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.ListAdapter
import com.aiadvent.day1.model.Message


class MessageAdapter : ListAdapter<Message, MessageAdapter.MessageViewHolder>(
    object : DiffUtil.ItemCallback<Message>() {
        override fun areItemsTheSame(oldItem: Message, newItem: Message): Boolean {
            // Сравниваем по содержимому, а не по ссылке
            return oldItem.role == newItem.role && oldItem.text == newItem.text
        }

        override fun areContentsTheSame(oldItem: Message, newItem: Message): Boolean {
            return oldItem == newItem
        }
    }
) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val textView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val timeView: TextView = itemView.findViewById(R.id.textViewTime)

        fun bind(message: Message) {
            val roleText = if (message.role == "assistant") "ИИ:" else "Вы:"
            textView.text = "${roleText}: ${message.text}"

            // Отображаем время, только если это ответ ИИ и время > 0
            if (message.role == "assistant" && message.responseTimeMs > 0) {
                timeView.text = "${message.responseTimeMs} мс"
                timeView.visibility = View.VISIBLE
            } else {
                timeView.visibility = View.GONE
            }
        }
    }
}
