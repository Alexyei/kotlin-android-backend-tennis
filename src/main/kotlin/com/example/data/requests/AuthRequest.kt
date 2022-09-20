package com.example.data.requests

import kotlinx.serialization.Serializable

@Serializable
data class AuthRequestLogin(
    val username: String,
    val password: String
)

@Serializable
data class AuthRequestSignup(
    val username: String,
    val password: String,
    val repeat:String
)