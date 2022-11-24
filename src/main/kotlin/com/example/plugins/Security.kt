package com.example.plugins

import io.ktor.server.auth.*
import io.ktor.util.*
import io.ktor.server.auth.jwt.*
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.example.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

//в необходимых роутах, используем контекст authentication,
//если мы не переделаи jwt-токен (или передали неверный) это вернёт 401 ошибку клиенту
//а метод роутера даже не будет вызван
fun Application.configureSecurity(config:TokenConfig) {

    println("jwt.realm: ${this@configureSecurity.environment.config.property("jwt.realm").getString()}");
    authentication {
        jwt {
//            val jwtAudience = this@configureSecurity.environment.config.property("jwt.audience").getString()
            val jwtAudience = config.audience
            realm = this@configureSecurity.environment.config.property("jwt.realm").getString()
            verifier(
                JWT
                    .require(Algorithm.HMAC256(config.secret))
                    .withAudience(jwtAudience)
                    .withIssuer(config.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
            }
        }
    }

}
