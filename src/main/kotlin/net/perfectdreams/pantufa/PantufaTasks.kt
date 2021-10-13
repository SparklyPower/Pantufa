package net.perfectdreams.pantufa

import net.perfectdreams.pantufa.threads.UpdateCachedGraphs
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class PantufaTasks(private val pantufa: PantufaBot) {
    private val executorService = Executors.newScheduledThreadPool(1)

    fun start() {
        executorService.scheduleWithFixedDelay(
            UpdateCachedGraphs(pantufa),
            0L,
            1L,
            TimeUnit.MINUTES
        )
    }

    fun shutdown() {
        executorService.shutdown()
    }
}