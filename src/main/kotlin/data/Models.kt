package data

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val username: String,
    val password: String
)

@Serializable
data class Session(
    val token: String,
    val username: String
)

@Serializable
data class Message(
    val id: Int = 0,
    val author: String,
    val text: String,
    val createdAt: String
)