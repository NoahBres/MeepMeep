package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.*
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
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
        private val endAngle: Double
) : ThemedEntity {
    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    override val tag = "TURN_INDICATOR_ENTITY"

    override var zIndex: Int = 0

    private val TURN_CIRCLE_RADIUS = 1.0
    private val TURN_ARC_RADIUS = 7.5
    private val TURN_STROKE_WIDTH = 0.5
    private val TURN_ARROW_LENGTH = 1.5
    private val TURN_ARROW_ANGLE = 30.0.toRadians()
    private val TURN_ARROW_ANGLE_ADJUSTMENT = (-12.5).toRadians()

    override fun update(deltaTime: Long) {
    }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        gfx.color = colorScheme.TRAJECTORY_TURN_COLOR
        gfx.stroke = BasicStroke(TURN_STROKE_WIDTH.scaleInToPixel().toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        gfx.fillOval(
                (pos.toScreenCoord().x - TURN_CIRCLE_RADIUS.scaleInToPixel() / 2).toInt(),
                (pos.toScreenCoord().y - TURN_CIRCLE_RADIUS.scaleInToPixel() / 2).toInt(),
                TURN_CIRCLE_RADIUS.scaleInToPixel().toInt(), TURN_CIRCLE_RADIUS.scaleInToPixel().toInt()
        )

        gfx.drawArc(
                (pos.toScreenCoord().x - TURN_ARC_RADIUS.scaleInToPixel() / 2).toInt(),
                (pos.toScreenCoord().y - TURN_ARC_RADIUS.scaleInToPixel() / 2).toInt(),
                TURN_ARC_RADIUS.scaleInToPixel().toInt(), TURN_ARC_RADIUS.scaleInToPixel().toInt(),
                min(startAngle.toDegrees().toInt(), endAngle.toDegrees().toInt()),
                abs(startAngle.toDegrees().toInt() - endAngle.toDegrees().toInt())
        )

        val arrowPointVec = Vector2d(TURN_ARC_RADIUS / 2, 0.0).rotated(endAngle)
        val translatedPoint = (pos + arrowPointVec).toScreenCoord()

        var arrow1Rotated = endAngle - 90.0.toRadians() + TURN_ARROW_ANGLE + TURN_ARROW_ANGLE_ADJUSTMENT
        if (endAngle < startAngle) arrow1Rotated = 360.0.toRadians() - arrow1Rotated

        var arrow2Rotated = endAngle - 90.0.toRadians() - TURN_ARROW_ANGLE + TURN_ARROW_ANGLE_ADJUSTMENT
        if (endAngle < startAngle) arrow2Rotated = 360.0.toRadians() - arrow2Rotated

        val arrowEndVec1 = (pos + arrowPointVec) + Vector2d(TURN_ARROW_LENGTH, 0.0)
                .rotated(arrow1Rotated)
        val translatedArrowEndVec1 = arrowEndVec1.toScreenCoord()

        val arrowEndVec2 = (pos + arrowPointVec) + Vector2d(TURN_ARROW_LENGTH, 0.0).rotated(arrow2Rotated)
        val translatedArrowEndVec2 = arrowEndVec2.toScreenCoord()

        gfx.drawLine(translatedPoint.x.toInt(), translatedPoint.y.toInt(), translatedArrowEndVec1.x.toInt(), translatedArrowEndVec1.y.toInt())
        gfx.drawLine(translatedPoint.x.toInt(), translatedPoint.y.toInt(), translatedArrowEndVec2.x.toInt(), translatedArrowEndVec2.y.toInt())
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        this.colorScheme = scheme
    }
}