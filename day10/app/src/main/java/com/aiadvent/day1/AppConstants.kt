package com.aiadvent.day1

object AppConstants {
    /**
     * Дефолтное значение температуры для ИИ-модели
     * Используется в настройках, API‑запросах и т. д.
     */
    const val DEFAULT_TEMPERATURE = 0.5

    /**
     * Количество сообщений, после которых будет произведена суммаризация предыдущего диалога
     * для уменьшения количества токенов
     */
    const val SUMMARIZATION_TRIGGER_COUNT = 5

    /**
     * Изначальный промпт для ИИ-модели
     */
    const val INITIAL_SYSTEM_PROMPT = "Ты - умный ассистент"
}
