package com.noahbres.meepmeep.core.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.util.FieldUtil
import java.awt.Color
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import kotlin.math.atan2

/** Width of the bot's wheels. */
private const val WHEEL_WIDTH = 0.2

/** The height of the bot's wheels. */
private const val WHEEL_HEIGHT = 0.3

/** The padding between the wheel and the bot's body in the X direction. */
private const val WHEEL_PADDING_X = 0.05

/** The padding between the wheel and the bot's body in the Y direction. */
private const val WHEEL_PADDING_Y = 0.05

/** The width of the line indicating the bot's direction. */
private const val DIRECTION_LINE_WIDTH = 0.05

/** The height of the line indicating the bot's direction. */
private const val DIRECTION_LINE_HEIGHT = 0.4

/**
 * Represents a bot entity in the MeepMeep simulation.
 *
 * @property meepMeep The MeepMeep instance.
 * @property width The width of the bot.
 * @property height The height of the bot.
 * @property pose The pose of the bot.
 * @property colorScheme The color scheme of the bot.
 * @property opacity The opacity of the bot.
 */
open class BotEntity(
    override val meepMeep: MeepMeep,
    private var width: Double,
    private var height: Double,

    var pose: Pose2d,
    private var colorScheme: ColorScheme,
    private val opacity: Double
): ThemedEntity {
    /** Tag for the bot entity. */
    override val tag = "DEFAULT_BOT_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex = 0

    /** Canvas width. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** Canvas height. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    /** Buffered image for rendering the bot. */
    private lateinit var baseBufferedImage: BufferedImage

    /** Initializes the bot entity and draws the bot. */
    init {
        redraw()
    }

    /**
     * Updates the bot entity.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    override fun update(deltaTime: Long) {
        pose = Pose2d(pose.x, pose.y, pose.heading)
    }

    /** Redraws the bot on the buffered image. */
    private fun redraw() {
        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the bot
        baseBufferedImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )

        val gfx = baseBufferedImage.createGraphics()

        // Set rendering hints for the graphics
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Draw the bot's body
        val colorAlphaBody = Color(
            colorScheme.botBodyColor.red, colorScheme.botBodyColor.green,
            colorScheme.botBodyColor.blue, (opacity * 255).toInt()
        )
        gfx.color = colorAlphaBody
        gfx.fillRect(0, 0, canvasWidth.toInt(), canvasHeight.toInt())

        // Draw the bot's wheels
        val colorAlphaWheel = Color(
            colorScheme.botWheelColor.red, colorScheme.botWheelColor.green,
            colorScheme.botBodyColor.blue, (opacity * 255).toInt()
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

        // Draw the bot's direction line
        val colorAlphaDirection = Color(
            colorScheme.botDirectionColor.red, colorScheme.botDirectionColor.green,
            colorScheme.botDirectionColor.blue, (opacity * 255).toInt()
        )
        gfx.color = colorAlphaDirection
        gfx.fillRect(
            (canvasWidth / 2 - DIRECTION_LINE_WIDTH * canvasWidth / 2).toInt(), 0,
            (DIRECTION_LINE_WIDTH * canvasWidth).toInt(),
            (canvasHeight * DIRECTION_LINE_HEIGHT).toInt()
        )
    }

    /**
     * Renders the bot on the given graphics context.
     *
     * @param gfx The graphics context.
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        // Convert field coordinates to screen coordinates
        val coords = FieldUtil.fieldCoordsToScreenCoords(Vector2d(pose.x, pose.y))

        // Create a transformation for the bot's position and orientation
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

        // Draw the bot image with the applied transformation
        gfx.drawImage(baseBufferedImage, transform, null)
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
     * Switches the color scheme of the bot.
     *
     * @param scheme The new color scheme.
     */
    override fun switchScheme(scheme: ColorScheme) {
        if (this.colorScheme != scheme) {
            colorScheme = scheme
            redraw()
        }
    }

    /**
     * Sets the dimensions of the bot.
     *
     * @param width The new width of the bot.
     * @param height The new height of the bot.
     * @return The updated BotEntity instance.
     */
    fun setDimensions(width: Double, height: Double): BotEntity {
        if (this.width != width || this.height != height) redraw()

        this.width = width
        this.height = height

        return this
    }
}
