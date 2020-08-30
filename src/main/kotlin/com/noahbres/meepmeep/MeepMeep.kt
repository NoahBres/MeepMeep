package com.noahbres.meepmeep

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.*
import com.noahbres.meepmeep.core.ui.WindowFrame
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.core.util.LoopManager
import com.noahbres.meepmeep.roadrunner.AddTrajectoryCallback
import com.noahbres.meepmeep.roadrunner.AddTrajectorySequenceCallback
import com.noahbres.meepmeep.roadrunner.DriveTrainType
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import java.awt.*
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.imageio.ImageIO
import javax.swing.*
import javax.swing.border.EtchedBorder

open class MeepMeep(private val windowSize: Int) {
    companion object {
        // Default entities
        @JvmStatic
        lateinit var DEFAULT_BOT_ENTITY: BotEntity

        @JvmStatic
        lateinit var DEFAULT_AXES_ENTITY: AxesEntity

        @JvmStatic
        lateinit var DEFAULT_COMPASS_ENTITY: CompassEntity

        // Custom Fonts
        @JvmStatic
        lateinit var FONT_CMU_BOLD_LIGHT: Font

        @JvmStatic
        lateinit var FONT_CMU: Font

        @JvmStatic
        lateinit var FONT_CMU_BOLD: Font

        // Road Runner Entities
        @JvmStatic
        lateinit var DEFAULT_ROADRUNNER_BOT_ENTITY: RoadRunnerBotEntity
    }

    val windowFrame = WindowFrame("Meep Meep", windowSize)
    val canvas = windowFrame.canvas

    private var bg: Image? = null

    private val colorManager = ColorManager()

    private val entityList = mutableListOf<Entity>()
    private val requestedAddEntityList = mutableListOf<Entity>()
    private val requestedClearEntityList = mutableListOf<Entity>()

    private val zIndexManager = ZIndexManager();

    // TODO: Make custom dirty list that auto sorts
    // Returns true if entity list needs to be sorted
    private var entityListDirty = false

    private var bgAlpha = 1.0f

    private val render: () -> Unit = {
        val g = canvas.bufferStrat.drawGraphics as Graphics2D
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g.clearRect(0, 0, canvas.width, canvas.height)

        // render
        if (bg != null) {
            if(bgAlpha < 1.0f) {
                val resetComposite = g.composite
                val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha)
                g.composite = alphaComposite
                g.drawImage(bg, 0, 0, null)
                g.composite = resetComposite
            } else {
                g.drawImage(bg, 0, 0, null)
            }
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

    // Road Runner UI Elements
    val sliderPanel = JPanel()
    var middleButtonPanel = JPanel()

    private val standardCursorButton = JButton("test")
    private val pathSelectionButton = JButton("test 2")

    private val middleButtonList = mutableListOf(standardCursorButton, pathSelectionButton)

    init {
        // Core init
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        windowFrame.contentPane.background = colorManager.theme.UI_MAIN_BG
        windowFrame.canvasPanel.background = colorManager.theme.UI_MAIN_BG

        val classLoader = Thread.currentThread().contextClassLoader

        FONT_CMU_BOLD_LIGHT = Font.createFont(
                Font.TRUETYPE_FONT, classLoader.getResourceAsStream("font/cmunbi.ttf")
        ).deriveFont(20f)
        FONT_CMU = Font.createFont(Font.TRUETYPE_FONT, classLoader.getResourceAsStream("font/cmunrm.ttf"))
        FONT_CMU_BOLD = Font.createFont(Font.TRUETYPE_FONT, classLoader.getResourceAsStream("font/cmunbx.ttf"))

        FieldUtil.CANVAS_WIDTH = windowSize.toDouble()
        FieldUtil.CANVAS_HEIGHT = windowSize.toDouble()

        DEFAULT_BOT_ENTITY = BotEntity(this, 18.0, 18.0, Pose2d(), colorManager.theme, 0.8)
        DEFAULT_AXES_ENTITY = AxesEntity(this, 0.8, colorManager.theme, FONT_CMU_BOLD_LIGHT, 20f)
        DEFAULT_COMPASS_ENTITY = CompassEntity(
                this, colorManager.theme, 30.0, 30.0, Vector2d(-54.0, 54.0)
        )

        // Road Runner Init
        // Handle UI
        sliderPanel.layout = BoxLayout(sliderPanel, BoxLayout.Y_AXIS)

        middleButtonList.forEach {
            it.alignmentX = 0.5f
            it.background = colorManager.theme.UI_MAIN_BG
        }

        middleButtonPanel.background = colorManager.theme.UI_MAIN_BG
        middleButtonPanel.border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)
        middleButtonPanel.layout = BoxLayout(middleButtonPanel, BoxLayout.Y_AXIS)

        middleButtonPanel.add(Box.createVerticalGlue())
        middleButtonPanel.add(standardCursorButton)
        middleButtonPanel.add(pathSelectionButton)
        middleButtonPanel.add(Box.createVerticalGlue())

        windowFrame.canvasPanel.add(sliderPanel)
//        windowFrame.contentPane.add(middleButtonPanel)

        windowFrame.pack()

        // Handle entities
        DEFAULT_ROADRUNNER_BOT_ENTITY = RoadRunnerBotEntity(
                this,
                DriveConstraints(
                        30.0, 30.0, 0.0,
                        Math.toRadians(180.0), Math.toRadians(180.0), 0.0
                ),
                18.0, 18.0,
                15.0,
                Pose2d(), colorManager.theme, 0.8)

        // Entities

        zIndexManager.setTagHierarchy(
                "DEFAULT_BOT_ENTITY",
                "RR_BOT_ENTITY",
                "TURN_INDICATOR_ENTITY",
                "MARKER_INDICATOR_ENTITY",
                "TRAJECTORY_SEQUENCE_ENTITY",
                "COMPASS_ENTITY",
                "AXES_ENTITY",
        )

        //        addEntity(DEFAULT_BOT_ENTITY)
        addEntity(DEFAULT_AXES_ENTITY)
        addEntity(DEFAULT_COMPASS_ENTITY)

        addEntity(DEFAULT_ROADRUNNER_BOT_ENTITY)
    }

    open fun start(): MeepMeep {
        // Core Start
        if(bg == null) setBackground(Background.GRID_BLUE)
        windowFrame.isVisible = true

        // Default added entities are initialized before color schemes are set
        // Thus make sure to reset them
        entityList.forEach {
            if (it is ThemedEntity) it.switchScheme(colorManager.theme)
        }

        onCanvasResize()

        Thread(loopManager).start()

        // Road Runner Start
//        removeEntity(DEFAULT_BOT_ENTITY)
        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList) DEFAULT_ROADRUNNER_BOT_ENTITY.start()

        return this
    }

    //-------------Theme Settings-------------//
    fun setBackground(background: Background = Background.GRID_BLUE): MeepMeep {
        val classLoader = Thread.currentThread().contextClassLoader

        bg = when (background) {
            Background.GRID_BLUE -> {
                colorManager.isDarkMode = false
                ImageIO.read(classLoader.getResourceAsStream("background/grid-blue.jpg"))
            }
            Background.GRID_GREEN -> {
                colorManager.isDarkMode = false
                ImageIO.read(classLoader.getResourceAsStream("background/grid-green.jpg"))
            }
            Background.GRID_GRAY -> {
                colorManager.isDarkMode = false
                ImageIO.read(classLoader.getResourceAsStream("background/grid-gray.jpg"))
            }
            Background.FIELD_SKYSTONE -> {
                colorManager.isDarkMode = false
                ImageIO.read(classLoader.getResourceAsStream("background/field-skystone.png"))
            }
            Background.FIELD_SKYSTONE_GF -> {
                colorManager.isDarkMode = true
                ImageIO.read(classLoader.getResourceAsStream("background/field-skystone-gf.png"))
            }
            Background.FIELD_SKYSTONE_LIGHT -> {
                colorManager.isDarkMode = false
                ImageIO.read(classLoader.getResourceAsStream("background/field-skystone-light-fix.jpg"))
            }
            Background.FIELD_SKYSTONE_DARK -> {
                colorManager.isDarkMode = true
                ImageIO.read(classLoader.getResourceAsStream("background/field-skystone-dark-fix.jpg"))
            }
            Background.FIELD_SKYSTONE_STARWARS -> {
                colorManager.isDarkMode = true
                ImageIO.read(classLoader.getResourceAsStream("background/field-skystone-starwars.png"))
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
        // Core Refresh
        entityList.forEach {
            if (it is ThemedEntity) it.switchScheme(colorManager.theme)
        }

        windowFrame.contentPane.background = colorManager.theme.UI_MAIN_BG
        windowFrame.canvasPanel.background = colorManager.theme.UI_MAIN_BG

        // Road Runner Refresh
        middleButtonPanel.background = colorManager.theme.UI_MAIN_BG

        middleButtonList.forEach {
            it.background = colorManager.theme.UI_MAIN_BG
        }
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
        // Core
        if (DEFAULT_BOT_ENTITY in entityList) {
            DEFAULT_BOT_ENTITY.setDimensions(width, height)
        }

        // Road Runner
        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
            DEFAULT_ROADRUNNER_BOT_ENTITY.setDimensions(width, height)

        return this
    }

    //-------------Axes Settings-------------//
    fun setAxesInterval(interval: Int): MeepMeep {
        if (DEFAULT_AXES_ENTITY in entityList) DEFAULT_AXES_ENTITY.setInterval(interval)

        return this
    }

    //-------------Entity-------------//
    fun addEntity(entity: Entity): MeepMeep {
        zIndexManager.addEntity(entity)

        entityList.add(entity)
        entityListDirty = true

        if (entity is MouseListener) canvas.addMouseListener(entity)

        if (entity is MouseMotionListener) canvas.addMouseMotionListener(entity)

        return this
    }

    fun removeEntity(entity: Entity): MeepMeep {
        entityList.remove(entity)
        requestedAddEntityList.remove(entity)
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

    //-------------Misc-------------//
    fun setBackgroundAlpha(alpha: Float): MeepMeep {
        bgAlpha = alpha

        return this
    }

    //-------------Road Runner Settings-------------//
    fun setStartPose(pose: Pose2d): MeepMeep {
        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList) DEFAULT_ROADRUNNER_BOT_ENTITY.pose = pose

        return this
    }

    fun setConstraints(constraints: DriveConstraints): MeepMeep {
        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
            DEFAULT_ROADRUNNER_BOT_ENTITY.setConstraints(constraints)

        return this
    }

    fun setTrackWidth(trackWidth: Double): MeepMeep {
        if(DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
            DEFAULT_ROADRUNNER_BOT_ENTITY.setTrackWidth(trackWidth)

        return this
    }

    fun setDriveTrainType(driveTrainType: DriveTrainType): MeepMeep {
        if(DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
            DEFAULT_ROADRUNNER_BOT_ENTITY.setDriveTrainType(driveTrainType)

        return this
    }

    fun followTrajectorySequence(callback: AddTrajectorySequenceCallback): MeepMeep {
        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
            DEFAULT_ROADRUNNER_BOT_ENTITY.followTrajectorySequence(
                    callback.buildTrajectorySequence(DEFAULT_ROADRUNNER_BOT_ENTITY.drive)
            )

        return this
    }

    fun followTrajectorySequence(trajectorySequence: TrajectorySequence): MeepMeep {
        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
            DEFAULT_ROADRUNNER_BOT_ENTITY.followTrajectorySequence(trajectorySequence)

        return this
    }

//    fun followTrajectory(callback: AddTrajectoryCallback): MeepMeep {
//        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
//            DEFAULT_ROADRUNNER_BOT_ENTITY.followTrajectoryList(
//                    callback.buildTrajectory(DEFAULT_ROADRUNNER_BOT_ENTITY.drive)
//            )
//
//        return this
//    }

//    fun followTrajectory(trajectory: List<Trajectory>): MeepMeep {
//        if (DEFAULT_ROADRUNNER_BOT_ENTITY in entityList)
//            DEFAULT_ROADRUNNER_BOT_ENTITY.followTrajectoryList(trajectory)
//
//        return this
//    }

    enum class Background {
        GRID_BLUE,
        GRID_GREEN,
        GRID_GRAY,
        FIELD_SKYSTONE,
        FIELD_SKYSTONE_GF,
        FIELD_SKYSTONE_LIGHT,
        FIELD_SKYSTONE_DARK,
        FIELD_SKYSTONE_STARWARS
    }
}