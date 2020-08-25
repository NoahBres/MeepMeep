package com.noahbres.meepmeep.core.anim

// Eases based off of https://gist.github.com/gre/1650294
class Ease {
    companion object {
        @JvmStatic
        val LINEAR: (t: Double) -> Double = { it }

        @JvmStatic
        val EASE_IN_QUAD: (t: Double) -> Double = { it * it }

        @JvmStatic
        val EASE_OUT_QUAD: (t: Double) -> Double = { it * (2 - it) }

        @JvmStatic
        val EASE_IN_OUT_QUAD: (t: Double) -> Double = { if (it < 0.5) 2 * it * it else -1 + (4 - 2 * it) * it }

        @JvmStatic
        val EASE_IN_CUBIC: (t: Double) -> Double = { it * it * it }

        @JvmStatic
        val EASE_OUT_CUBIC: (t: Double) -> Double = {
            var t = it
            (--t) * t * t + 1
        }

        @JvmStatic
        val EASE_IN_OUT_CUBIC: (t: Double) -> Double = { if (it < 0.5) 4 * it * it * it else (it - 1) * (2 * it - 2) * (2 * it - 2) + 1 }

        @JvmStatic
        val EASE_IN_QUART: (t: Double) -> Double = { it * it * it * it }

        @JvmStatic
        val EASE_OUT_QUART: (t: Double) -> Double = {
            var t = it
            1 - (--t) * t * t * t
        }

        @JvmStatic
        val EASE_IN_OUT_QUART: (t: Double) -> Double = {
            var t = it
            if (t < 0.5) 8 * it * it * it * it else 1 - 8 * (--t) * t * t * t
        }

        @JvmStatic
        val EASE_IN_QUINT: (t: Double) -> Double = { it * it * it * it * it }

        @JvmStatic
        val EASE_OUT_QUINT: (t: Double) -> Double = {
            var t = it
            1 + (--t) * t * t * t * t
        }

        @JvmStatic
        val EASE_IN_OUT_QUINT: (t: Double) -> Double = {
            var t = it
            if (t < 0.5) 16 * t * t * t * t * t else 1 + 16 * (--t) * t * t * t * t
        }
    }
}