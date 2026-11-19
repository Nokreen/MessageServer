package repository

import data.User
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import java.io.File

class UserRepository {
    private val file = File("users.json")
    private val json = Json { prettyPrint = true }

    // Блокировка, чтобы два потока не записали файл одновременно
    private val lock = Any()

    // Получить всех пользователей
    fun getAll(): List<User> = synchronized(lock) {
        if (!file.exists()) return emptyList()
        return try {
            val text = file.readText()
            if (text.isBlank()) emptyList() else json.decodeFromString<List<User>>(text)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Сохранить всех пользователей
    fun saveAll(users: List<User>) = synchronized(lock) {
        file.writeText(json.encodeToString(users))
    }

    // Найти одного пользователя по имени
    fun findByUsername(username: String): User? {
        return getAll().find { it.username == username }
    }
}