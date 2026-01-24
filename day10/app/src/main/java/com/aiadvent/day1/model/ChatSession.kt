package com.aiadvent.day1.model

import com.aiadvent.day1.AppConstants.INITIAL_SYSTEM_PROMPT
import kotlinx.serialization.Serializable

@Serializable
data class ChatSession(
    val messages: List<Message> = emptyList(),
    val systemPrompt: String = INITIAL_SYSTEM_PROMPT
)
