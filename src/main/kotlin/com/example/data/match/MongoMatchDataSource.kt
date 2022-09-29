package com.example.data.match

import com.example.data.requests.MatchRequestInsertOrUpdate
import com.example.data.responses.MatchResponse
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoDatabase
import kotlinx.datetime.toKotlinInstant
import org.bson.types.ObjectId
import org.litote.kmongo.*
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

class MongoMatchDataSource(db: MongoDatabase):MatchDataSource {
    private val matches = db.getCollection<Match>()
    override suspend fun getAll(): List<MatchResponse> {
        val result = matches.find().toList().map {
                el->MatchResponse(
            el.id.toString(),
            el.created.toInstant().toKotlinInstant().toString(),
            el.setCount,
            el.endType,
            el.whoServiceFirst,
            el.firstPlayerName,
            el.secondPlayerName,
            el.penalties,
            el.sets,
            el.points,
        ) }

        return result
    }

//    override suspend fun getUserByUsername(username: String): User? {
//        return users.findOne(User::username eq username)
//    }

    override suspend fun insertMatch(match: Match): Boolean {
        return matches.insertOne(match).wasAcknowledged()
    }

    override suspend fun insertOrUpdateMatch(match:Match): Boolean {
        val matchExist = matches.findOneById(match.id) != null
        return if(matchExist){
           matches.updateOneById(match.id,match).wasAcknowledged()
        }else{
            matches.insertOne(match).wasAcknowledged()
        }
    }

    override suspend fun deleteMatch(matchId: String): Boolean {
        val matchExist = matches.findOneById(matchId) != null
        return if(matchExist){
            matches.deleteOneById(matchId).wasAcknowledged()
        }else{
            return false;
        }
    }
//    override suspend fun updateMatch(id:String,match:Match):Boolean{
//        return users.updateOneById("",match).wasAcknowledged()
//    }
}