package com.example.plugins

import com.example.*
import com.example.data.match.MatchDataSource
import com.example.data.user.UserDataSource
import com.example.security.hashing.HashingService
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(
    userDataSource: UserDataSource,
    matchDataSource: MatchDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    routing {
        signIn(userDataSource, hashingService, tokenService, tokenConfig)
        signUp(hashingService, userDataSource)
        authenticate()
        getSecretInfo()
        insertOrUpdateMatch(userDataSource,matchDataSource)
        getAllMatches(matchDataSource)
        getMyMatches(userDataSource, matchDataSource)
        deleteMatch(userDataSource, matchDataSource)
    }
}
