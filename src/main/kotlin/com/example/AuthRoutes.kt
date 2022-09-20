package com.example

import com.example.data.requests.AuthRequestLogin
import com.example.data.requests.AuthRequestSignup
import com.example.data.responses.AuthResponse
import com.example.data.user.User
import com.example.data.user.UserDataSource
import com.example.security.hashing.HashingService
import com.example.security.hashing.SaltedHash
import com.example.security.token.TokenClaim
import com.example.security.token.TokenConfig
import com.example.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import org.apache.commons.codec.digest.DigestUtils



fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
        val request = kotlin.runCatching { call.receiveNullable<AuthRequestSignup>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest,"ABCD")
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank() || request.repeat.isBlank()
        val isPwTooShortOrLong = request.password.length < 4 || request.password.length > 7
        val isLoginTooShortOrLong = request.password.length < 3 || request.password.length > 10
        val isPwAndRepeatEquals = request.password != request.repeat

        if(areFieldsBlank || isPwTooShortOrLong || isLoginTooShortOrLong || isPwAndRepeatEquals) {
            call.respond(HttpStatusCode.Conflict,"ABCD")
            return@post
        }

        if (userDataSource.getUserByUsername(request.username)!==null){
            call.respond(HttpStatusCode.Conflict,"Такой login уже занят")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)
        if(!wasAcknowledged)  {
            call.respond(HttpStatusCode.Conflict, "ABCD")
            return@post
        }

        call.respond(HttpStatusCode.OK)
    }
}

fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
        val request = kotlin.runCatching { call.receiveNullable<AuthRequestLogin>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val isPwTooShortOrLong = request.password.length < 4 || request.password.length > 7
        val isLoginTooShortOrLong = request.password.length < 3 || request.password.length > 10
        if( isPwTooShortOrLong || isLoginTooShortOrLong ) {
//            call.application.environment.log.info("Hello from module!")
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)
        if(user == null) {
//            call.application.environment.log.info("Hello from module!")
            call.respond(HttpStatusCode.Conflict, "Неправильный логин или пароль")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )
        if(!isValidPassword) {
            println("Entered hash: ${DigestUtils.sha256Hex("${user.salt}${request.password}")}, Hashed PW: ${user.password}")
            call.respond(HttpStatusCode.Conflict, "Неправильный логин или пароль")
            return@post
        }

        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

        call.respond(
            status = HttpStatusCode.OK,
            message = AuthResponse(
                token = token
            )
        )
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}