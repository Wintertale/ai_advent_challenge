package com.aiadvent.day1.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.aiadvent.day1.BuildConfig
import com.aiadvent.day1.model.CompletionOptions
import com.aiadvent.day1.model.Message
import com.aiadvent.day1.model.YandexGptRequest
import com.aiadvent.day1.retrofit.RetrofitClient
import com.aiadvent.day1.service.YandexGptService
import kotlinx.coroutines.*

class ChatViewModel : ViewModel() {
    private val _messages = MutableLiveData<MutableList<String>>()
    val messages: LiveData<MutableList<String>> get() = _messages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // 1. Храним полную историю диалога как список Message
    private val conversationHistory = mutableListOf<Message>()

    private val service: YandexGptService = RetrofitClient.instance.create(YandexGptService::class.java)

    init {
        _messages.value = mutableListOf()
        _isLoading.value = false
        // 2. Добавляем системный промпт в историю при инициализации
        conversationHistory.add(
            Message(role = "system", text = "You are smart assistant.\n" +
            "You should answer the questions that the user will ask." +
            "Before providing the reply, clarify all questions you need to give the correct answer." +
            "Ask clarifying questions one by one." +
            "If you feel you have enough information, then reply"),
        )
    }

    fun sendMessage(userInput: String) {
        _isLoading.value = true

        // 3. Добавляем сообщение пользователя в историю
        val userMessage = Message(role = "user", text = userInput)
        conversationHistory.add(userMessage)

        // Обновляем UI (опционально: можно объединить с обновлением истории)
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add("Вы: $userInput")
        _messages.value = currentMessages

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    service.sendMessage(
                        "Api-Key ${BuildConfig.API_KEY}",
                        buildRequest() // 4. Передаем всю историю!
                    ).execute()
                }
                if (response.isSuccessful && response.body() != null) {
                    val answer = response.body()!!.result.alternatives[0].message.text
                        .replace("```", "")

                    // 5. Добавляем ответ ИИ в историю
                    val aiMessage = Message(role = "assistant", text = answer)
                    conversationHistory.add(aiMessage)

                    // Обновляем UI
                    val updatedMessages = _messages.value ?: mutableListOf()
                    updatedMessages.add("ИИ: $answer")
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
        return YandexGptRequest(
            modelUri = "gpt://${BuildConfig.FOLDER_ID}/yandexgpt/latest",
            completionOptions = CompletionOptions(temperature = 0.7, maxTokens = "2000"),
            messages = conversationHistory // Вся история!
        )
    }

    private fun handleError(errorMessage: String) {
        val updatedMessages = _messages.value ?: mutableListOf()
        updatedMessages.add("ИИ: $errorMessage")
        _messages.value = updatedMessages
    }
}
