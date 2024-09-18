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
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.Image
import java.awt.Transparency
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

/**
 * Represents a compass entity in the MeepMeep simulation.
 *
 * @property meepMeep The MeepMeep instance.
 * @property colorScheme The color scheme of the compass.
 * @property width The width of the compass.
 * @property height The height of the compass.
 * @property pos The position of the compass.
 */
class CompassEntity(
    override val meepMeep: MeepMeep,
    private var colorScheme: ColorScheme,
    private val width: Double,
    private val height: Double,
    private val pos: Vector2d
) : ThemedEntity, MouseMotionListener {
    /** Tag for the compass entity. */
    override val tag = "COMPASS_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex: Int = 0

    /** Canvas width. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** Canvas height. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    /** Buffered image for rendering the compass. */
    private lateinit var image: BufferedImage

    /** Current opacity of the compass. */
    private var currentOpacity = colorScheme.axisNormalOpacity

    /** Animation controller for the compass opacity. */
    private val animationController = AnimationController(currentOpacity).clip(0.0, 1.0)

    /** Light background image for the compass. */
    private var bgLight: Image

    /** Dark background image for the compass. */
    private var bgDark: Image

    /** Initializes the compass entity and draws the compass. */
    init {
        val classLoader = Thread.currentThread().contextClassLoader

        // Initialize the light background image
        bgLight = ImageIO.read(classLoader.getResourceAsStream("misc/compass-rose-black-text.png"))

        // Initialize the dark background image
        bgDark = ImageIO.read(classLoader.getResourceAsStream("misc/compass-rose-white-text.png"))
        redraw()
    }

    /**
     * Updates the axes entity.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    override fun update(deltaTime: Long) {
        currentOpacity = animationController.value
        animationController.update()
    }

    /** Draws the compass on the buffered image. */
    private fun redraw() {
        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the compass
        image = config.createCompatibleImage(
            width.scaleInToPixel().toInt(),
            height.scaleInToPixel().toInt(),
            Transparency.TRANSLUCENT
        )
        val gfx = image.createGraphics()

        // Scale the compass image to the correct size based on the color scheme
        val scaled = (if (colorScheme.isDark) bgDark else bgLight).getScaledInstance(
            width.scaleInToPixel().toInt(), height.scaleInToPixel().toInt(), Image.SCALE_SMOOTH
        )

        // Draw the scaled compass image
        gfx.drawImage(scaled, 0, 0, null)
    }

    /**
     * Renders the compass on the given graphics context.
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
        gfx.drawImage(
            image,
            null,
            (pos.toScreenCoord().x - width.scaleInToPixel() / 2).toInt(),
            (pos.toScreenCoord().y - height.scaleInToPixel() / 2).toInt()
        )
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
     * Switches the color scheme of the compass.
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
     * Handles mouse moved events to animate the compass opacity.
     *
     * @param me The mouse event.
     */
    override fun mouseMoved(me: MouseEvent?) {
        if (me!!.x > pos.toScreenCoord().x - width.scaleInToPixel() / 2 && me.x < pos.toScreenCoord().x + width.scaleInToPixel() / 2 && me.y > pos.toScreenCoord().y - height.scaleInToPixel() / 2 && me.y < pos.toScreenCoord().y + height.scaleInToPixel() / 2) {
            animationController.anim(colorScheme.axisHoverOpacity, 200.0, Ease.EASE_OUT_CUBIC)
        } else {
            animationController.anim(colorScheme.axisNormalOpacity, 200.0, Ease.EASE_OUT_CUBIC)
        }
    }

    /** Unused mouse dragged event handler. */
    override fun mouseDragged(me: MouseEvent?) {}
}
