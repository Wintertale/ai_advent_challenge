package com.aiadvent.day1.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aiadvent.day1.AppConstants
import com.aiadvent.day1.BuildConfig
import com.aiadvent.day1.model.CompletionOptions
import com.aiadvent.day1.model.Message
import com.aiadvent.day1.model.YandexGptRequest
import com.aiadvent.day1.retrofit.RetrofitClient
import com.aiadvent.day1.service.YandexGptService
import kotlinx.coroutines.*

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<List<Message>>()
    val messages: LiveData<List<Message>> get() = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // 1. Храним полную историю диалога как список Message
    private val conversationHistory = mutableListOf<Message>()

    var systemPrompt: String = "You are a smart assistant"

    // Добавьте в класс ChatViewModel:
    var temperature = AppConstants.DEFAULT_TEMPERATURE // Значение по умолчанию

    private val service: YandexGptService = RetrofitClient.instance.create(YandexGptService::class.java)

    init {
        _messages.value = emptyList()
        _isLoading.value = false
        resetConversationWithPrompt()
    }

    // Метод для сброса истории с новым промптом
    fun updateSystemPrompt(newPrompt: String) {
        systemPrompt = newPrompt
        resetConversationWithPrompt()
    }

    // Добавляем системный промпт в историю
    private fun resetConversationWithPrompt() {
        conversationHistory.clear()
        conversationHistory.add(Message(role = "system", text = systemPrompt))
    }


    // Добавьте метод для изменения температуры
    fun updateTemperature(newTemperature: Double) {
        temperature = newTemperature
    }

    fun sendMessage(userInput: String) {
        _isLoading.value = true

        // 3. Добавляем сообщение пользователя в историю
        val userMessage = Message(role = "user", text = userInput)
        conversationHistory.add(userMessage)

        // Обновляем UI (опционально: можно объединить с обновлением истории)
        val currentMessages = (_messages.value ?: emptyList()).toMutableList()
        currentMessages.add(userMessage)
        _messages.value = currentMessages

        viewModelScope.launch {
            val startTime = System.currentTimeMillis() // Замер начала

            try {
                val response = withContext(Dispatchers.IO) {
                    service.sendMessage(
                        "Api-Key ${BuildConfig.API_KEY}",
                        buildRequest() // 4. Передаем всю историю!
                    ).execute()
                }

                val endTime = System.currentTimeMillis() // Замер конца
                val responseTimeMs = endTime - startTime // Вычисляем время ответа

                if (response.isSuccessful && response.body() != null) {
                    val answer = response.body()!!.result.alternatives[0].message.text
                        .replace("```", "")

                    // 5. Добавляем ответ ИИ в историю
                    val aiMessage = Message(role = "assistant", text = answer, responseTimeMs = responseTimeMs)
                    conversationHistory.add(aiMessage)

                    // Обновляем UI
                    val updatedMessages = (_messages.value ?: emptyList()).toMutableList()
                    updatedMessages.add(aiMessage) // добавляем объект Message целиком
                    _messages.value = updatedMessages
                } else {
                    handleError("Ошибка ответа")
                }
            } catch (e: Exception) {
                handleError("Ошибка сети")
            }
            _isLoading.value = false
        }
    }

    // 6. Строим запрос с полной историей диалога
    private fun buildRequest(): YandexGptRequest {
        val onlyOneMessageWithSystemPrompt = mutableListOf<Message>()
        onlyOneMessageWithSystemPrompt.add(Message(role = "system", text = systemPrompt))
        onlyOneMessageWithSystemPrompt.add(Message(role = "user", text = conversationHistory.last().text))

        return YandexGptRequest(
            modelUri = "gpt://${BuildConfig.FOLDER_ID}/yandexgpt/latest",
            completionOptions = CompletionOptions(temperature = temperature, maxTokens = "2000"),
            messages = conversationHistory // Вся история!
        )
    }

    private fun handleError(errorMessage: String) {
        val updatedMessages = (_messages.value ?: emptyList()).toMutableList()
        updatedMessages.add(Message(role = "assistant", text = errorMessage))
        _messages.value = updatedMessages
    }
}
