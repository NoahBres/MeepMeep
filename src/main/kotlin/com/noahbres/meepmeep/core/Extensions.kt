package com.noahbres.meepmeep.core

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.core.util.FieldUtil

/**
 * Extension function to convert a Vector2d object to screen coordinates.
 *
 * @return The screen coordinates as a Vector2d object.
 */
fun Vector2d.toScreenCoord() = FieldUtil.fieldCoordsToScreenCoords(this)

/**
 * Extension function to scale a measurement in inches to pixels.
 *
 * @return The measurement in pixels.
 */
fun Double.scaleInToPixel() = FieldUtil.scaleInchesToPixel(this)

/**
 * Extension function to convert a measurement in radians to degrees.
 *
 * @return The measurement in degrees.
 */
fun Double.toDegrees() = Math.toDegrees(this)

/**
 * Extension function to convert a measurement in degrees to radians.
 *
 * @return The measurement in radians.
 */
fun Double.toRadians() = Math.toRadians(this)

/** Extension property to enforce exhaustiveness in when statements. */
val <T> T.exhaustive: T
    get() = this
