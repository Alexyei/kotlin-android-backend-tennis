package com.example

import com.example.data.requests.AuthRequestLogin
import com.example.data.requests.AuthRequestSignup
import com.example.data.responses.AuthResponse
import com.example.data.user.User
import com.example.data.user.UserDataSource
import com.example.plugins.configureSecurity
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


//регистрация
fun Route.signUp(
    hashingService: HashingService,
    userDataSource: UserDataSource
) {
    post("signup") {
//        Проверяем, что переданные пользователем данные имеют тип AuthRequestSignup
        val request = kotlin.runCatching { call.receiveNullable<AuthRequestSignup>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest,"ABCD")
            return@post
        }

//        валидация данных
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

//        данные валидны, создаём пользователя
        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )
        val wasAcknowledged = userDataSource.insertUser(user)
//        произошла ошибка при записи в базу данных
        if(!wasAcknowledged)  {
            call.respond(HttpStatusCode.Conflict, "ABCD")
            return@post
        }

//        пользователь зарегистрирован
        call.respond(HttpStatusCode.OK)
    }
}

//вход
fun Route.signIn(
    userDataSource: UserDataSource,
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {
//        проверяем что пользователь передал данные типа AuthRequestLogin
        val request = kotlin.runCatching { call.receiveNullable<AuthRequestLogin>() }.getOrNull() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

//        валидация переданных пользователем данных
        val isPwTooShortOrLong = request.password.length < 4 || request.password.length > 7
        val isLoginTooShortOrLong = request.password.length < 3 || request.password.length > 10
        if( isPwTooShortOrLong || isLoginTooShortOrLong ) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

//        проверяем существует ли пользователь
        val user = userDataSource.getUserByUsername(request.username)
        if(user == null) {
            call.respond(HttpStatusCode.Conflict, "Неправильный логин или пароль")
            return@post
        }

//        проверяем пароль
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

//        данные валидны, генерируем новый токен
        val token = tokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
            )
        )

//        возвращаем пользователю новый токен
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
            println("SECRET ROUTE");
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}