package com.noahbres.meepmeep.core.util

import com.acmerobotics.roadrunner.geometry.Vector2d

import kotlin.math.max
import kotlin.math.min

/** Utility class for field-related calculations and conversions. */
class FieldUtil {
    companion object {
        /** The width of the field. */
        @JvmStatic
        var FIELD_WIDTH = 141

        /** The height of the field. */
        @JvmStatic
        var FIELD_HEIGHT = 143

        /** The width of the canvas in pixels. */
        @JvmStatic
        var CANVAS_WIDTH = 0.0

        /** The height of the canvas in pixels. */
        @JvmStatic
        var CANVAS_HEIGHT = 0.0

        /**
         * Converts screen coordinates to field coordinates.
         *
         * @param vector2d The screen coordinates as a Vector2d object.
         * @param canvasWidth The width of the canvas.
         * @param canvasHeight The height of the canvas.
         * @return The field coordinates as a Vector2d object.
         */
        @JvmStatic
        @JvmOverloads
        fun screenCoordsToFieldCoords(
            vector2d: Vector2d,
            canvasWidth: Double = CANVAS_WIDTH,
            canvasHeight: Double = CANVAS_HEIGHT
        ): Vector2d {
            // Mirror the Y coordinate and scale to field dimensions
            return mirrorY(vector2d) / max(
                canvasWidth,
                canvasHeight
            ) * FIELD_WIDTH.toDouble() + Vector2d(-FIELD_WIDTH / 2.0, FIELD_HEIGHT / 2.0)
        }

        /**
         * Converts field coordinates to screen coordinates.
         *
         * @param vector2d The field coordinates as a Vector2d object.
         * @param canvasWidth The width of the canvas.
         * @param canvasHeight The height of the canvas.
         * @return The screen coordinates as a Vector2d object.
         */
        @JvmStatic
        @JvmOverloads
        fun fieldCoordsToScreenCoords(
            vector2d: Vector2d,
            canvasWidth: Double = CANVAS_WIDTH,
            canvasHeight: Double = CANVAS_HEIGHT
        ): Vector2d {
            // Mirror the Y coordinate and scale to screen dimensions
            return (mirrorY(vector2d) + Vector2d(FIELD_WIDTH / 2.0, FIELD_HEIGHT / 2.0)) * min(
                canvasWidth,
                canvasHeight
            ) / FIELD_WIDTH.toDouble()
        }

        /**
         * Scales inches to pixels based on canvas dimensions.
         *
         * @param inches The measurement in inches.
         * @param canvasWidth The width of the canvas.
         * @param canvasHeight The height of the canvas.
         * @return The measurement in pixels.
         */
        @JvmStatic
        fun scaleInchesToPixel(
            inches: Double,
            canvasWidth: Double = CANVAS_WIDTH,
            canvasHeight: Double = CANVAS_HEIGHT
        ): Double {
            // Scale inches to pixels based on the smaller dimension of the canvas
            return inches / min(FIELD_WIDTH.toDouble(), FIELD_HEIGHT.toDouble()) * min(
                canvasWidth,
                canvasHeight
            )
        }

        /**
         * Mirrors the X coordinate of a vector.
         *
         * @param vector The vector to mirror.
         * @return The mirrored vector.
         */
        private fun mirrorX(vector: Vector2d): Vector2d {
            return Vector2d(-vector.x, vector.y)
        }


        /**
         * Mirrors the Y coordinate of a vector.
         *
         * @param vector The vector to mirror.
         * @return The mirrored vector.
         */
        private fun mirrorY(vector: Vector2d): Vector2d {
            return Vector2d(vector.x, -vector.y)
        }
    }
}
