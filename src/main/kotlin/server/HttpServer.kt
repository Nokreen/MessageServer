package server

import java.io.File
import java.net.ServerSocket
import java.net.Socket
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HttpServer(private val port: Int) {

    fun start(router: (HttpRequest) -> HttpResponse) {
        val serverSocket = ServerSocket(port)
        println("Сервер запущен на порту $port")

        while (true) {
            val clientSocket = serverSocket.accept()
            Thread {
                handleClient(clientSocket, router)
            }.start()
        }
    }

    private fun handleClient(socket: Socket, router: (HttpRequest) -> HttpResponse) {
        socket.use {
            try {
                val reader = it.getInputStream().bufferedReader()
                val writer = it.getOutputStream().bufferedWriter()

                val requestLines = mutableListOf<String>()
                var contentLength = 0

                while (true) {
                    val line = reader.readLine() ?: break
                    if (line.isBlank()) break
                    requestLines.add(line)

                    if (line.lowercase().startsWith("content-length:")) {
                        contentLength = line.substringAfter(":").trim().toInt()
                    }
                }

                val bodyChars = CharArray(contentLength)
                if (contentLength > 0) {
                    reader.read(bodyChars, 0, contentLength)
                }

                val rawRequest = requestLines.joinToString("\r\n") + "\r\n\r\n" + String(bodyChars)
                val request = parseRequest(rawRequest)

                // --- ЛОГИРОВАНИЕ (Пункт 2.6) ---
                val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
                // Пытаемся достать токен из заголовка, чтобы понять кто жто
                val user = if (request.headers["authorization"] != null) "authorized_user" else "guest"
                val logEntry = "$time | ${request.method} | ${request.path} | $user\n"
                File("server.log").appendText(logEntry)
                // ------------------------------

                val response = try {
                    router(request)
                } catch (e: Exception) {
                    HttpResponse(500, "Internal Server Error", "{\"error\":\"${e.message}\"}")
                }

                writer.write("HTTP/1.1 ${response.statusCode} ${response.statusText}\r\n")
                writer.write("Content-Type: application/json\r\n")
                writer.write("Content-Length: ${response.body.toByteArray().size}\r\n")
                writer.write("Connection: close\r\n")
                writer.write("\r\n")
                writer.write(response.body)
                writer.flush()

            } catch (e: Exception) {
                println("Ошибка соединения: ${e.message}")
            }
        }
    }
}