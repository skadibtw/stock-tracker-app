package com.example.stocktracker.loadtester

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.measureTimeMillis

fun main() = runBlocking {
    val client = HttpClient(CIO) {
        engine {
            maxConnectionsCount = 1000
            endpoint {
                maxConnectionsPerRoute = 500
            }
        }
    }

    val totalRequests = 10000
    val concurrency = 100
    val successCount = AtomicInteger(0)
    val errorCount = AtomicInteger(0)
    val targetUrl = "https://song-analysis.app/market/quotes/AAPL"

    println("Starting load test: $totalRequests requests with concurrency $concurrency to $targetUrl")

    val timeMillis = measureTimeMillis {
        val jobs = List(concurrency) { workerId ->
            launch(Dispatchers.IO) {
                val requestsPerWorker = totalRequests / concurrency
                for (i in 0 until requestsPerWorker) {
                    try {
                        val response = client.get(targetUrl)
                        if (response.status.value in 200..299 || response.status.value == 404 || response.status.value == 503) {
                            successCount.incrementAndGet()
                        } else {
                            errorCount.incrementAndGet()
                        }
                    } catch (e: Exception) {
                        errorCount.incrementAndGet()
                    }
                }
            }
        }
        jobs.forEach { it.join() }
    }

    client.close()

    println("\n--- Load Test Results ---")
    println("Total Requests: $totalRequests")
    println("Successful Requests (including handled app errors): ${successCount.get()}")
    println("Failed Requests (connection/unhandled errors): ${errorCount.get()}")
    println("Time Taken: ${timeMillis}ms")
    println("Requests Per Second: ${"%.2f".format(totalRequests / (timeMillis / 1000.0))}")
}