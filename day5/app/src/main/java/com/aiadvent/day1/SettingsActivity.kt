package com.aiadvent.day1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
class SettingsActivity : AppCompatActivity() {

    private lateinit var editTextPrompt: EditText
    private lateinit var buttonSave: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextPrompt = findViewById(R.id.editTextPrompt)
        buttonSave = findViewById(R.id.buttonSave)

        // Получаем текущий промпт из Intent
        val currentPrompt = intent.getStringExtra("currentPrompt") ?: ""
        editTextPrompt.setText(currentPrompt)

        buttonSave.setOnClickListener {
            val newPrompt = editTextPrompt.text.toString().trim()
            if (newPrompt.isNotEmpty()) {
                // Передаём новый промпт в ViewModel через Result
                val resultIntent = Intent()
                resultIntent.putExtra("newPrompt", newPrompt)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
