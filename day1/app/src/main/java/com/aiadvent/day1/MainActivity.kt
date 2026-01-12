package com.aiadvent.day1

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiadvent.day1.viewModel.ChatViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ChatViewModel()

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = findViewById<Button>(R.id.buttonSend)

        adapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        viewModel.messages.observe(this, Observer { messages ->
            adapter.submitList(messages)
        })

        viewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) android.view.View.VISIBLE else android.view.View.GONE
        })

        buttonSend.setOnClickListener {
            val text = editTextMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                viewModel.sendMessage(text)
                editTextMessage.text.clear()
            }
        }
    }
}
