package repository

import data.Session
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class SessionRepository {
    private val file = File("sessions.json")
    private val json = Json { prettyPrint = true }
    private val lock = Any()

    fun getAll(): List<Session> = synchronized(lock) {
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            if (text.isBlank()) emptyList() else json.decodeFromString<List<Session>>(text)
        } catch (e: Exception) { emptyList() }
    }

    fun saveAll(sessions: List<Session>) = synchronized(lock) {
        file.writeText(json.encodeToString(sessions))
    }
}