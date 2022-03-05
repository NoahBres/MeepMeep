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

class MarkerIndicatorEntity(
        override val meepMeep: MeepMeep,
        private var colorScheme: ColorScheme,
        private val pos: Pose2d,
        private val callback: MarkerCallback,
        val time: Double,
) : ThemedEntity {
    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    override val tag = "MARKER_INDICATOR_ENTITY"

    override var zIndex: Int = 0

    private val MARKER_X_RADIUS = 0.15
    private val MARKER_X_STROKE_WIDTH = 0.3
    private val MARKER_CIRCLE_RADIUS = 3.9
    private val MARKER_CIRCLE_STROKE_WIDTH = 0.4

    private var currentCircleRadius = MARKER_CIRCLE_RADIUS
    private val animationController = AnimationController(MARKER_CIRCLE_RADIUS).clip(
            0.0, MARKER_CIRCLE_RADIUS
    )

    private var passed = false

    override fun update(deltaTime: Long) {
        currentCircleRadius = animationController.value

        animationController.update()
    }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        gfx.color = colorScheme.TRAJECTORY_MARKER_COLOR

        val X_LEFT_UP = ((Vector2d(
                cos((-45.0).toRadians()), sin((-45.0).toRadians())
        ) * MARKER_X_RADIUS.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()
        val X_LEFT_DOWN = ((Vector2d(
                cos((135.0).toRadians()), sin((135.0).toRadians())
        ) * MARKER_X_RADIUS.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()
        val X_RIGHT_UP = ((Vector2d(
                cos((45.0).toRadians()), sin((45.0).toRadians())
        ) * MARKER_X_RADIUS.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()
        val X_RIGHT_DOWN = ((Vector2d(
                cos((225.0).toRadians()), sin((255.0).toRadians())
        ) * MARKER_X_RADIUS.scaleInToPixel()).rotated(pos.heading) + pos.vec()).toScreenCoord()

        gfx.stroke = BasicStroke(MARKER_X_STROKE_WIDTH.scaleInToPixel().toFloat())
        gfx.drawLine(
                X_LEFT_UP.x.toInt(), X_LEFT_UP.y.toInt(),
                X_LEFT_DOWN.x.toInt(), X_LEFT_DOWN.y.toInt()
        )
        gfx.drawLine(
                X_RIGHT_UP.x.toInt(), X_RIGHT_UP.y.toInt(),
                X_RIGHT_DOWN.x.toInt(), X_RIGHT_DOWN.y.toInt()
        )

        gfx.stroke = BasicStroke(MARKER_CIRCLE_STROKE_WIDTH.scaleInToPixel().toFloat())
        gfx.drawArc(
                (pos.vec().toScreenCoord().x - currentCircleRadius.scaleInToPixel() / 2).toInt(),
                (pos.vec().toScreenCoord().y - currentCircleRadius.scaleInToPixel() / 2).toInt(),
                currentCircleRadius.scaleInToPixel().toInt(),
                currentCircleRadius.scaleInToPixel().toInt(),
                0, 360
        )
    }

    fun passed() {
        if (!passed) {
            passed = true
            animationController.anim(0.0, 200.0, Ease.EASE_IN_OUT_CUBIC)
            callback.onMarkerReached()
        }
    }

    fun reset() {
        if (passed) {
            passed = false
            animationController.anim(MARKER_CIRCLE_RADIUS, 200.0, Ease.EASE_IN_CUBIC)
        }
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        this.colorScheme = scheme
    }
}