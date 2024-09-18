package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.MarkerCallback
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.anim.AnimationController
import com.noahbres.meepmeep.core.anim.Ease
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.scaleInToPixel
import com.noahbres.meepmeep.core.toRadians
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import java.awt.BasicStroke
import java.awt.Graphics2D
import kotlin.math.cos
import kotlin.math.sin

/**
 * Entity representing a marker in the MeepMeep simulation.
 *
 * @property meepMeep The MeepMeep instance.
 * @property colorScheme The color scheme used for rendering.
 * @property pos The position of the marker.
 * @property callback The callback to be invoked when the marker is
 *    reached.
 * @property time The time at which the marker is reached.
 */
class MarkerIndicatorEntity(
    override val meepMeep: MeepMeep,
    private var colorScheme: ColorScheme,
    private val pos: Pose2d,
    private val callback: MarkerCallback,
    val time: Double,
) : ThemedEntity {
    /** Tag for the marker entity. */
    override val tag = "MARKER_INDICATOR_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex: Int = 0

    /** Canvas width. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** Canvas height. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    // Radius of the marker's X shape
    private val markerXRadius = 0.15

    // Stroke width of the marker's X shape
    private val markerXStrokeWidth = 0.3

    // Radius of the marker's circle
    private val markerCircleRadius = 3.9

    // Stroke width of the marker's circle
    private val markerCircleStrokeWidth = 0.4

    // Current radius of the marker's circle
    private var currentCircleRadius = markerCircleRadius

    // Animation controller for the marker's circle radius
    private val animationController = AnimationController(markerCircleRadius).clip(
        0.0, markerCircleRadius
    )

    // Flag indicating weather the marker has been passed
    private var passed = false

    /**
     * Updates the MarkerIndicator entity.
     *
     * @param deltaTime The time since the last update.
     */
    override fun update(deltaTime: Long) {
        currentCircleRadius = animationController.value

        animationController.update()
    }

    /**
     * Renders the marker on the given graphics context.
     *
     * @param gfx The graphics context.
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        gfx.color = colorScheme.trajectoryMarkerColor

        // Calculate the coordinates  for the X shape of the marker
        val xLeftUp = ((Vector2d(
            cos((-45.0).toRadians()), sin((-45.0).toRadians())
        ) * markerXRadius.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()
        val xLeftDown = ((Vector2d(
            cos((135.0).toRadians()), sin((135.0).toRadians())
        ) * markerXRadius.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()
        val xRightUp = ((Vector2d(
            cos((45.0).toRadians()), sin((45.0).toRadians())
        ) * markerXRadius.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()
        val xRightDown = ((Vector2d(
            cos((225.0).toRadians()), sin((255.0).toRadians())
        ) * markerXRadius.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()

        // Draw the X shape of the marker
        gfx.stroke = BasicStroke(markerXStrokeWidth.scaleInToPixel().toFloat())
        gfx.drawLine(
            xLeftUp.x.toInt(), xLeftUp.y.toInt(),
            xLeftDown.x.toInt(), xLeftDown.y.toInt()
        )
        gfx.drawLine(
            xRightUp.x.toInt(), xRightUp.y.toInt(),
            xRightDown.x.toInt(), xRightDown.y.toInt()
        )

        // Draw the circle of the marker
        gfx.stroke = BasicStroke(markerCircleStrokeWidth.scaleInToPixel().toFloat())
        gfx.drawArc(
            (pos.vec().toScreenCoord().x - currentCircleRadius.scaleInToPixel() / 2).toInt(),
            (pos.vec().toScreenCoord().y - currentCircleRadius.scaleInToPixel() / 2).toInt(),
            currentCircleRadius.scaleInToPixel().toInt(),
            currentCircleRadius.scaleInToPixel().toInt(),
            0, 360
        )
    }

    /**
     * Sets the dimensions of the canvas.
     *
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    /**
     * Switches the color scheme of the marker.
     *
     * @param scheme The new color scheme.
     */
    override fun switchScheme(scheme: ColorScheme) {
        this.colorScheme = scheme
    }

    /** Marks the indicator as passed and triggers the callback. */
    fun passed() {
        if (!passed) {
            passed = true

            // Animate the circle radius to 0
            animationController.anim(0.0, 200.0, Ease.EASE_IN_OUT_CUBIC)

            // Trigger the callback
            callback.onMarkerReached()
        }
    }

    /** Resets the marker indicator to its initial state. */
    fun reset() {
        if (passed) {
            passed = false

            // Animate the circle radius back to its original value
            animationController.anim(markerCircleRadius, 200.0, Ease.EASE_IN_CUBIC)
        }
    }
}