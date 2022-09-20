package com.example.security.token

data class TokenConfig(
    //кто выдал токен
    val issuer: String,
    //тип пользователей (обычные юзеры, админы) для кого предназначен токен
    val audience: String,

    //срок годности
    val expiresIn: Long,

    //секретное слово используемое для генерации токена
    val secret: String

)
