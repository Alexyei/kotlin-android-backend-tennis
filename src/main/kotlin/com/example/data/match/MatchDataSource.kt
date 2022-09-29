package com.example.data.match

import com.example.data.requests.MatchRequestInsertOrUpdate
import com.example.data.responses.MatchResponse

interface MatchDataSource {
    suspend fun getAll(): List<MatchResponse>
    suspend fun insertMatch(match: Match):Boolean
    suspend fun insertOrUpdateMatch(match: Match):Boolean
    suspend fun deleteMatch(matchId: String):Boolean
}