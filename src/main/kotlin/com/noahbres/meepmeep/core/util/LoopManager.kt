package com.noahbres.meepmeep.core.util

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class LoopManager(targetFPS: Int, val updateFunction: (deltaTime: Long) -> Unit, val renderFunction: () -> Unit) {
    private val targetDeltaLoopTime = (1000L * 1000 * 1000) / targetFPS // Nanoseconds / fps

    var fps = 0.0
        private set
    private var fpsCount = 0
    private var fpsCounterInterval = 500L * 1000 * 1000
    private var fpsCounterStartTime = System.currentTimeMillis()

    private var beginLoopTime: Long = System.nanoTime()
    private var lastBeginTime: Long = System.nanoTime()

    private val service = Executors.newSingleThreadScheduledExecutor()

    fun start() {
        service.scheduleAtFixedRate(::loop, 0L, targetDeltaLoopTime, TimeUnit.NANOSECONDS)
    }

    private fun loop() {
        beginLoopTime = System.nanoTime()

        if (beginLoopTime - fpsCounterStartTime > fpsCounterInterval) {
            fps = fpsCount.toDouble() / ((beginLoopTime - fpsCounterStartTime).toDouble() / (1000 * 1000 * 1000))
            fpsCount = 0
            fpsCounterStartTime = beginLoopTime
        }

        fpsCount++

        update(beginLoopTime - lastBeginTime)
        render()

        lastBeginTime = beginLoopTime
    }

    private fun render() {
        renderFunction()
    }

    private fun update(deltaTime: Long) {
        updateFunction(deltaTime)
    }
}