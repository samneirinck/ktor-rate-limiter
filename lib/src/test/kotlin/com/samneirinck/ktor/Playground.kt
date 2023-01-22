package com.samneirinck.ktor

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.junit.jupiter.api.Test

class Playground {
    @Test
    fun tryItOut() {
        embeddedServer(Netty, port = 8282, host = "0.0.0.0", module = Application::myModule)
            .start(wait = true)
    }
}

fun Application.myModule() {
//    install(TokenBucketRateLimiter) {
//        bucketSize = 10
//        refillInterval = 10.seconds
//    }
    install(LeakingBucketRateLimiter) {

    }

    routing {
        get("/") {
            call.respondText("✅")
        }
    }
}