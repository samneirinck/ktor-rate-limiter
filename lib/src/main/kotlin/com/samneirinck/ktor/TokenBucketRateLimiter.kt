/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package com.samneirinck.ktor

import io.ktor.http.HttpStatusCode.Companion.TooManyRequests
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.response.*
import kotlinx.coroutines.*
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.coroutineContext
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

val TokenBucketRateLimiter = createApplicationPlugin(
    name = "RateLimiter",
    createConfiguration = ::TokenBucketConfiguration
) {
    val bucket = Bucket(pluginConfig.bucketSize)
    val refiller = Refiller(
        bucket = bucket,
        interval = pluginConfig.refillInterval
    )

    onCall { call ->
        if (!bucket.tryConsumeToken()) {
            call.respond(
                status = TooManyRequests,
                message = "🚫"
            )
        }
    }

    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.launch {
            refiller.run()
        }
    }
}

class TokenBucketConfiguration {
    var bucketSize = 5
    var refillInterval = 1.minutes
}


class Refiller(val bucket: Bucket, val interval: Duration) {
    suspend fun run() {
        while (coroutineContext.isActive) {
            delay(interval)
            bucket.refillTokens()
        }
    }
}
class Bucket(private val capacity: Int) {
    var availableTokens = AtomicInteger(capacity)
    fun tryConsumeToken(): Boolean {
        if (availableTokens.get() <= 0) {
            println("Not enough tokens available")
            return false
        }

        val remainingTokens = availableTokens.decrementAndGet()
        println("Remaining tokens: $remainingTokens/$capacity")
        return true
    }

    fun refillTokens() {
        availableTokens.set(capacity)
        println("Refilled tokens: $availableTokens/$capacity")
    }
}