package com.aiadvent.day1.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import androidx.lifecycle.viewModelScope
import com.aiadvent.day1.AppConstants
import com.aiadvent.day1.AppConstants.INITIAL_SYSTEM_PROMPT
import com.aiadvent.day1.AppConstants.SUMMARIZATION_TRIGGER_COUNT
import com.aiadvent.day1.BuildConfig
import com.aiadvent.day1.model.ChatSession
import com.aiadvent.day1.model.CompletionOptions
import com.aiadvent.day1.model.Message
import com.aiadvent.day1.model.TokenDetails
import com.aiadvent.day1.model.YandexGptRequest
import com.aiadvent.day1.model.YandexGptResponse
import com.aiadvent.day1.retrofit.RetrofitClient
import com.aiadvent.day1.service.YandexGptService
import com.aiadvent.day1.storage.ChatStorage
import kotlinx.coroutines.*
import retrofit2.Response

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val storage = ChatStorage(application)
    private val _messages = MutableLiveData<List<Message>?>()  // Теперь может быть null
    val messages: LiveData<List<Message>>
        get() = _messages.map { list ->
            list?.filter { it.role != "system" } ?: emptyList()   // Исключаем system-сообщения
        } // Если null → возвращаем emptyList()


    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    // Храним полную историю диалога как список Message
    private val conversationHistory = mutableListOf<Message>()

    var systemPrompt: String = INITIAL_SYSTEM_PROMPT

    var temperature = AppConstants.DEFAULT_TEMPERATURE // Значение по умолчанию

    private var messageCounter = 0  // счётчик сообщений пользователя

    private val service: YandexGptService = RetrofitClient.instance.create(YandexGptService::class.java)

    init {
        _messages.value = emptyList()
        _isLoading.value = false

        // Загружаем предыдущий сеанс
        val loadedSession = storage.loadSession()
        systemPrompt = loadedSession.systemPrompt
        conversationHistory.addAll(loadedSession.messages)

        // Если есть история — обновляем UI
        if (conversationHistory.isNotEmpty()) {
            _messages.value = conversationHistory
        } else {
            resetConversationWithPrompt()
        }
    }

    // Метод для сброса истории с новым промптом
    fun updateSystemPrompt(newPrompt: String) {
        systemPrompt = newPrompt
        resetConversationWithPrompt()
        saveCurrentSession() // Сохраняем после изменения промпта
    }

    // Добавляем системный промпт в историю
    private fun resetConversationWithPrompt() {
        conversationHistory.clear()
        conversationHistory.add(Message(role = "system", text = systemPrompt))
    }

    // Добавляем метод для изменения температуры
    fun updateTemperature(newTemperature: Double) {
        temperature = newTemperature
    }

    fun sendMessage(userInput: String) {
        _isLoading.value = true

        // Добавляем сообщение пользователя в историю
        val userMessage = Message(role = "user", text = userInput)
        conversationHistory.add(userMessage)

        // Обновляем UI _messages ВСЕМ списком (system останется внутри)
        _messages.value = conversationHistory  // ← Все сообщения (включая system)

        viewModelScope.launch {
            val startTime = System.currentTimeMillis() // Замер начала

            try {
                val response = withContext(Dispatchers.IO) {
                    service.sendMessage(
                        "Api-Key ${BuildConfig.API_KEY}",
                        buildRequest() // 4. Передаем всю историю
                    ).execute()
                }

                val endTime = System.currentTimeMillis() // Замер конца
                val responseTimeMs = endTime - startTime // Вычисляем время ответа

                if (response.isSuccessful && response.body() != null) {
                    val answer = response.body()!!.result.alternatives[0].message.text
                        .replace("```", "")

                    // Добавляем ответ ИИ в историю
                    val aiMessage = Message(
                        role = "assistant",
                        text = answer,
                        responseTimeMs = responseTimeMs, calculateTokenDetails(userInput+systemPrompt, answer, response)
                    )
                    conversationHistory.add(aiMessage)

                    // Обновляем UI
                    _messages.value = conversationHistory

                    // Увеличиваем счётчик отправленных пользователем сообщений
                    messageCounter++

                    // Проверяем, нужно ли делать суммаризацию
                    if (messageCounter % SUMMARIZATION_TRIGGER_COUNT == 0) {
                        summarizeConversation()
                    }

                    // Сохраняем текущий сеанс
                    saveCurrentSession()
                } else {
                    handleError("Ошибка ответа")
                }
            } catch (e: Exception) {
                handleError("Ошибка сети")
            }
            _isLoading.value = false
        }
    }

    // Строим запрос с полной историей диалога
    private fun buildRequest(): YandexGptRequest {
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

    private fun saveCurrentSession() {
        val session = ChatSession(
            messages = conversationHistory,
            systemPrompt = systemPrompt
        )
        storage.saveSession(session)
    }

    private fun estimateTokens(text: String, language: String = "ru"): Int {
        val avgCharsPerToken = when (language) {
            "ru" -> 2.7   // Русский
            "en" -> 4.0   // Английский
            else -> 3.5   // Другие языки
        }
        return (text.length / avgCharsPerToken).toInt()
    }

    private fun calculateTokenDetails(userInput: String, answer: String, response: Response<YandexGptResponse>): TokenDetails {
        // 1. Считаем оценки
        val estimatedTokensInput = estimateTokens(userInput, language = "ru")
        val estimatedTokensOutput = estimateTokens(answer, language = "ru")

        // 2. Получаем реальные токены из API
        val actualTokens = response.body()
            ?.result
            ?.usage
            ?.totalTokens
            ?.toIntOrNull()
            ?: 0

        // 3. Создаём объект TokenDetails
        val tokenDetails = TokenDetails(
            estimatedInput = estimatedTokensInput,
            estimatedOutput = estimatedTokensOutput,
            actual = actualTokens
        )

        return tokenDetails
    }

    private suspend fun summarizeConversation() {
        _isLoading.value = true

        try {
            // Формируем промпт для суммаризации
            val summaryPrompt = """
            Сжато перескажи суть предыдущего диалога, сохранив ключевые факты и намерения.
            Ответ должен быть лаконичным (3–5 предложений), без лишних деталей.
            Не добавляй комментариев, только сам пересказ.
        """.trimIndent()

            // Создаём временный запрос только для суммаризации
            val summaryRequest = YandexGptRequest(
                modelUri = "gpt://${BuildConfig.FOLDER_ID}/yandexgpt/latest",
                completionOptions = CompletionOptions(temperature = 0.3, maxTokens = "300"),
                messages = conversationHistory + Message(role = "user", text = summaryPrompt)
            )

            val response = withContext(Dispatchers.IO) {
                service.sendMessage(
                    "Api-Key ${BuildConfig.API_KEY}",
                    summaryRequest
                ).execute()
            }

            if (response.isSuccessful && response.body() != null) {
                val summaryText = response.body()!!.result.alternatives[0].message.text
                    .replace("```", "")

                // Обновляем системный промпт — теперь он содержит суммаризацию
                systemPrompt = "Ты — умный помощник. Предыдущий диалог можно свести к следующему:\n\n$summaryText\n\nПродолжай общение, опираясь на этот контекст."

                // Перезапускаем историю с новым системным сообщением
                resetConversationWithPrompt()

                // Сохраняем обновлённый сеанс (с новым systemPrompt и пустой историей)
                saveCurrentSession()
            } else {
                handleError("Не удалось выполнить суммаризацию")
            }
        } catch (e: Exception) {
            handleError("Ошибка при суммаризации диалога")
        }

        _isLoading.value = false
    }

    fun startNewDialog() {
        // 1. Очищаем историю сообщений
        conversationHistory.clear()

        // 2. Сбрасываем счётчик сообщений
        messageCounter = 0

        // 3. Пересоздаём системный промпт (сохраняем текущий systemPrompt)
        resetConversationWithPrompt()

        // 4. Удаляем файл с историей
        storage.deleteSession()

        // 5. Сохраняем новый сеанс
        saveCurrentSession()

        // 6. Обновляем UI
        // Принудительно очищаем адаптер (на всякий случай)
        _messages.value = null
        // Передаём актуальный список (1 элемент — system message)
        _messages.value = conversationHistory
    }
}
