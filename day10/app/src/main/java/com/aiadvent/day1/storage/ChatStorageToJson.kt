package com.aiadvent.day1.storage

import android.content.Context
import com.aiadvent.day1.model.ChatSession
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class ChatStorage(private val context: Context) {

    private val fileName = "chat_session.json"
    private val file: File
        get() = File(context.filesDir, fileName)

    fun saveSession(session: ChatSession) {
        try {
            val json = Json.encodeToString(session)
            file.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun loadSession(): ChatSession {
        return try {
            if (file.exists()) {
                val json = file.readText()
                Json.decodeFromString(ChatSession.serializer(), json)
            } else {
                ChatSession()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            ChatSession() // возврат дефолта при ошибке
        }
    }

    // Метод для удаления файла
    fun deleteSession() {
        file.delete()
    }
}
