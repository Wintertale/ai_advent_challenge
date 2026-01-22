package com.aiadvent.day1.model

data class YandexGptResponse(
    val result: Result
)

data class Result(
    val alternatives: List<Alternative>,
    val usage: Usage
)

data class Alternative(
    val message: Message,
    val statusCode: Int
)

data class Usage(
    val totalTokens: String
)