package service

import data.Message
import repository.MessageRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class MessageService(private val messageRepo: MessageRepository) {

    fun createMessage(author: String, text: String?): Message {
        if (text.isNullOrBlank()) {
            throw IllegalArgumentException("Text cannot be empty")
        }

        val newId = messageRepo.generateId()
        val time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))

        val message = Message(id = newId, author = author, text = text, createdAt = time)

        val messages = messageRepo.getAll() + message
        messageRepo.saveAll(messages)

        return message
    }

    fun getAllMessages(): List<Message> {
        return messageRepo.getAll()
    }

    fun deleteMessage(id: Int, username: String) {
        val messages = messageRepo.getAll()
        val message = messages.find { it.id == id } ?: throw NoSuchElementException("Message not found")

        // Проверка прав (пункт 2.5 из задания)
        if (message.author != username) {
            throw SecurityException("Forbidden: You are not the author")
        }

        val updatedMessages = messages.filter { it.id != id }
        messageRepo.saveAll(updatedMessages)
    }
}