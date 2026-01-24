package com.aiadvent.day1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.aiadvent.day1.viewModel.ChatViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: ChatViewModel
    private lateinit var adapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Передаем application
        viewModel = ViewModelProvider(this,
            ViewModelProvider.AndroidViewModelFactory(application)
        ).get(ChatViewModel::class.java)

        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        val progressBar = findViewById<ProgressBar>(R.id.progressBar)
        val editTextMessage = findViewById<EditText>(R.id.editTextMessage)
        val buttonSend = findViewById<Button>(R.id.buttonSend)

        val buttonNewDialog: ImageButton = findViewById(R.id.buttonNewDialog)
        buttonNewDialog.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Новый диалог")
                .setMessage("Очистить историю и начать заново?")
                .setPositiveButton("Да") { _, _ ->
                    viewModel.startNewDialog()
                }
                .setNegativeButton("Нет", null)
                .show()
        }

        val buttonSettings: ImageButton = findViewById(R.id.buttonSettings)
        buttonSettings.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            intent.putExtra("currentPrompt", viewModel.systemPrompt)
            intent.putExtra("currentTemperature", viewModel.temperature)
            startActivityForResult(intent, 1) // 1 — код запроса
        }

        adapter = MessageAdapter()
        recyclerView.layoutManager = LinearLayoutManager(this) // ОБЯЗАТЕЛЬНО!
        recyclerView.adapter = adapter

        // В observe messages
        viewModel.messages.observe(this, Observer { messages ->
            adapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    recyclerView.smoothScrollToPosition(messages.size - 1)
                }
            }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            val newPrompt = data.getStringExtra("newPrompt") ?: return
            val newTemperature = data.getDoubleExtra("newTemperature", AppConstants.DEFAULT_TEMPERATURE)
            viewModel.updateSystemPrompt(newPrompt)
            viewModel.updateTemperature(newTemperature)
        }
    }
}
