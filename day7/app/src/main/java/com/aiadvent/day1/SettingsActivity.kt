package com.aiadvent.day1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
class SettingsActivity : AppCompatActivity() {

    private lateinit var editTextPrompt: EditText
    private lateinit var buttonSave: Button
    private lateinit var temperatureValue: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        editTextPrompt = findViewById(R.id.editTextPrompt)
        buttonSave = findViewById(R.id.buttonSave)
        temperatureValue = findViewById(R.id.editTextTemperature)

        // Получаем текущий промпт из Intent
        val currentPrompt = intent.getStringExtra("currentPrompt") ?: ""
        editTextPrompt.setText(currentPrompt)

        // Получаем текущее значение температуры (если передано)
        val currentTemperature = intent.getDoubleExtra("currentTemperature", AppConstants.DEFAULT_TEMPERATURE)
        temperatureValue.setText(currentTemperature.toString())

        buttonSave.setOnClickListener {
            val newPrompt = editTextPrompt.text.toString().trim()
            val newTemperatureText = temperatureValue.text.toString().trim()
            // Пытаемся преобразовать температуру в Double
            val newTemperature = try {
                newTemperatureText.toDouble()
            } catch (e: NumberFormatException) {
                AppConstants.DEFAULT_TEMPERATURE // Значение по умолчанию при ошибке
            }

            // Проверяем, что промпт не пустой
            if (newPrompt.isNotEmpty()) {
                // Передаём новые данные в результате
                val resultIntent = Intent()
                resultIntent.putExtra("newPrompt", newPrompt)
                resultIntent.putExtra("newTemperature", newTemperature)
                setResult(RESULT_OK, resultIntent)
                finish()
            }
        }
    }
}
