package com.samneirinck.ktor

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.response.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.minutes

val LeakingBucketRateLimiter = createApplicationPlugin(
    name = "LeakingBucketRateLimiter",
    createConfiguration = ::LeakingBucketConfiguration
) {
    val queue = ConcurrentLinkedQueue<Channel<Unit>>()

    onCall { call ->
        if (queue.size >= pluginConfig.bucketSize) {
            call.respond(
                status = HttpStatusCode.TooManyRequests,
                message = "🚫"
            )
        } else {
            val channel = Channel<Unit>(0)
            queue.add(channel)
            channel.receive()
        }
    }

    on(MonitoringEvent(ApplicationStarted)) { application ->
        application.launch {
            val delay = pluginConfig.outflowRate / pluginConfig.bucketSize
            while  (coroutineContext.isActive) {
                delay(delay)
                queue.poll()?.trySend(Unit)
            }
        }
    }
}

class LeakingBucketConfiguration {
    var bucketSize = 5
    var outflowRate = 1.minutes
}
