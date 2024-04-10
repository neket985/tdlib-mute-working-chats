package ru.smirnov.muteworkingchats.worker

import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.time.DurationUnit
import kotlin.time.toTimeUnit

interface WorkerI {

    fun awaitForComplete(timeout: Duration = Duration.ofMinutes(5), block: (CompletableFuture<Any>) -> Unit) {
        val future = CompletableFuture<Any>()
        block(future)
        future.get(timeout.seconds, DurationUnit.SECONDS.toTimeUnit())
    }
}