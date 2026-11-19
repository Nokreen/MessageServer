import controller.Router
import repository.MessageRepository
import repository.SessionRepository
import repository.UserRepository
import server.HttpServer
import service.AuthService
import service.MessageService

fun main() {
    // Репозитории
    val userRepo = UserRepository()
    val sessionRepo = SessionRepository()
    val messageRepo = MessageRepository()

    // Сервисы
    val authService = AuthService(userRepo, sessionRepo)
    val messageService = MessageService(messageRepo)

    // Роутер
    val router = Router(authService, messageService)

    // Сервер
    val server = HttpServer(8080)
    server.start { request -> router.route(request) }
}