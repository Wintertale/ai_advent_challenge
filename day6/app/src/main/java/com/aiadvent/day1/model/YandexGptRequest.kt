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
    val text: String
)
