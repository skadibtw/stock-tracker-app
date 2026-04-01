package com.example.stocktracker.mobileapi.presentation.http

import com.example.stocktracker.mobileapi.infrastructure.http.UpstreamProxy
import io.ktor.server.application.call
import io.ktor.server.request.path
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.proxyRoutes(upstreamProxy: UpstreamProxy) {
    route("/auth") {
        post("/register") {
            upstreamProxy.forward(call, "/auth/register")
        }
        post("/login") {
            upstreamProxy.forward(call, "/auth/login")
        }
    }

    route("/market") {
        get("/quotes/{symbol}") {
            upstreamProxy.forward(call, call.request.path())
        }
    }

    route("/portfolio") {
        get("/stocks/{symbol}") {
            upstreamProxy.forward(call, call.request.path())
        }
        post("/stocks/buy") {
            upstreamProxy.forward(call, "/portfolio/stocks/buy")
        }
        post("/stocks/sell") {
            upstreamProxy.forward(call, "/portfolio/stocks/sell")
        }
        get("/statistics") {
            upstreamProxy.forward(call, "/portfolio/statistics")
        }
    }
}
