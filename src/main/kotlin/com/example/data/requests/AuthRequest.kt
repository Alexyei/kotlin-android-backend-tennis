package com.example.data.requests

import kotlinx.serialization.Serializable

//данные передаваемые пользователем для входа
@Serializable
data class AuthRequestLogin(
    val username: String,
    val password: String
)

//данные передаваемые пользователем для регистрации
@Serializable
data class AuthRequestSignup(
    val username: String,
    val password: String,
    val repeat:String
)