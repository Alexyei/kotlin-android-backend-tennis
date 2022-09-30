package com.example.data.match

import com.example.data.requests.MatchRequestInsertOrUpdate
import com.example.data.responses.MatchResponse
import org.bson.types.ObjectId

interface MatchDataSource {
    suspend fun getAll(): List<MatchResponse>
    suspend fun getMyMatches(userId: ObjectId): List<MatchResponse>
    suspend fun checkItIsUserMatch(matchId: ObjectId, userId:ObjectId):Boolean
    suspend fun checkMatchExist(matchId: ObjectId):Boolean
    suspend fun insertMatch(match: Match):Boolean
    suspend fun insertOrUpdateMatch(match: Match):Boolean
    suspend fun deleteMatch(matchId: ObjectId):Boolean
}