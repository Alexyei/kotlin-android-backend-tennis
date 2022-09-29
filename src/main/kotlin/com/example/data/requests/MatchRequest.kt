package com.example.data.requests

import com.example.data.match.Penalty
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable

import kotlin.collections.ArrayList



@Serializable
data class MatchRequestInsertOrUpdate(
    var id: String,
    val created: String,
    val setCount: Int,
    val endType: String,
    val whoServiceFirst:Int,

    val firstPlayerName:String,
    val secondPlayerName:String,
    var penalties: ArrayList<Penalty>,
    var sets: Pair<ArrayList<Int>,ArrayList<Int>>,
    var points: Pair<String,String>,
)