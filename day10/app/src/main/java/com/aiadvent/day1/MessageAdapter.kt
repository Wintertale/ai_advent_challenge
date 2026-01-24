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
            return oldItem.id == newItem.id
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

    override fun getItemCount(): Int {
        return currentList?.size ?: 0  // Защита от null
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val textView: TextView = itemView.findViewById(R.id.textViewMessage)
        private val timeView: TextView = itemView.findViewById(R.id.textViewTime)
        private val tokensInputView: TextView = itemView.findViewById(R.id.textViewTokensInput)
        private val tokensOutputView: TextView = itemView.findViewById(R.id.textViewTokensOutput)
        private val tokensApiView: TextView = itemView.findViewById(R.id.textViewTokensApi)

        fun bind(message: Message) {
            val roleText = if (message.role == "assistant") "ИИ:" else "Вы:"
            textView.text = "${roleText}: ${message.text}"

            // Отображаем время, только если это ответ ИИ и время > 0
            if (message.role == "assistant" && message.responseTimeMs > 0) {
                timeView.text = "Время ответа: ${message.responseTimeMs} мс"
                timeView.visibility = View.VISIBLE
            } else {
                timeView.visibility = View.GONE
            }

            if (message.role == "assistant") {
                // Отображаем поля, если tokensDetails есть
                message.tokensDetails?.let { tokens ->
                    tokensInputView.text = "Запрос (оценка количества токенов): ${tokens.estimatedInput}"
                    tokensInputView.visibility = View.VISIBLE

                    tokensOutputView.text = "Ответ (оценка количества токенов): ${tokens.estimatedOutput}"
                    tokensOutputView.visibility = View.VISIBLE

                    tokensApiView.text = "Общее количество токенов из API: ${tokens.actual}"
                    tokensApiView.visibility = View.VISIBLE
                } ?: run {
                    // Если tokensDetails == null — скрываем все поля
                    tokensInputView.visibility = View.GONE
                    tokensOutputView.visibility = View.GONE
                    tokensApiView.visibility = View.GONE
                }
            } else {
                tokensInputView.visibility = View.GONE
                tokensOutputView.visibility = View.GONE
                tokensApiView.visibility = View.GONE
            }
        }
    }
}
