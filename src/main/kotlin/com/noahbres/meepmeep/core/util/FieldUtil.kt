package com.noahbres.meepmeep.core.util

import com.acmerobotics.roadrunner.geometry.Vector2d

import kotlin.math.max
import kotlin.math.min

class FieldUtil {
    companion object {
        @JvmStatic
        var FIELD_WIDTH = 141

        @JvmStatic
        var FIELD_HEIGHT = 141

        @JvmStatic
        var CANVAS_WIDTH = 0.0
        var CANVAS_HEIGHT = 0.0

        @JvmStatic
        @JvmOverloads
        fun screenCoordsToFieldCoords(vector2d: Vector2d, canvasWidth: Double = CANVAS_WIDTH, canvasHeight: Double = CANVAS_HEIGHT): Vector2d {
            return mirrorY(vector2d) / max(canvasWidth, canvasHeight) * FIELD_WIDTH.toDouble() + Vector2d(-FIELD_WIDTH / 2.0, FIELD_HEIGHT / 2.0)
        }

        @JvmStatic
        @JvmOverloads
        fun fieldCoordsToScreenCoords(vector2d: Vector2d, canvasWidth: Double = CANVAS_WIDTH, canvasHeight: Double = CANVAS_HEIGHT): Vector2d {
            return (mirrorY(vector2d) + Vector2d(FIELD_WIDTH / 2.0, FIELD_HEIGHT / 2.0)) * min(canvasWidth, canvasHeight) / FIELD_WIDTH.toDouble()
        }

        @JvmStatic
        fun scaleInchesToPixel(inches: Double, canvasWidth: Double = CANVAS_WIDTH, canvasHeight: Double = CANVAS_HEIGHT): Double {
            return inches / min(FIELD_WIDTH.toDouble(), FIELD_HEIGHT.toDouble()) * min(canvasWidth, canvasHeight)
        }

        // Mirror x
        private fun mirrorX(vector: Vector2d): Vector2d {
            return Vector2d(-vector.x, vector.y)
        }

        // Mirror y
        private fun mirrorY(vector: Vector2d): Vector2d {
            return Vector2d(vector.x, -vector.y)
        }
    }
}
