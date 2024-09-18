package com.noahbres.meepmeep.core.entity

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.anim.AnimationController
import com.noahbres.meepmeep.core.anim.Ease
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.scaleInToPixel
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage

/** Length of the ticks on the axes. */
private const val TICK_LENGTH = 3.0

/** Thickness of the ticks on the axes. */
private const val TICK_THICKNESS = 0.45

/** Y-offset for the text on the X-axis in inches. */
private const val X_TEXT_Y_OFFSET = 0.3

/** X-offset for the label on the positive side of the X-axis in inches. */
private const val X_LABEL_X_OFFSET = 1.5

/** Y-offset for the label on the X-axis in inches. */
private const val X_LABEL_Y_OFFSET = -1.0

/** X-offset for the text on the positive side of the X-axis in inches. */
private const val X_TEXT_POSITIVE_X_OFFSET = -0.6

/** X-offset for the text on the negative side of the X-axis in inches. */
private const val X_TEXT_NEGATIVE_X_OFFSET = 0.7

/** X-offset for the text on the Y-axis in inches. */
private const val Y_TEXT_X_OFFSET = 0.6

/** X-offset for the label on the Y-axis in inches. */
private const val Y_LABEL_X_OFFSET = -4.0

/** Y-offset for the label on the Y-axis in inches. */
private const val Y_LABEL_Y_OFFSET = 3.0

/** Size of the hover target area in inches. */
private const val HOVER_TARGET = 20.0

/** Start point of the X-axis. */
private val X_START = -FieldUtil.FIELD_WIDTH / 2

/** End point of the X-axis. */
private val X_END = FieldUtil.FIELD_WIDTH / 2

/** Start point of the Y-axis. */
private val Y_START = -FieldUtil.FIELD_HEIGHT / 2

/** End point of the Y-axis. */
private val Y_END = FieldUtil.FIELD_HEIGHT / 2

/** Interval between ticks on the X-axis. */
private var X_INCREMENTS = 18

/** Interval between ticks on the Y-axis. */
private var Y_INCREMENTS = 18

/**
 * Entity representing the axes in the MeepMeep shell.
 *
 * @property meepMeep The MeepMeep instance.
 * @property axesThickness The thickness of the axes.
 * @property colorScheme The color scheme used for rendering the axes.
 * @property font The font used for rendering text on the axes.
 * @property fontSize The size of the font used for rendering text on the
 *    axes.
 */
class AxesEntity
@JvmOverloads constructor(
    override val meepMeep: MeepMeep,
    private val axesThickness: Double,
    private var colorScheme: ColorScheme,

    private var font: Font? = null,
    private var fontSize: Float = 20f
) : ThemedEntity, MouseMotionListener {
    /** Tag for the axes entity. */
    override val tag = "AXES_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex = 0

    /** Canvas width. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** Canvas height. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    /** Buffered image for rendering the axes. */
    private lateinit var baseBufferedImage: BufferedImage

    /** Current opacity of the axes. */
    private var currentOpacity = colorScheme.axisNormalOpacity

    /** Animation controller for the axes opacity. */
    private val animationController = AnimationController(currentOpacity).clip(0.0, 1.0)

    /** Initializes the axes entity and draws the axes. */
    init {
        redraw()
    }

    /**
     * Updates the axes entity.
     *
     * @param deltaTime The time since the last update.
     */
    override fun update(deltaTime: Long) {
        currentOpacity = animationController.value
        // TODO: fix issue that necessitates the controller needing a clip
        // Starts at like 30 or 28 for some reason
//        println(currentOpacity)
        animationController.update()
    }

    /** Redraws the axes on the buffered image. */
    private fun redraw() {
        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the axes
        baseBufferedImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )
        val gfx = baseBufferedImage.createGraphics()

        // Set rendering hints for the graphics
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Get the pixel thickness of the axes
        val pixelThickness = axesThickness.scaleInToPixel()

        // Draw the X-axis
        gfx.color = colorScheme.axisXColor
        gfx.fillRect(
            0, (canvasHeight / 2.0 - pixelThickness / 2).toInt(), canvasWidth.toInt(),
            pixelThickness.toInt()
        )

        // Draw the Y-axis
        gfx.color = colorScheme.axisYColor
        gfx.fillRect(
            (canvasWidth / 2.0 - pixelThickness / 2).toInt(), 0, pixelThickness.toInt(),
            canvasHeight.toInt()
        )

        // Check and set the font
        if (font != null) {
            gfx.font = font
        }
        val fontMetrics = gfx.fontMetrics

        // Draw ticks and labels for the X-axis
        gfx.color = colorScheme.axisXColor
        for (i in X_START..X_END step X_INCREMENTS) {
            if (i == 0) continue

            // Draw tick
            val tickCoords = FieldUtil.fieldCoordsToScreenCoords(
                Vector2d(i.toDouble() - (TICK_THICKNESS / 2), 0 + TICK_LENGTH / 2)
            )

            gfx.fillRect(
                tickCoords.x.toInt(),
                tickCoords.y.toInt(),
                FieldUtil.scaleInchesToPixel(TICK_THICKNESS).toInt(),
                FieldUtil.scaleInchesToPixel(TICK_LENGTH).toInt()
            )

            // Draw number
            var xOffsetIn: Double
            var xOffsetPx = 0.0
            if (i > 0) {
                xOffsetIn = X_TEXT_POSITIVE_X_OFFSET
                xOffsetPx = -fontMetrics.stringWidth(i.toString()).toDouble() // Right align
            } else {
                xOffsetIn = X_TEXT_NEGATIVE_X_OFFSET
            }

            val textCoords = Vector2d(
                i.toDouble() + xOffsetIn, X_TEXT_Y_OFFSET.scaleInToPixel()
            ).toScreenCoord()

            gfx.drawString(i.toString(), (textCoords.x + xOffsetPx).toInt(), textCoords.y.toInt())
        }

        // Increase font size for the X labels
        font = font?.deriveFont((fontSize * 1.2).toFloat())
        gfx.font = font

        val textNegativeXCoords = Vector2d(
            (X_START + X_LABEL_X_OFFSET / 2), X_LABEL_Y_OFFSET
        ).toScreenCoord()
        val textPositiveXCoords = Vector2d(
            (X_END - X_LABEL_X_OFFSET), X_LABEL_Y_OFFSET
        ).toScreenCoord()

        gfx.drawString(
            "-x", textNegativeXCoords.x.toInt(),
            (textNegativeXCoords.y + fontMetrics.ascent).toInt()
        )
        gfx.drawString(
            "x", (textPositiveXCoords.x - fontMetrics.stringWidth("x")).toInt(),
            (textPositiveXCoords.y + fontMetrics.ascent).toInt()
        )

        // Reset font size
        font = font?.deriveFont(fontSize)
        gfx.font = font

        // Draw ticks and labels for the Y-axis
        gfx.color = colorScheme.axisYColor
        for (i in Y_START..Y_END step Y_INCREMENTS) {
            if (i == 0) continue

            // Draw tick
            val coords = Vector2d(
                0 - TICK_LENGTH / 2, i.toDouble() + (TICK_THICKNESS / 2)
            ).toScreenCoord()

            gfx.fillRect(
                coords.x.toInt(),
                coords.y.toInt(),
                FieldUtil.scaleInchesToPixel(TICK_LENGTH).toInt(),
                FieldUtil.scaleInchesToPixel(TICK_THICKNESS).toInt()
            )

            // Draw number
            val yOffsetIn = 0.0
            var yOffsetPx = -fontMetrics.height.toDouble() / 2 // Bottom align

            if (i == Y_START) {
                yOffsetPx = 0.0
            } else if (i == Y_END) {
                yOffsetPx = -fontMetrics.height.toDouble()
            }

            val textCoords = Vector2d(
                Y_TEXT_X_OFFSET.scaleInToPixel(), i.toDouble() + yOffsetIn
            ).toScreenCoord()

            gfx.drawString(
                i.toString(), textCoords.x.toInt(), (textCoords.y - yOffsetPx / 2).toInt()
            )
        }

        // Increase font size for the Y labels
        font = font?.deriveFont((fontSize * 1.2).toFloat())
        gfx.font = font

        val textNegativeYCoords = Vector2d(
            Y_LABEL_X_OFFSET, (Y_START + Y_LABEL_Y_OFFSET / 2)
        ).toScreenCoord()
        val textPositiveYCoords = Vector2d(
            Y_LABEL_X_OFFSET, (Y_END - Y_LABEL_Y_OFFSET)
        ).toScreenCoord()

        gfx.drawString(
            "-y", (textNegativeYCoords.x - fontMetrics.stringWidth("-y")).toInt(),
            (textNegativeYCoords.y).toInt()
        )
        gfx.drawString(
            "y", (textPositiveYCoords.x - fontMetrics.stringWidth("y")).toInt(),
            (textPositiveYCoords.y).toInt()
        )
    }

    /**
     * Renders the axes on the given graphics context.
     *
     * @param gfx The graphics context.
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        val alpha = currentOpacity.toFloat()
        val resetComposite = gfx.composite
        val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

        gfx.composite = alphaComposite
        gfx.drawImage(baseBufferedImage, null, 0, 0)
        gfx.composite = resetComposite
    }

    /**
     * Sets the dimensions of the canvas.
     *
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) redraw()
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    /**
     * Switches the color scheme of the axes.
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
     * Sets the interval for the axes ticks.
     *
     * @param interval The interval between ticks.
     */
    fun setInterval(interval: Int) {
        X_INCREMENTS = interval
        Y_INCREMENTS = interval
    }

    /**
     * Handles mouse moved events to animate the axes opacity.
     *
     * @param me The mouse event.
     */
    override fun mouseMoved(me: MouseEvent?) {
        val hoverTargetPixels = HOVER_TARGET.scaleInToPixel()

        if ((me!!.x > canvasWidth / 2 - hoverTargetPixels / 2 && me.x < canvasWidth / 2 + hoverTargetPixels / 2) ||
            me.y > canvasHeight / 2 - hoverTargetPixels / 2 && me.y < canvasHeight / 2 + hoverTargetPixels / 2
        ) {
            animationController.anim(colorScheme.axisHoverOpacity, 200.0, Ease.EASE_OUT_CUBIC)
        } else {
            animationController.anim(colorScheme.axisNormalOpacity, 200.0, Ease.EASE_OUT_CUBIC)
        }
    }

    /** Unused mouse dragged event handler. */
    override fun mouseDragged(p0: MouseEvent?) {}
}