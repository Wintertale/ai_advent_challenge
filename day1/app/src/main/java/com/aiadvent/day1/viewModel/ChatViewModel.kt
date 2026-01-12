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

    private val service: YandexGptService = RetrofitClient.instance.create(YandexGptService::class.java)

    init {
        _messages.value = mutableListOf()  // Инициализация
        _isLoading.value = false
    }

    fun sendMessage(userInput: String) {
        _isLoading.value = true
        val currentMessages = _messages.value ?: mutableListOf()
        currentMessages.add("Вы: $userInput")
        _messages.value = currentMessages

        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    service.sendMessage("Api-Key ${BuildConfig.API_KEY}", buildRequest(userInput)).execute()
                }
                if (response.isSuccessful && response.body() != null) {
                    val answer = response.body()!!.result.alternatives[0].message.text
                    val updatedMessages = _messages.value ?: mutableListOf()
                    updatedMessages.add("ИИ: $answer")
                    _messages.value = updatedMessages
                } else {
                    val updatedMessages = _messages.value ?: mutableListOf()
                    updatedMessages.add("ИИ: Ошибка ответа")
                    _messages.value = updatedMessages
                }
            } catch (e: Exception) {
                val updatedMessages = _messages.value ?: mutableListOf()
                updatedMessages.add("ИИ: Ошибка сети")
                _messages.value = updatedMessages
            }
            _isLoading.value = false
        }
    }

    private fun buildRequest(userInput: String): YandexGptRequest {
        return YandexGptRequest(
            modelUri = "gpt://${BuildConfig.FOLDER_ID}/yandexgpt/latest",
            completionOptions = CompletionOptions(temperature = 0.7, maxTokens = "2000"),
            messages = listOf(
                Message(role = "user", text = userInput)
            )
        )
    }
}
