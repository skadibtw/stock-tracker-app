package com.example.stocktracker.presentation.http.auth

import com.example.stocktracker.application.auth.LoginUserCommand
import com.example.stocktracker.application.auth.LoginUserUseCase
import com.example.stocktracker.application.auth.RegisterUserCommand
import com.example.stocktracker.application.auth.RegisterUserUseCase
import com.example.stocktracker.presentation.http.dto.auth.LoginRequest
import com.example.stocktracker.presentation.http.dto.auth.RegisterRequest
import com.example.stocktracker.presentation.http.dto.auth.toResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route

private val logger = KotlinLogging.logger {}

fun Route.authRoutes(
    registerUserUseCase: RegisterUserUseCase,
    loginUserUseCase: LoginUserUseCase,
) {
    route("/auth") {
        post("/register") {
            val request = call.receive<RegisterRequest>()
            logger.debug { "[AuthRoutes.register] Processing register request {login=${request.login}}" }
            val result = registerUserUseCase.execute(
                RegisterUserCommand(
                    login = request.login,
                    rawPassword = request.password,
                ),
            )
            call.respond(HttpStatusCode.Created, result.toResponse())
        }

        post("/login") {
            val request = call.receive<LoginRequest>()
            logger.debug { "[AuthRoutes.login] Processing login request {login=${request.login}}" }
            val result = loginUserUseCase.execute(
                LoginUserCommand(
                    login = request.login,
                    rawPassword = request.password,
                ),
            )
            call.respond(HttpStatusCode.OK, result.toResponse())
        }
    }
}
