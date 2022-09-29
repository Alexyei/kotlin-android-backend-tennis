package com.example.data.responses

import com.example.data.match.Penalty
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MatchResponse(
    val id:String,
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

//date.toInstant().toKotlinInstant()