package repository

import data.Message
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class MessageRepository {
    private val file = File("messages.json")
    private val json = Json { prettyPrint = true }
    private val lock = Any()

    fun getAll(): List<Message> = synchronized(lock) {
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            if (text.isBlank()) emptyList() else json.decodeFromString<List<Message>>(text)
        } catch (e: Exception) { emptyList() }
    }

    fun saveAll(messages: List<Message>) = synchronized(lock) {
        file.writeText(json.encodeToString(messages))
    }

    // Генерация нового ID
    fun generateId(): Int {
        return (getAll().maxOfOrNull { it.id } ?: 0) + 1
    }
}