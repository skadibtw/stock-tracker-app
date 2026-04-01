package com.example.stocktracker.mobileapi.presentation.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.callid.CallId
import java.util.UUID

fun Application.configureCallId() {
    install(CallId) {
        header("X-Request-Id")
        generate {
            UUID.randomUUID().toString()
        }
        verify { callId ->
            callId.isNotBlank()
        }
        replyToHeader("X-Request-Id")
    }
}
