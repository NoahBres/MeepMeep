package com.noahbres.meepmeep.core.util

class LoopManager(targetFPS: Long, val updateFunction: (deltaTime: Long) -> Unit, val renderFunction: () -> Unit): Runnable {
    private val targetDeltaLoop = (1000 * 1000 * 1000) / targetFPS // Nanoseconds / fps

    private var running = true

    var fps = 0.0
    private var fpsCounter = 0
    private var fpsCounterTime = 1000
    override fun run() {
        var beginLoopTime: Long
        var endLoopTime: Long
        var currentUpdateTime = System.nanoTime()
        var lastUpdateTime: Long
        var deltaLoop: Long

        var startFpsTime = System.currentTimeMillis()

        while(running) {
            beginLoopTime = System.nanoTime()

            lastUpdateTime = currentUpdateTime
            currentUpdateTime = System.nanoTime()
            update((currentUpdateTime - lastUpdateTime) / (1000 * 1000))

            endLoopTime = System.nanoTime()
            deltaLoop = endLoopTime - beginLoopTime

            if(deltaLoop > targetDeltaLoop) {

            } else {
                Thread.sleep((targetDeltaLoop - deltaLoop) / (1000 * 1000))
            }

            render()

            fpsCounter++
            if(System.currentTimeMillis() - startFpsTime > fpsCounterTime) {
                fps = (fpsCounter.toDouble() / ((System.currentTimeMillis() - startFpsTime) / 1000))
                fpsCounter = 0
                startFpsTime = System.currentTimeMillis()
            }
        }
    }

    private fun render() {
        renderFunction()
    }

    private fun update(deltaTime: Long) {
        updateFunction(deltaTime)
    }
}