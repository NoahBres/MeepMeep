package com.noahbres.meepmeep

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.*
import com.noahbres.meepmeep.core.ui.WindowFrame
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.core.util.LoopManager
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.io.File
import javax.imageio.ImageIO
import javax.swing.UIManager

open class MeepMeep(private val windowSize: Int) {
    companion object {
        @JvmStatic
        lateinit var DEFAULT_BOT_ENTITY: BotEntity

        @JvmStatic
        lateinit var DEFAULT_AXES_ENTITY: AxesEntity

        @JvmStatic
        lateinit var DEFAULT_COMPASS_ENTITY: CompassEntity

        @JvmStatic
        lateinit var FONT_CMU_BOLD_LIGHT: Font

        @JvmStatic
        lateinit var FONT_CMU: Font

        @JvmStatic
        lateinit var FONT_CMU_BOLD: Font
    }

    val windowFrame = WindowFrame("Meep Meep", windowSize)
    val canvas = windowFrame.canvas

    protected var bg: Image? = null

    protected val colorManager = ColorManager()

    protected val entityList = mutableListOf<Entity>()
    private val requestedAddEntityList = mutableListOf<Entity>()
    private val requestedClearEntityList = mutableListOf<Entity>()

    // TODO: Make custom dirty list that auto sorts
    // Returns true if entity list needs to be sorted
    private var entityListDirty = false

    private val render: () -> Unit = {
        val g = canvas.bufferStrat.drawGraphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.clearRect(0, 0, canvas.width, canvas.height)

        // render
        if (bg != null) {
//            val bgAlpha = 0.8f
//            val resetComposite = g.composite
//            val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha)
//
//            g.composite = alphaComposite
            g.drawImage(bg, 0, 0, null)
//            g.composite = resetComposite
        }

        entityList.forEach { it.render(g, canvas.width, canvas.height) }

        // Draw fps
        val fpsFont = Font("Sans", Font.BOLD, 20)
        g.font = fpsFont
        g.color = ColorManager.COLOR_PALETTE.GREEN_600
        g.drawString("${loopManager.fps} FPS", 10, 20)

        g.dispose()
        canvas.bufferStrat.show()
    }

    private val update: (deltaTime: Long) -> Unit = { deltaTime ->
        if (entityListDirty) {
            requestedClearEntityList.forEach {
                removeEntity(it)
            }

            requestedAddEntityList.forEach {
                addEntity(it)
            }

            entityList.sortBy { it.zIndex }
            entityListDirty = false
        }

        val originalSize = entityList.size
        for (i in 0 until originalSize) {
            entityList[i].update(deltaTime)
        }
    }

    private val loopManager = LoopManager(120, update, render)

    init {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        windowFrame.contentPane.background = colorManager.theme.UI_MAIN_BG
        windowFrame.canvasPanel.background = colorManager.theme.UI_MAIN_BG

        FONT_CMU_BOLD_LIGHT = Font.createFont(
                Font.TRUETYPE_FONT, File("res/font/cmunbi.ttf")
        ).deriveFont(20f)
        FONT_CMU = Font.createFont(Font.TRUETYPE_FONT, File("res/font/cmunrm.ttf"))
        FONT_CMU_BOLD = Font.createFont(Font.TRUETYPE_FONT, File("res/font/cmunbx.ttf"))

        FieldUtil.CANVAS_WIDTH = windowSize.toDouble()
        FieldUtil.CANVAS_HEIGHT = windowSize.toDouble()

        DEFAULT_BOT_ENTITY = BotEntity(this, 18.0, 18.0, Pose2d(), colorManager.theme, 0.8)
        DEFAULT_AXES_ENTITY = AxesEntity(this, 0.8, colorManager.theme, FONT_CMU_BOLD_LIGHT, 20f)
        DEFAULT_COMPASS_ENTITY = CompassEntity(
                this, colorManager.theme, 30.0, 30.0, Vector2d(-54.0, 54.0)
        )

        addEntity(DEFAULT_BOT_ENTITY)
        addEntity(DEFAULT_AXES_ENTITY)
        addEntity(DEFAULT_COMPASS_ENTITY)
    }

    open fun start(): MeepMeep {
        if(bg == null) setBackground(Background.GRID_BLUE)
        windowFrame.isVisible = true

        // Default added entities are initialized before color schemes are set
        // Thus make sure to reset them
        entityList.forEach {
            if (it is ThemedEntity) it.switchScheme(colorManager.theme)
        }

        onCanvasResize()

        Thread(loopManager).start()

        return this
    }

    //-------------Theme Settings-------------//
    fun setBackground(background: Background = Background.GRID_BLUE): MeepMeep {
        bg = when (background) {
            Background.GRID_BLUE -> {
                colorManager.isDarkMode = false
                ImageIO.read(File("res/background/grid-blue.jpg"))
            }
            Background.FIELD_SKYSTONE -> {
                colorManager.isDarkMode = false
                ImageIO.read(File("res/background/field-skystone.png"))
            }
            Background.FIELD_SKYSTONE_GF -> {
                colorManager.isDarkMode = true
                ImageIO.read(File("res/background/field-skystone-gf.png"))
            }
            Background.FIELD_SKYSTONE_LIGHT -> {
                colorManager.isDarkMode = false
                ImageIO.read(File("res/background/field-skystone-light-fix.jpg"))
            }
            Background.FIELD_SKYSTONE_DARK -> {
                colorManager.isDarkMode = true
                ImageIO.read(File("res/background/field-skystone-dark-fix.jpg"))
            }
            Background.FIELD_SKYSTONE_STARWARS -> {
                colorManager.isDarkMode = true
                ImageIO.read(File("res/background/field-skystone-starwars.png"))
            }
        }.getScaledInstance(windowSize, windowSize, Image.SCALE_SMOOTH)

        refreshTheme()

        return this
    }

    fun setBackground(image: Image): MeepMeep {
        bg = image.getScaledInstance(windowSize, windowSize, Image.SCALE_SMOOTH)

        return this
    }

    @JvmOverloads
    fun setTheme(schemeLight: ColorScheme, schemeDark: ColorScheme = schemeLight): MeepMeep {
        colorManager.setTheme(schemeLight, schemeDark)

        refreshTheme()

        return this
    }

    open fun refreshTheme() {
        entityList.forEach {
            if (it is ThemedEntity) it.switchScheme(colorManager.theme)
        }

        windowFrame.contentPane.background = colorManager.theme.UI_MAIN_BG
        windowFrame.canvasPanel.background = colorManager.theme.UI_MAIN_BG
    }

    fun setDarkMode(isDarkMode: Boolean): MeepMeep {
        colorManager.isDarkMode = isDarkMode

        return this
    }

    private fun onCanvasResize() {
        FieldUtil.CANVAS_WIDTH = windowSize.toDouble()
        FieldUtil.CANVAS_HEIGHT = windowSize.toDouble()

        entityList.forEach {
            it.setCanvasDimensions(FieldUtil.CANVAS_WIDTH, FieldUtil.CANVAS_HEIGHT)
        }
    }

    //-------------Robot Settings-------------//
    open fun setBotDimensions(width: Double, height: Double): MeepMeep {
        if (DEFAULT_BOT_ENTITY in entityList) {
            DEFAULT_BOT_ENTITY.setDimensions(width, height)
        }

        return this
    }

    //-------------Axes Settings-------------//
    fun setAxesInterval(interval: Int): MeepMeep {
        if (DEFAULT_AXES_ENTITY in entityList) DEFAULT_AXES_ENTITY.setInterval(interval)

        return this
    }

    //-------------Entity-------------//
    fun addEntity(entity: Entity): MeepMeep {
        entityList.add(entity)
        entityListDirty = true

        if (entity is MouseListener) canvas.addMouseListener(entity)

        if (entity is MouseMotionListener) canvas.addMouseMotionListener(entity)

        return this
    }

    fun removeEntity(entity: Entity): MeepMeep {
        entityList.remove(entity)
        entityListDirty = true


        if (entity is MouseListener) canvas.removeMouseListener(entity)

        if (entity is MouseMotionListener) canvas.removeMouseMotionListener(entity)

        return this
    }

    fun requestToAddEntity(entity: Entity): MeepMeep {
        requestedAddEntityList.add(entity)
        entityListDirty = true

        return this
    }


    fun requestToClearEntity(entity: Entity): MeepMeep {
        requestedClearEntityList.add(entity)
        entityListDirty = true

        return this
    }

    enum class Background {
        GRID_BLUE,
        FIELD_SKYSTONE,
        FIELD_SKYSTONE_GF,
        FIELD_SKYSTONE_LIGHT,
        FIELD_SKYSTONE_DARK,
        FIELD_SKYSTONE_STARWARS
    }
}