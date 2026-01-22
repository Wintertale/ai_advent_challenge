package com.aiadvent.day1.model

data class YandexGptRequest(
    val modelUri: String,
    val completionOptions: CompletionOptions,
    val messages: List<Message>
)

data class CompletionOptions(
    val temperature: Double,
    val maxTokens: String
)

data class Message(
    val role: String,
    val text: String,
    val responseTimeMs: Long = 0,
    val tokensDetails: TokenDetails? = null,
)

data class TokenDetails(
    val estimatedInput: Int = 0,      // оценка для запроса пользователя
    val estimatedOutput: Int = 0,   // оценка для ответа модели
    val actual: Int = 0               // реальное число из API
)
