package ru.smirnov.muteworkingchats.worker

import java.time.Duration
import java.util.concurrent.CompletableFuture
import kotlin.time.DurationUnit
import kotlin.time.toTimeUnit

interface WorkerI {

    fun <T> awaitForComplete(timeout: Duration = Duration.ofMinutes(5), block: (CompletableFuture<T>) -> Unit): T {
        val future = CompletableFuture<T>()
        block(future)
        return future.get(timeout.seconds, DurationUnit.SECONDS.toTimeUnit())
    }
}