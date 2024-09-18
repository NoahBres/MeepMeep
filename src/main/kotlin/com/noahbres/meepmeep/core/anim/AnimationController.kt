package com.noahbres.meepmeep.core.anim

import kotlin.math.max
import kotlin.math.min

/**
 * Class that represents an animation controller. It can be used to animate
 * a value towards a target value over a specified time using a given
 * easing function. The animation can be clipped to a specified range.
 *
 * @property value The current value of the animation.
 */
class AnimationController(var value: Double) {
    private var isAnimating = false

    private var currentStartTime = 0.0
    private var currentEndTime = 0.0
    private var currentTotalTime = 0.0

    private var currentTargetStart = 0.0
    private var currentTargetDelta = 0.0
    private var currentEase = Ease.LINEAR

    private var isClipped = false
    private var lowerBound = 0.0
    private var upperBound = 0.0

    /**
     * Updates the [AnimationController] instance. This method should be called
     * regularly to update the animation state. It calculates the progress of
     * the animation and updates the value accordingly. If clipping is enabled,
     * the value is clipped to the specified bounds. The animation stops if it
     * has reached the end.
     */
    fun update() {
        if (!isAnimating) return

        // Calculate elapsed time since the animation started
        val elapsedTime = System.currentTimeMillis() - currentStartTime

        // Calculate the progress of the animation using the current ease function
        val progress = currentEase(elapsedTime / currentTotalTime)

        // Calculate the new value of the animation based on the progress
        value = currentTargetStart + progress * currentTargetDelta

        // Clip the value if clipping is enabled
        if (isClipped) value = max(lowerBound, min(value, upperBound))

        // Stop the animation if it has reached the end
        if (progress >= 1) isAnimating = false
    }

    /**
     * Starts a new animation towards the target value over the specified time
     * using the given easing function.
     *
     * @param target The target value to animate to.
     * @param timeMs The duration of the animation in milliseconds.
     * @param ease The easing function to use for the animation.
     * @throws Error if the animation length is less than or equal to 0.
     */
    fun anim(target: Double, timeMs: Double, ease: (t: Double) -> Double) {
        if (timeMs <= 0) throw Error("Animation length can not be equal or less than 0")

        // If the target value is the same as the current value, return
        if (target == value) return

        isAnimating = true

        // Set the start and target values for the animation
        currentTargetStart = value
        currentTargetDelta = target - value

        // Set the start and end times for the animation
        currentStartTime = System.currentTimeMillis().toDouble()
        currentEndTime = currentStartTime + timeMs
        currentTotalTime = currentEndTime - currentStartTime

        // Set the ease function for the animation
        currentEase = ease
    }

    /**
     * Sets the bounds for the animation value. When clipping is enabled, the
     * value will be constrained within the specified lower and upper bounds.
     *
     * @param lowerBound The lower bound for the animation value.
     * @param upperBound The upper bound for the animation value.
     * @return The [AnimationController] instance with updated bounds.
     */
    fun clip(lowerBound: Double, upperBound: Double): AnimationController {
        this.isClipped = true
        this.lowerBound = lowerBound
        this.upperBound = upperBound

        return this
    }
}
