package com.example

import com.example.data.match.Match
import com.example.data.match.MatchDataSource
import com.example.data.requests.AuthRequestSignup
import com.example.data.requests.MatchRequestInsertOrUpdate
import com.example.data.user.User
import com.example.data.user.UserDataSource
import com.example.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toKotlinInstant
import org.bson.types.ObjectId
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat

fun Route.insertOrUpdateMatch(
    userDataSource: UserDataSource,
    matchDataSource: MatchDataSource
) {
    post("insert-or-update") {
        val request = kotlin.runCatching { call.receiveNullable<MatchRequestInsertOrUpdate>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest,"ABCD")
            return@post
        }

        call.application.environment.log.info("End type: ${request.endType}")
        println("Count type: ${request.setCount}")
        println("First player: ${request.firstPlayerName}")
        println("Second player: ${request.secondPlayerName}")
        println("Who service First: ${request.whoServiceFirst}")
        println("points: ${request.points.first}")
        println("sets: ${request.sets.first.last()}")
        println("penalties: ${request.penalties.count()}")
        println("penalties: ${request.created}")






        var id = ObjectId()
        var userId: ObjectId

        try{
            id = ObjectId(request.id)
        }catch (_: IllegalArgumentException){ }


        try{
            userId = ObjectId("6324ad24f2653008b220285d")
            if (userDataSource.getUserById(userId)==null){
                call.respond(HttpStatusCode.Conflict,"ABCD")
                return@post
            }

        }catch (_: IllegalArgumentException){
            call.respond(HttpStatusCode.Conflict,"ABCD")
            return@post
        }

        val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
        val date = format.parse(request.created)
        val match = Match(
            id = id,
            userId = userId,
            endType = request.endType,
            setCount = request.setCount,
            whoServiceFirst = request.whoServiceFirst,
            firstPlayerName = request.firstPlayerName,
            secondPlayerName = request.secondPlayerName,
            points = request.points,
            sets = request.sets,
            penalties = request.penalties,
            created = date
        )

//        val areFieldsBlank = request.username.isBlank() || request.password.isBlank() || request.repeat.isBlank()
//        val isPwTooShortOrLong = request.password.length < 4 || request.password.length > 7
//        val isLoginTooShortOrLong = request.password.length < 3 || request.password.length > 10
//        val isPwAndRepeatEquals = request.password != request.repeat
//
//        if(areFieldsBlank || isPwTooShortOrLong || isLoginTooShortOrLong || isPwAndRepeatEquals) {
//            call.respond(HttpStatusCode.Conflict,"ABCD")
//            return@post
//        }
//
//        if (userDataSource.getUserByUsername(request.username)!==null){
//            call.respond(HttpStatusCode.Conflict,"Такой login уже занят")
//            return@post
//        }
//
//        val saltedHash = hashingService.generateSaltedHash(request.password)
//        val user = User(
//            username = request.username,
//            password = saltedHash.hash,
//            salt = saltedHash.salt
//        )
        val wasAcknowledged = matchDataSource.insertOrUpdateMatch(match)
        if(!wasAcknowledged)  {
            call.respond(HttpStatusCode.Conflict, "ABCD")
            return@post
        }

        call.respond(HttpStatusCode.OK, true)
    }

    get("all-matches") {
        call.respond(HttpStatusCode.OK, matchDataSource.getAll())
    }
}