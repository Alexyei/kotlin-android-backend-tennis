package com.example

import com.example.data.match.MongoMatchDataSource
import com.example.data.user.MongoUserDataSource
import com.example.data.user.User
import io.ktor.server.application.*
import com.example.plugins.*
import com.example.security.hashing.SHA256HashingService
import com.example.security.token.JwtTokenService
import com.example.security.token.TokenConfig
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.litote.kmongo.KMongo

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {
    val db = KMongo.createClient(
        "mongodb://localhost:27017/?readPreference=primary&appname=MongoDB%20Compass&directConnection=true&ssl=false"
    ).getDatabase("android-tennis")

//    получаем объект для работы с таблицей пользователей
    val userDataSource = MongoUserDataSource(db)
//    получаем объект для работы с таблицей матчей
    val matchDataSource = MongoMatchDataSource(db)

//    получаем объект для работы c jwt токенами
    val tokenService = JwtTokenService()

    println("jwt.issue: ${environment.config.property("jwt.issue").getString()}");
    println("jwt.audience: ${environment.config.property("jwt.audience").getString()}");

//    создаём jwt конфигурацию
//   значения хранятся в resources.application.conf, JWT_SECRET аргумент командной строки
    val tokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issue").getString(),

        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = 365L * 1000L * 60L * 60L * 24L,
        secret = System.getenv("JWT_SECRET")
    )

//    получаем объект для хэширования паролей пользователей и их проверки
    val hashingService = SHA256HashingService()

//    GlobalScope.launch {
//        val user = User(
//            username = "test",
//            password = "test-password",
//            salt = "salt"
//        )
//        userDataSource.insertUser(user)
//    }

//Создаём auth-middleware, затем в необходимых роутерах будет его подключать
    configureSecurity(tokenConfig)

//    логируем входящие запросы пользователей
    configureMonitoring()
//    получаем и возвращаем JSON
    configureSerialization()
//    подключаем роутер
    configureRouting(userDataSource,matchDataSource, hashingService, tokenService, tokenConfig)

}
