package com.noahbres.meepmeep.core.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import kotlin.math.atan2

open class BotEntity(
    override val meepMeep: MeepMeep,
    private var width: Double,
    private var height: Double,

    var pose: Pose2d,
    private var colorScheme: ColorScheme,
    private val opacity: Double
) : ThemedEntity {
    override val tag = "DEFAULT_BOT_ENTITY"

    override var zIndex = 0

    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    private val WHEEL_WIDTH = 0.2
    private val WHEEL_HEIGHT = 0.3

    private val WHEEL_PADDING_X = 0.05
    private val WHEEL_PADDING_Y = 0.05

    private val DIRECTION_LINE_WIDTH = 0.05
    private val DIRECTION_LINE_HEIGHT = 0.4

    private lateinit var baseBufferedImage: BufferedImage

    init {
        redrawBot()
    }

    override fun update(deltaTime: Long) {
        pose = Pose2d(pose.x, pose.y, pose.heading)
    }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        val coords = FieldUtil.fieldCoordsToScreenCoords(Vector2d(pose.x, pose.y))

        val transform = AffineTransform()
        transform.translate(coords.x, coords.y)
        transform.rotate(atan2(pose.headingVec().x, pose.headingVec().y))
        transform.translate(
            FieldUtil.scaleInchesToPixel(-width / 2),
            FieldUtil.scaleInchesToPixel(-height / 2)
        )
        transform.scale(
            FieldUtil.scaleInchesToPixel(width) / canvasWidth,
            FieldUtil.scaleInchesToPixel(height) / canvasHeight
        )

        gfx.drawImage(baseBufferedImage, transform, null)
    }

    private fun redrawBot() {
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        baseBufferedImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )

        val gfx = baseBufferedImage.createGraphics()

        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val colorAlphaBody = Color(
            colorScheme.BOT_BODY_COLOR.red, colorScheme.BOT_BODY_COLOR.green,
            colorScheme.BOT_BODY_COLOR.blue, (opacity * 255).toInt()
        )
        gfx.color = colorAlphaBody
        gfx.fillRect(0, 0, canvasWidth.toInt(), canvasHeight.toInt())

        val colorAlphaWheel = Color(
            colorScheme.BOT_WHEEL_COLOR.red, colorScheme.BOT_WHEEL_COLOR.green,
            colorScheme.BOT_BODY_COLOR.blue, (opacity * 255).toInt()
        )
        gfx.color = colorAlphaWheel
        gfx.fillRect(
            (WHEEL_PADDING_X * canvasWidth).toInt(), (WHEEL_PADDING_Y * canvasHeight).toInt(),
            (WHEEL_WIDTH * canvasWidth).toInt(), (WHEEL_HEIGHT * canvasHeight).toInt()
        )
        gfx.fillRect(
            (canvasWidth - WHEEL_WIDTH * canvasWidth - WHEEL_PADDING_X * canvasWidth).toInt(),
            (WHEEL_PADDING_Y * canvasHeight).toInt(), (WHEEL_WIDTH * canvasWidth).toInt(),
            (WHEEL_HEIGHT * canvasHeight).toInt()
        )
        gfx.fillRect(
            (canvasWidth - WHEEL_WIDTH * canvasWidth - WHEEL_PADDING_X * canvasWidth).toInt(),
            (canvasHeight - WHEEL_HEIGHT * canvasHeight - WHEEL_PADDING_Y * canvasHeight).toInt(),
            (WHEEL_WIDTH * canvasWidth).toInt(), (WHEEL_HEIGHT * canvasHeight).toInt()
        )
        gfx.fillRect(
            (WHEEL_PADDING_X * canvasWidth).toInt(),
            (canvasHeight - WHEEL_HEIGHT * canvasHeight - WHEEL_PADDING_Y * canvasHeight).toInt(),
            (WHEEL_WIDTH * canvasWidth).toInt(), (WHEEL_HEIGHT * canvasHeight).toInt()
        )

        val colorAlphaDirection = Color(
            colorScheme.BOT_DIRECTION_COLOR.red, colorScheme.BOT_DIRECTION_COLOR.green,
            colorScheme.BOT_DIRECTION_COLOR.blue, (opacity * 255).toInt()
        )
        gfx.color = colorAlphaDirection
        gfx.fillRect(
            (canvasWidth / 2 - DIRECTION_LINE_WIDTH * canvasWidth / 2).toInt(), 0,
            (DIRECTION_LINE_WIDTH * canvasWidth).toInt(),
            (canvasHeight * DIRECTION_LINE_HEIGHT).toInt()
        )
    }

    override fun switchScheme(scheme: ColorScheme) {
        if (this.colorScheme != scheme) {
            colorScheme = scheme
            redrawBot()
        }
    }

    fun setDimensions(width: Double, height: Double): BotEntity {
        if (this.width != width || this.height != height) redrawBot()

        this.width = width
        this.height = height

        return this
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }
}
