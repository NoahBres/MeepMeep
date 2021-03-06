package com.noahbres.meepmeep.core.entity

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.core.anim.AnimationController
import com.noahbres.meepmeep.core.anim.Ease
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.scaleInToPixel
import com.noahbres.meepmeep.core.toScreenCoord
import java.awt.*
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage

class AxesEntity
@JvmOverloads constructor(
        override val meepMeep: MeepMeep,
        private val axesThickness: Double,
        private var colorScheme: ColorScheme,

        private var font: Font? = null,
        private var fontSize: Float = 20f
) : ThemedEntity, MouseMotionListener {
    override val tag = "AXES_ENTITY"

    override var zIndex = 0

    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    private lateinit var baseBufferedImage: BufferedImage

    private val NUMBER_X_AXIS_X_OFFSET = 0
    private val NUMBER_X_AXIS_Y_OFFSET = 0

    private val TICK_LENGTH = 3.0
    private val TICK_THICKNESS = 0.45

    private var X_INCREMENTS = 18
    private var Y_INCREMENTS = 18

    private val X_START = -FieldUtil.FIELD_WIDTH / 2
    private val X_END = FieldUtil.FIELD_WIDTH / 2
    private val X_TEXT_Y_OFFSET = 0.3
    private val X_TEXT_POSITIVE_X_OFFSET = -0.6
    private val X_TEXT_NEGATIVE_X_OFFSET = 0.7

    private val Y_START = -FieldUtil.FIELD_HEIGHT / 2
    private val Y_END = FieldUtil.FIELD_HEIGHT / 2
    private val Y_TEXT_X_OFFSET = 0.6

    private val X_LABEL_X_OFFSET = 1.5
    private val X_LABEL_Y_OFFSET = -1.0

    private val Y_LABEL_X_OFFSET = -4.0
    private val Y_LABEL_Y_OFFSET = 3.0

    private val HOVER_TARGET = 20.0

    private var currentOpacity = colorScheme.AXIS_NORMAL_OPACITY
    private val animationController = AnimationController(currentOpacity).clip(0.0, 1.0)

    override fun update(deltaTime: Long) {
        currentOpacity = animationController.value
        // Todo fix issue that necessitates the controller needing a clip
        // Starts at like 30 or 28 for some reason
//        println(currentOpacity)
        animationController.update()
    }

    init {
        redrawAxes()
    }

    private fun redrawAxes() {
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        baseBufferedImage = config.createCompatibleImage(
                canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )
        val gfx = baseBufferedImage.createGraphics()

        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val pixelThickness = axesThickness.scaleInToPixel()

        gfx.color = colorScheme.AXIS_X_COLOR
        gfx.fillRect(
                0, (canvasHeight / 2.0 - pixelThickness / 2).toInt(), canvasWidth.toInt(),
                pixelThickness.toInt()
        )

        gfx.color = colorScheme.AXIS_Y_COLOR
        gfx.fillRect(
                (canvasWidth / 2.0 - pixelThickness / 2).toInt(), 0, pixelThickness.toInt(),
                canvasHeight.toInt()
        )

        if (font != null) {
            gfx.font = font
        }

        val fontMetrics = gfx.fontMetrics

        // Draw x axis
        gfx.color = colorScheme.AXIS_X_COLOR
        for (i in X_START..X_END step X_INCREMENTS) {
            if (i == 0) continue

            // axis
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

        // Increase font size for the x labels
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

        // Draw y ticks
        gfx.color = colorScheme.AXIS_Y_COLOR
        for (i in Y_START..Y_END step Y_INCREMENTS) {
            if (i == 0) continue

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
            var yOffsetIn = 0.0
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

        // Increase font size for the x labels
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

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        val alpha = currentOpacity.toFloat()
        val resetComposite = gfx.composite
        val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

        gfx.composite = alphaComposite
        gfx.drawImage(baseBufferedImage, null, 0, 0)
        gfx.composite = resetComposite
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) redrawAxes()
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        if (this.colorScheme != scheme) {
            colorScheme = scheme
            redrawAxes()
        }
    }

    fun setInterval(interval: Int) {
        X_INCREMENTS = interval
        Y_INCREMENTS = interval
    }

    override fun mouseMoved(e: MouseEvent?) {
        val HOVER_TARGET_PIXELS = HOVER_TARGET.scaleInToPixel()

        if ((e!!.x > canvasWidth / 2 - HOVER_TARGET_PIXELS / 2 && e.x < canvasWidth / 2 + HOVER_TARGET_PIXELS / 2) ||
                e.y > canvasHeight / 2 - HOVER_TARGET_PIXELS / 2 && e.y < canvasHeight / 2 + HOVER_TARGET_PIXELS / 2) {
//            currentOpacity = hoverOpacity
            animationController.anim(colorScheme.AXIS_HOVER_OPACITY, 200.0, Ease.EASE_OUT_CUBIC)
        } else {
//            currentOpacity = normalOpacity
            animationController.anim(colorScheme.AXIS_NORMAL_OPACITY, 200.0, Ease.EASE_OUT_CUBIC)
        }
    }

    override fun mouseDragged(p0: MouseEvent?) {}
}