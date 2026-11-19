package service

import data.Session
import data.User
import repository.SessionRepository
import repository.UserRepository
import java.util.UUID

class AuthService(
    private val userRepo: UserRepository,
    private val sessionRepo: SessionRepository
) {
    // Регистрация
    fun register(username: String?, password: String?) {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            throw IllegalArgumentException("Username and password required")
        }
        if (userRepo.findByUsername(username) != null) {
            throw IllegalStateException("User already exists")
        }
        val users = userRepo.getAll() + User(username, password)
        userRepo.saveAll(users)
    }

    // Логин
    fun login(username: String?, password: String?): String {
        if (username.isNullOrBlank() || password.isNullOrBlank()) {
            throw IllegalArgumentException("Username and password required")
        }
        val user = userRepo.findByUsername(username) ?: throw SecurityException("Invalid credentials")
        if (user.password != password) {
            throw SecurityException("Invalid credentials")
        }

        // Генерируем токен
        val token = UUID.randomUUID().toString()
        val sessions = sessionRepo.getAll() + Session(token, username)
        sessionRepo.saveAll(sessions)
        return token
    }

    // Проверка токена (возвращает username, если токен валиден)
    fun authenticate(tokenHeader: String?): String {
        if (tokenHeader == null || !tokenHeader.startsWith("Bearer ")) {
            throw SecurityException("Unauthorized")
        }
        val token = tokenHeader.removePrefix("Bearer ").trim()
        val session = sessionRepo.getAll().find { it.token == token }
        return session?.username ?: throw SecurityException("Unauthorized")
    }
}