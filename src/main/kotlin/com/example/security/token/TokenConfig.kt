package com.example.security.token

data class TokenConfig(
    //кто выдал токен (в этом проекте http://localhost:8080)
    val issuer: String,
    //тип пользователей (обычные юзеры, админы) для кого предназначен токен (в этом проекте users
    val audience: String,

    //срок годности
    val expiresIn: Long,

    //секретное слово используемое для генерации токена
    val secret: String

)
