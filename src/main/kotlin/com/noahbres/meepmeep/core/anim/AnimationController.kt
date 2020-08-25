package com.noahbres.meepmeep.core.anim

import kotlin.math.max
import kotlin.math.min

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

    fun update() {
        if(!isAnimating) return

        val elapsedTime = System.currentTimeMillis() - currentStartTime
        val progress = currentEase(elapsedTime / currentTotalTime)

        value = currentTargetStart + progress * currentTargetDelta

        if(isClipped) value = max(lowerBound, min(value, upperBound))

        if(progress >= 1) isAnimating = false
    }

    fun anim(target: Double, timeMs: Double, ease: (t: Double) -> Double) {
        if(timeMs <= 0) throw Error("Animation length can not be equal or less than 0")

        if(target == value) return

        isAnimating = true

        currentTargetStart = value
        currentTargetDelta = target - value

        currentStartTime = System.currentTimeMillis().toDouble()
        currentEndTime = currentStartTime + timeMs
        currentTotalTime = currentEndTime - currentStartTime

        currentEase = ease
    }

    fun clip(lowerBound: Double, upperBound: Double): AnimationController {
        this.isClipped = true
        this.lowerBound = lowerBound
        this.upperBound = upperBound

        return this
    }
}
