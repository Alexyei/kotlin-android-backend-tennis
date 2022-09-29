package com.example.data.match

import kotlinx.serialization.Serializable

@Serializable
data class Penalty(
    val cause: String,
    val penalty: String,
    val toFirstPlayer:Boolean
)
