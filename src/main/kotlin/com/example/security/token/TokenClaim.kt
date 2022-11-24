package com.example.security.token

// данные хранящиеся внутри токена: в этом проекте я сохраняю только id пользователя (name: "userId", value: user.id.toString())
data class TokenClaim(
    val name: String,
    val value: String
)
