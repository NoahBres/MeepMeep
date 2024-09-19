package com.noahbres.meepmeep.core.util

import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

// Constant for nanoseconds in a second
private const val NANOSECONDS_IN_SECOND = 1_000_000_000L

/**
 * Manages the main loop for updating and rendering at a target frames per
 * second (FPS).
 *
 * @param targetFPS The target frames per second.
 * @param updateFunction The function to call for updating logic.
 * @param renderFunction The function to call for rendering.
 */
class LoopManager(
    targetFPS: Int, val updateFunction: (deltaTime: Long) -> Unit, val renderFunction: () -> Unit
) {
    // Target time per loop iteration in nanoseconds
    private val targetDeltaLoopTime = NANOSECONDS_IN_SECOND / targetFPS

    /** Frames per second, calculated and updated periodically. */
    var fps = 0.0
        private set

    // Counter for the number of frames rendered in the current interval
    private var fpsCount = 0

    // Interval for calculating FPS
    private var fpsCalculationInterval = 500L * 1_000_000

    // Start time for the current FPS calculation interval, in milliseconds
    private var fpsCounterStartTime = System.currentTimeMillis()

    // Time at the beginning of the current loop iteration, in nanoseconds
    private var currentLoopTime: Long = System.nanoTime()

    // Time at the beginning of the previous loop iteration, in nanoseconds
    private var previousBeginTime: Long = System.nanoTime()

    // Executor service for scheduling the loop at a fixed rate
    private val service = Executors.newSingleThreadScheduledExecutor()

    /** Starts the loop manager, scheduling the loop at a fixed rate. */
    fun start() {
        service.scheduleAtFixedRate(::loop, 0L, targetDeltaLoopTime, TimeUnit.NANOSECONDS)
    }

    /** The main loop function, called at a fixed rate. */
    private fun loop() {
        currentLoopTime = System.nanoTime()

        // Calculate FPS every fpsCalculationInterval
        if (currentLoopTime - fpsCounterStartTime > fpsCalculationInterval) {
            fps =
                    fpsCount.toDouble() / ((currentLoopTime - fpsCounterStartTime).toDouble() / NANOSECONDS_IN_SECOND)
            fpsCount = 0
            fpsCounterStartTime = currentLoopTime
        }

        fpsCount++

        // Update and render
        update(currentLoopTime - previousBeginTime)
        render()

        previousBeginTime = currentLoopTime
    }

    /** Calls the render function provided */
    private fun render() {
        renderFunction()
    }

    /**
     * Calls the update function provided with the delta time since the last
     * loop.
     *
     * @param deltaTime The time since the last loop in nanoseconds.
     */
    private fun update(deltaTime: Long) {
        updateFunction(deltaTime)
    }
}