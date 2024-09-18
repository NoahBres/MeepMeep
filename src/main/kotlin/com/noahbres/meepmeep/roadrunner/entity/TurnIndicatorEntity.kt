package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.scaleInToPixel
import com.noahbres.meepmeep.core.toDegrees
import com.noahbres.meepmeep.core.toRadians
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import java.awt.BasicStroke
import java.awt.Graphics2D
import kotlin.math.abs
import kotlin.math.min

class TurnIndicatorEntity(
    override val meepMeep: MeepMeep,
    private var colorScheme: ColorScheme,
    private val pos: Vector2d,
    private val startAngle: Double,
    private val endAngle: Double,
) : ThemedEntity {
    /** The tag for the turn indicator entity. */
    override val tag = "TURN_INDICATOR_ENTITY"

    /** The z-index of the turn indicator entity. */
    override var zIndex: Int = 0

    /** The width of the canvas. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** The height of the canvas. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    /** The radius of the turn circle. */
    private val turnCircleRadius = 1.0

    /** The radius of the turn arc. */
    private val turnArcRadius = 7.5

    /** The width of the turn stroke. */
    private val turnStrokeWidth = 0.5

    /** The length of the turn arrow. */
    private val turnArrowLength = 1.5

    /** The angle of the turn arrow. */
    private val turnArrowAngle = 30.0.toRadians()

    /** The angle adjustment for the turn arrow. */
    private val turnArrowAngleAdjustment = (-12.5).toRadians()

    /**
     * Updates the turn indicator entity.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    override fun update(deltaTime: Long) {}

    /**
     * Renders the turn indicator on the given graphics context.
     *
     * @param gfx The graphics context.
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun render(
        gfx: Graphics2D,
        canvasWidth: Int,
        canvasHeight: Int,
    ) {
        // Set the color and stroke for the turn indicator
        gfx.color = colorScheme.trajectoryTurnColor
        gfx.stroke =
            BasicStroke(
                turnStrokeWidth.scaleInToPixel().toFloat(),
                BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND,
            )

        // Draw the turn circle
        gfx.fillOval(
            (pos.toScreenCoord().x - turnCircleRadius.scaleInToPixel() / 2).toInt(),
            (pos.toScreenCoord().y - turnCircleRadius.scaleInToPixel() / 2).toInt(),
            turnCircleRadius.scaleInToPixel().toInt(),
            turnCircleRadius.scaleInToPixel().toInt(),
        )

        // Draw the turn arc
        gfx.drawArc(
            (pos.toScreenCoord().x - turnArcRadius.scaleInToPixel() / 2).toInt(),
            (pos.toScreenCoord().y - turnArcRadius.scaleInToPixel() / 2).toInt(),
            turnArcRadius.scaleInToPixel().toInt(),
            turnArcRadius.scaleInToPixel().toInt(),
            min(startAngle.toDegrees().toInt(), endAngle.toDegrees().toInt()),
            abs(startAngle.toDegrees().toInt() - endAngle.toDegrees().toInt()),
        )

        // Calculate the arrow point vector and its screen coordinates
        val arrowPointVec = Vector2d(turnArcRadius / 2, 0.0).rotated(endAngle)
        val translatedPoint = (pos + arrowPointVec).toScreenCoord()

        // Calculate the rotation for the first arrow line
        var arrow1Rotated =
            endAngle - 90.0.toRadians() + turnArrowAngle + turnArrowAngleAdjustment
        if (endAngle < startAngle) arrow1Rotated = 360.0.toRadians() - arrow1Rotated

        // Calculate the rotation for the second arrow line
        var arrow2Rotated =
            endAngle - 90.0.toRadians() - turnArrowAngle + turnArrowAngleAdjustment
        if (endAngle < startAngle) arrow2Rotated = 360.0.toRadians() - arrow2Rotated

        // Calculate the end points for the arrow lines and their screen coordinates
        val arrowEndVec1 =
            (pos + arrowPointVec) +
                Vector2d(turnArrowLength, 0.0)
                    .rotated(arrow1Rotated)
        val translatedArrowEndVec1 = arrowEndVec1.toScreenCoord()

        val arrowEndVec2 =
            (pos + arrowPointVec) + Vector2d(turnArrowLength, 0.0).rotated(arrow2Rotated)
        val translatedArrowEndVec2 = arrowEndVec2.toScreenCoord()

        // Draw the arrow lines
        gfx.drawLine(
            translatedPoint.x.toInt(),
            translatedPoint.y.toInt(),
            translatedArrowEndVec1.x.toInt(),
            translatedArrowEndVec1.y.toInt(),
        )
        gfx.drawLine(
            translatedPoint.x.toInt(),
            translatedPoint.y.toInt(),
            translatedArrowEndVec2.x.toInt(),
            translatedArrowEndVec2.y.toInt(),
        )
    }

    /**
     * Sets the dimensions of the canvas.
     *
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun setCanvasDimensions(
        canvasWidth: Double,
        canvasHeight: Double,
    ) {
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    /**
     * Switches the color scheme of the turn indicator.
     *
     * @param scheme The new color scheme.
     */
    override fun switchScheme(scheme: ColorScheme) {
        this.colorScheme = scheme
    }
}
