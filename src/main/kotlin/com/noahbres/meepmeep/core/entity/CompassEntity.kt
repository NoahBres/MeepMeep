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
import java.io.File
import javax.imageio.ImageIO

class CompassEntity(
        override val meepMeep: MeepMeep,
        private var colorScheme: ColorScheme,

        private val width: Double, private val height: Double,
        val pos: Vector2d
) : ThemedEntity, MouseMotionListener {
    override val tag = "COMPASS_ENTITY"

    override var zIndex: Int = 0

    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    private lateinit var image: BufferedImage

    private val NORMAL_OPACITY = 0.5
    private val HOVER_OPACITY = 1.0

    private var currentOpacity = NORMAL_OPACITY
    private val animationController = AnimationController(currentOpacity).clip(0.0, 1.0)

    private val bgLight: Image by lazy {
        val classLoader = Thread.currentThread().contextClassLoader
        ImageIO.read(classLoader.getResourceAsStream("misc/compass-rose-black-text.png"))
    }

    private val bgDark: Image by lazy {
        val classLoader = Thread.currentThread().contextClassLoader
        ImageIO.read(classLoader.getResourceAsStream("misc/compass-rose-white-text.png"))
    }

    init {
        redraw()
    }

    private fun redraw() {
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        image = config.createCompatibleImage(
                width.scaleInToPixel().toInt(),
                height.scaleInToPixel().toInt(), Transparency.TRANSLUCENT
        )
        val gfx = image.createGraphics()

        val scaled = (if (colorScheme.isDark) bgDark else bgLight).getScaledInstance(
                width.scaleInToPixel().toInt(),
                height.scaleInToPixel().toInt(), Image.SCALE_SMOOTH
        )
        gfx.drawImage(scaled, 0, 0, null)
    }

    override fun update(deltaTime: Long) {
        currentOpacity = animationController.value
        animationController.update()
    }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        val alpha = currentOpacity.toFloat()
        val resetComposite = gfx.composite
        val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

        gfx.composite = alphaComposite
        gfx.drawImage(
                image, null,
                (pos.toScreenCoord().x - width.scaleInToPixel() / 2).toInt(),
                (pos.toScreenCoord().y - height.scaleInToPixel() / 2).toInt()
        )
        gfx.composite = resetComposite
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) redraw()
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        if (this.colorScheme != scheme) {
            colorScheme = scheme
            redraw()
        }
    }

    override fun mouseMoved(me: MouseEvent?) {
        if (me!!.x > pos.toScreenCoord().x - width.scaleInToPixel() / 2
                && me.x < pos.toScreenCoord().x + width.scaleInToPixel() / 2
                && me.y > pos.toScreenCoord().y - height.scaleInToPixel() / 2
                && me.y < pos.toScreenCoord().y + height.scaleInToPixel() / 2) {
            animationController.anim(colorScheme.AXIS_HOVER_OPACITY, 200.0, Ease.EASE_OUT_CUBIC)
        } else {
            animationController.anim(colorScheme.AXIS_NORMAL_OPACITY, 200.0, Ease.EASE_OUT_CUBIC)
        }
    }

    override fun mouseDragged(me: MouseEvent?) {}
}
