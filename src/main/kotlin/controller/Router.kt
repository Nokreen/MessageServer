package controller

import server.HttpRequest
import server.HttpResponse
import service.AuthService
import service.MessageService
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString

@Serializable
data class AuthRequest(val username: String, val password: String)

@Serializable
data class AuthResponse(val token: String)

@Serializable
data class MessageRequest(val text: String)

@Serializable
data class ErrorResponse(val error: String)

class Router(
    private val authService: AuthService,
    private val messageService: MessageService
) {
    private val json = Json { ignoreUnknownKeys = true }

    fun route(request: HttpRequest): HttpResponse {
        return try {
            when {
                // --- Маршруты для пользователей ---
                request.method == "POST" && request.path == "/register" -> handleRegister(request)
                request.method == "POST" && request.path == "/login" -> handleLogin(request)

                // --- Маршруты для сообщений ---
                request.method == "POST" && request.path == "/messages" -> handleCreateMessage(request)
                request.method == "GET" && request.path == "/messages" -> handleGetMessages(request)
                request.method == "DELETE" && request.path.startsWith("/messages/") -> handleDeleteMessage(request)

                // --- 404 для всего остального ---
                else -> HttpResponse(404, "Not Found", json.encodeToString(ErrorResponse("Not found")))
            }
        }
        // --- Обработка ошибок (пункт 2.7) ---
        catch (e: IllegalArgumentException) {
            HttpResponse(400, "Bad Request", json.encodeToString(ErrorResponse(e.message ?: "Bad request")))
        }
        catch (e: IllegalStateException) {
            HttpResponse(400, "Bad Request", json.encodeToString(ErrorResponse(e.message ?: "Error")))
        }
        catch (e: NoSuchElementException) {
            HttpResponse(404, "Not Found", json.encodeToString(ErrorResponse(e.message ?: "Not found")))
        }
        catch (e: SecurityException) {
            // Если это ошибка прав доступа (удаление чужого сообщения) - 403
            if (e.message?.contains("Forbidden") == true) {
                HttpResponse(403, "Forbidden", json.encodeToString(ErrorResponse(e.message ?: "Forbidden")))
            } else {
                // Если это ошибка авторизации (нет токена или он неверный) - 401
                HttpResponse(401, "Unauthorized", json.encodeToString(ErrorResponse(e.message ?: "Unauthorized")))
            }
        }
        catch (e: Exception) {
            HttpResponse(500, "Internal Server Error", json.encodeToString(ErrorResponse("Server error")))
        }
    }

    // --- Логика пользователей ---
    private fun handleRegister(request: HttpRequest): HttpResponse {
        val body = json.decodeFromString<AuthRequest>(request.body)
        authService.register(body.username, body.password)
        return HttpResponse(201, "Created", "{\"message\":\"User registered\"}")
    }

    private fun handleLogin(request: HttpRequest): HttpResponse {
        val body = json.decodeFromString<AuthRequest>(request.body)
        val token = authService.login(body.username, body.password)
        return HttpResponse(200, "OK", json.encodeToString(AuthResponse(token)))
    }

    // --- Логика сообщений ---
    private fun handleCreateMessage(request: HttpRequest): HttpResponse {
        // Проверяем токен (если нет или неверный - AuthService кинет SecurityException)
        val username = authService.authenticate(request.headers["authorization"])

        val body = json.decodeFromString<MessageRequest>(request.body)
        val message = messageService.createMessage(username, body.text)

        return HttpResponse(201, "Created", json.encodeToString(message))
    }

    private fun handleGetMessages(request: HttpRequest): HttpResponse {
        // По заданию GET /messages доступен без авторизации
        val messages = messageService.getAllMessages()
        return HttpResponse(200, "OK", json.encodeToString(messages))
    }

    private fun handleDeleteMessage(request: HttpRequest): HttpResponse {
        // Проверяем токен
        val username = authService.authenticate(request.headers["authorization"])

        // Достаем ID из пути (например, из "/messages/5" получаем "5")
        val idStr = request.path.removePrefix("/messages/").trim()
        val id = idStr.toIntOrNull() ?: throw IllegalArgumentException("Invalid message ID")

        // Удаляем (внутри MessageService проверится, совпадает ли автор)
        messageService.deleteMessage(id, username)

        return HttpResponse(200, "OK", "{\"message\":\"Deleted\"}")
    }
}