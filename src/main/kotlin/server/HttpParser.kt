package server

fun parseRequest(rawText: String): HttpRequest {
    val lines = rawText.lines()
    if (lines.isEmpty()) return HttpRequest("", "", emptyMap(), "")

    val requestLine = lines[0].split(" ")
    val method = requestLine.getOrElse(0) { "" }
    val path = requestLine.getOrElse(1) { "" }

    val headers = mutableMapOf<String, String>()
    var bodyStartIndex = 0

    for (i in 1 until lines.size) {
        if (lines[i].isBlank()) {
            bodyStartIndex = i + 1
            break
        }
        val parts = lines[i].split(": ", limit = 2)
        if (parts.size == 2) {
            headers[parts[0].lowercase()] = parts[1]
        }
    }

    val body = if (bodyStartIndex > 0 && bodyStartIndex < lines.size) {
        lines.drop(bodyStartIndex).joinToString("\n")
    } else ""

    return HttpRequest(method, path, headers, body)
}