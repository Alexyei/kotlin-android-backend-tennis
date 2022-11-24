package com.example

import com.example.authenticate
import com.example.data.match.Match
import com.example.data.match.MatchDataSource
import com.example.data.requests.AuthRequestSignup
import com.example.data.requests.MatchRequestInsertOrUpdate
import com.example.data.responses.AuthResponse
import com.example.data.responses.MatchIdResponse
import com.example.data.user.User
import com.example.data.user.UserDataSource
import com.example.security.hashing.HashingService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.datetime.toKotlinInstant
import org.bson.types.ObjectId
import java.lang.IllegalArgumentException
import java.text.SimpleDateFormat


suspend fun validateMatchRequestInsertOrUpdate(request: MatchRequestInsertOrUpdate): Boolean {
    if (!(request.setCount == 3 || request.setCount == 5)) return false;
    if (!(request.whoServiceFirst == 0 || request.whoServiceFirst == 1)) return false;
    if (!(request.endType == "tie-break" || request.endType == "super TB" || request.endType == "+2 games")) return false;
    if (request.firstPlayerName.isBlank() || request.firstPlayerName.length < 3 || request.firstPlayerName.length > 30) return false;
    if (request.secondPlayerName.isBlank() || request.secondPlayerName.length < 3 || request.secondPlayerName.length > 30) return false;
    if (request.firstPlayerName == request.secondPlayerName) return false;


    println("1")
    try {
        request.points.first.toInt()
    } catch (e: Exception) {
        println("2")
        if (request.points.first !in arrayListOf<String>("", "AD"))
            return false;
    }

    try {
        request.points.second.toInt()
    } catch (e: Exception) {
        println("3")
        if (request.points.second !in arrayListOf<String>("", "AD"))
            return false;
    }

    if (request.sets.first.count() != request.sets.second.count()) return false;
    println("4")
    if (!request.penalties.all { p ->
            if (p.cause.length > 100) return false;
            if (p.penalty !in arrayListOf<String>(
                    "Предупреждение",
                    "+1 очко",
                    "+1 гейм",
                    "+1 сет",
                    "дисквалификация"
                )
            ) return false;
            return true;
        }) return false

    if (request.penalties.filter { p -> p.penalty == "дисквалификация" }.count() > 1) return false;

    return true;
}

suspend fun convertMatchRequestInsertOrUpdateToMatch(
    request: MatchRequestInsertOrUpdate,
    userId: String,
    userDataSource: UserDataSource,
): Match {
    var id = ObjectId()

    try {
        id = ObjectId(request.id)
    } catch (_: IllegalArgumentException) {
    }


    val userId: ObjectId = ObjectId(userId)
    if (userDataSource.getUserById(userId) == null) {
        throw Exception("Пользователь ${userId} не найден")
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

    return match;
}

fun Route.insertOrUpdateMatch(
    userDataSource: UserDataSource,
    matchDataSource: MatchDataSource
) {
    authenticate {
        post("insert-or-update") {
//            проверяем, что переданные данные соответствуют типу MatchRequestInsertOrUpdate
            val request =
                kotlin.runCatching { call.receiveNullable<MatchRequestInsertOrUpdate>() }.getOrNull() ?: kotlin.run {
                    call.respond(HttpStatusCode.BadRequest, "ABCD")
                    return@post
                }
// получаем userId из переданного пользователем jwt-токена
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)

            if (userId == null) {
                call.respond(HttpStatusCode.BadRequest, "ABCD")
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


            println("match start validate")
            if (!validateMatchRequestInsertOrUpdate(request)) {
                call.respond(HttpStatusCode.BadRequest, "ABCD")
                return@post
            }

            println("match validated")
            val match: Match

//            преобразуем данные переданные пользователем в формат используемый в mongoDB
            try {
                match = convertMatchRequestInsertOrUpdateToMatch(request, userId, userDataSource)

                println("match converted")
                if (matchDataSource.checkMatchExist(match.id)) {
                    if (!matchDataSource.checkItIsUserMatch(match.id, match.userId))
                        throw Exception("Матч не принадлежит пользователю")
                }

            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "ABCD")
                return@post
            }

            val wasAcknowledged = matchDataSource.insertOrUpdateMatch(match)
//            если возникла ошибка при записи в БД
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict, "ABCD")
                return@post
            }

            call.respond(
                HttpStatusCode.OK, MatchIdResponse(
                    id = match.id.toString()
                )
            )
        }
    }
}

fun Route.getAllMatches(matchDataSource: MatchDataSource) {
    get("all-matches") {
        call.respond(HttpStatusCode.OK, matchDataSource.getAll())
    }
}

fun Route.getMyMatches(userDataSource: UserDataSource, matchDataSource: MatchDataSource) {
    authenticate {
        get("my-matches") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)

            val user: ObjectId
            try {
                user = ObjectId(userId)
                if (userDataSource.getUserById(user) == null) {
                    call.respond(HttpStatusCode.Conflict, "ABCD")
                    return@get
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "ABCD")
                return@get
            }

            call.respond(HttpStatusCode.OK, matchDataSource.getMyMatches(user))
        }
    }
}

fun Route.deleteMatch(userDataSource: UserDataSource, matchDataSource: MatchDataSource) {
    authenticate {
        delete("delete-match/{id}") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)

            val user: ObjectId
            try {
                user = ObjectId(userId)
                if (userDataSource.getUserById(user) == null) {
                    call.respond(HttpStatusCode.Conflict, "ABCD")
                    return@delete
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "ABCD")
                return@delete
            }

//            получаем параметр из маршрута
            val matchId = call.parameters["id"]
            val match: ObjectId;
            try {
                match = ObjectId(matchId)
                if (!matchDataSource.checkMatchExist(match)) {
                    call.respond(HttpStatusCode.Conflict, "ABCD")
                    return@delete
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.Conflict, "ABCD")
                return@delete
            }

//            проверяем что матч принадлежит пользователю
            if (!matchDataSource.checkItIsUserMatch(match, user)) {
                call.respond(HttpStatusCode.Conflict, "ABCD")
                return@delete
            }

            call.respond(HttpStatusCode.OK, matchDataSource.deleteMatch(match))
        }
    }
}