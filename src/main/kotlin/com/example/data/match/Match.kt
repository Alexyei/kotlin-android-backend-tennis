package com.example.data.match

import kotlinx.serialization.Serializable
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*
import kotlin.collections.ArrayList


data class Match(
    @BsonId var id: ObjectId,
    var userId: ObjectId,
// = ObjectId().toString()
    val created: Date,
    val setCount: Int,
    val endType: String,
    val whoServiceFirst:Int,

//    var saved:Boolean,
    val firstPlayerName:String,
    val secondPlayerName:String,

    var penalties: ArrayList<Penalty>,
    var sets: Pair<ArrayList<Int>,ArrayList<Int>>,
    var points: Pair<String,String>,
)
