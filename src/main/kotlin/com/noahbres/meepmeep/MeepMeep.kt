package com.noahbres.meepmeep

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.AxesEntity
import com.noahbres.meepmeep.core.entity.CompassEntity
import com.noahbres.meepmeep.core.entity.Entity
import com.noahbres.meepmeep.core.entity.EntityEventListener
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.entity.ZIndexManager
import com.noahbres.meepmeep.core.ui.WindowFrame
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.core.util.LoopManager
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import com.noahbres.meepmeep.roadrunner.ui.TrajectoryProgressSliderMaster
import java.awt.AlphaComposite
import java.awt.Font
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.imageio.ImageIO
import javax.swing.BorderFactory
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.UIManager
import javax.swing.border.EtchedBorder

/**
 * The [MeepMeep] class is the main entry point for the Meep Meep
 * application. It handles the initialization and management of
 * the application window, rendering, and entity management.
 *
 * @constructor Creates a [MeepMeep] instance with specified window
 *    dimensions and optional fps.
 * @property windowX The width of the application window.
 * @property windowY The height of the application window.
 * @property fps The frames per second for the application loop.
 * @see [WindowFrame]
 * @see [ColorManager]
 * @see [Entity]
 * @see [AxesEntity]
 * @see [CompassEntity]
 * @see [LoopManager]
 * @see [TrajectoryProgressSliderMaster]
 * @see [FieldUtil]
 */
@Suppress("unused", "MemberVisibilityCanBePrivate", "SpellCheckingInspection")
class MeepMeep @JvmOverloads constructor(
    private val windowX: Int, private val windowY: Int, private val fps: Int = 60
) {
    /**
     * Companion object to hold default entities and fonts used in the MeepMeep
     * application.
     */
    companion object {
        /** Default axes entity used in the application. */
        lateinit var DEFAULT_AXES_ENTITY: AxesEntity

        /** Default compass entity used in the application. */
        lateinit var DEFAULT_COMPASS_ENTITY: CompassEntity

        /** Roboto regular font. */
        lateinit var FONT_ROBOTO_REGULAR: Font

        /** Roboto bold font. */
        lateinit var FONT_ROBOTO_BOLD: Font

        /** Roboto bold italic font. */
        lateinit var FONT_ROBOTO_BOLD_ITALIC: Font
    }

    /** The main application window frame. */
    val windowFrame = WindowFrame("MeepMeep", windowX, windowY)

    /** The canvas where all rendering occurs. */
    val canvas = windowFrame.canvas

    /** Manages the color scheme of the application. */
    val colorManager = ColorManager()

    /** The background image of the application. */
    private var bg: Image? = null

    /** The alpha transparency level of the background image. */
    private var bgAlpha = 1.0f

    /** List of all entities currently in the application. */
    private val entityList = mutableListOf<Entity>()

    /** List of entities requested to be added to the application. */
    private val requestedAddEntityList = mutableListOf<Entity>()

    /** List of entities requested to be removed from the application. */
    private val requestedRemoveEntityList = mutableListOf<Entity>()

    /** Manages the z-index of entities for rendering order. */
    private val zIndexManager = ZIndexManager()

    /** Panel containing the middle buttons. */
    private var middleButtonPanel = JPanel()

    /** Button for standard cursor mode. */
    private val standardCursorButton = JButton("test")

    /** Button for path selection mode. */
    private val pathSelectionButton = JButton("test 2")

    /** List of buttons in the middle panel. */
    private val middleButtonList = mutableListOf(standardCursorButton, pathSelectionButton)

    /** The x-coordinate for displaying mouse coordinates. */
    private var mouseCoordinateDisplayX = 10

    /** The y-coordinate for displaying mouse coordinates. */
    private var mouseCoordinateDisplayY = canvas.height - 8

    /** X-coordinate of the mouse on the canvas. */
    private var canvasMouseX = 0

    /** Y-coordinate of the mouse on the canvas. */
    private var canvasMouseY = 0

    /** The width of the canvas, publicly accessible. */
    val canvasWidth = canvas.width

    /** The height of the canvas, publicly accessible. */
    val canvasHeight = canvas.height

    /** Control visibility of FPS display. */
    private var showFPS: Boolean = false

    /**
     * Initializes the [TrajectoryProgressSliderMaster] panel.
     *
     * This lazy-initialized property sets up the progress slider elements for
     * the application. The [TrajectoryProgressSliderMaster] is responsible
     * for managing and displaying the progress of trajectories for the bots.
     *
     * @see [TrajectoryProgressSliderMaster]
     * @see [FieldUtil.CANVAS_WIDTH]
     */
    private val progressSliderMasterPanel: TrajectoryProgressSliderMaster by lazy {
        // Create a new instance of TrajectoryProgressSliderMaster
        TrajectoryProgressSliderMaster(
            this, FieldUtil.CANVAS_WIDTH.toInt(), 20
        )
    }

    // TODO: Make custom dirty list that auto sorts
    // Returns true if entity list needs to be sorted
    private var entityListDirty = false

    init {
        // Create class loader to load resources
        val classLoader = Thread.currentThread().contextClassLoader

        // Load Roboto Regular font from file
        FONT_ROBOTO_REGULAR = Font.createFont(
            Font.TRUETYPE_FONT, classLoader.getResourceAsStream("font/Roboto-Regular.ttf")
        )

        // Load Roboto Bold font from file
        FONT_ROBOTO_BOLD = Font.createFont(
            Font.TRUETYPE_FONT, classLoader.getResourceAsStream("font/Roboto-Bold.ttf")
        )

        // Load Roboto Bold Italic font from file
        FONT_ROBOTO_BOLD_ITALIC = Font.createFont(
            Font.TRUETYPE_FONT, classLoader.getResourceAsStream("font/Roboto-BoldItalic.ttf")
        )

        // Set the look and feel of the UI to the system's default
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

        // Set the background color of the main content pane
        windowFrame.contentPane.background = colorManager.theme.uiMainBG

        // Set the background color of the canvas panel
        windowFrame.canvasPanel.background = colorManager.theme.uiMainBG

        // Set canvas width and height in FieldUtil
        FieldUtil.CANVAS_WIDTH = windowX.toDouble()
        FieldUtil.CANVAS_HEIGHT = windowY.toDouble()

        // Initialize axes entity
        DEFAULT_AXES_ENTITY = AxesEntity(
            this, 0.8, colorManager.theme, FONT_ROBOTO_BOLD_ITALIC, 20f
        )

        // Initialize compass entity
        DEFAULT_COMPASS_ENTITY = CompassEntity(
            this, colorManager.theme, 30.0, 30.0, Vector2d(-54.0, 54.0)
        )

        // Set alignment and background color for each button in the middle button list
        middleButtonList.forEach {
            it.alignmentX = 0.5f
            it.background = colorManager.theme.uiMainBG
        }

        // Set background color and border for the middle button panel
        middleButtonPanel.background = colorManager.theme.uiMainBG
        middleButtonPanel.border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)

        // Set layout for the middle button panel
        middleButtonPanel.layout = BoxLayout(middleButtonPanel, BoxLayout.Y_AXIS)

        // Add vertical glue and buttons to the middle button panel
        middleButtonPanel.add(Box.createVerticalGlue())
        middleButtonPanel.add(standardCursorButton)
        middleButtonPanel.add(pathSelectionButton)
        middleButtonPanel.add(Box.createVerticalGlue())

        // Add the progress slider panel to the canvas panel
        windowFrame.canvasPanel.add(progressSliderMasterPanel) //        windowFrame.contentPane.add(middleButtonPanel)

        // Pack the window frame to fit the preferred sizes of its components
        windowFrame.pack()

        // Add mouse motion listener to the canvas
        canvas.addMouseMotionListener(object: MouseMotionListener {
            override fun mouseDragged(p0: MouseEvent?) {}

            override fun mouseMoved(e: MouseEvent) {
                canvasMouseX = e.x
                canvasMouseY = e.y
            }
        })

        // Add key listener to the canvas
        canvas.addKeyListener(object: KeyListener {
            /**
             * Invoked when a key has been typed. This event occurs when a key press is
             * followed by a key release.
             *
             * @param p0 The KeyEvent that triggered this method.
             */
            override fun keyTyped(p0: KeyEvent?) {}

            /**
             * Invoked when a key has been pressed. This event occurs when a key press
             * is detected.
             *
             * @param e The KeyEvent that triggered this method.
             */
            override fun keyPressed(e: KeyEvent) {
                // Check if the 'C' or 'COPY' (Often `Ctrl/CMD + C`) key is pressed
                if (e.keyCode == KeyEvent.VK_C || e.keyCode == KeyEvent.VK_COPY) {
                    // Convert mouse coordinates from screen to field coordinates
                    val mouseToFieldCoords = FieldUtil.screenCoordsToFieldCoords(
                        Vector2d(
                            canvasMouseX.toDouble(), canvasMouseY.toDouble()
                        )
                    )

                    // Format the coordinates as a string
                    val stringSelection = StringSelection(
                        "%.1f, %.1f".format(
                            mouseToFieldCoords.x,
                            mouseToFieldCoords.y,
                        )
                    )

                    // Get the system clipboard and set the contents to the formatted coordinates
                    val clipboard = Toolkit.getDefaultToolkit().systemClipboard
                    clipboard.setContents(stringSelection, null)
                }
            }

            /**
             * Invoked when a key has been released. This event occurs when a key
             * release is detected.
             *
             * @param p0 The KeyEvent that triggered this method.
             */
            override fun keyReleased(p0: KeyEvent?) {}
        })

        // Set the z-index hierarchy for entities
        zIndexManager.setTagHierarchy(
            "RR_BOT_ENTITY",
            "TURN_INDICATOR_ENTITY",
            "MARKER_INDICATOR_ENTITY",
            "TRAJECTORY_SEQUENCE_ENTITY",
            "COMPASS_ENTITY",
            "AXES_ENTITY",
        )

        // Add default entities to the entity list
        addEntity(DEFAULT_AXES_ENTITY)
        addEntity(DEFAULT_COMPASS_ENTITY)
    }

    /**
     * Renders the current state of the application onto the canvas.
     *
     * This function handles the rendering of the background image, all
     * entities, the FPS counter, and the mouse coordinates. It uses
     * [Graphics2D] for rendering and applies anti-aliasing for smoother
     * visuals.
     *
     * @see [WindowFrame]
     * @see [ColorManager]
     * @see [FieldUtil]
     * @see [LoopManager]
     */
    private val render: () -> Unit = {
        // Get the graphics context from the canvas buffer strategy
        val g = canvas.bufferStrat.drawGraphics as Graphics2D

        // Enable anti-aliasing for smoother visuals
        g.apply {
            setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            clearRect(0, 0, canvas.width, canvas.height)
        }

        // Render the background image if it exists
        bg?.let {
            if (bgAlpha < 1.0f) {
                // Apply alpha transparency to the background image
                val resetComposite = g.composite
                val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, bgAlpha)

                g.composite = alphaComposite
                g.drawImage(it, 0, 0, null)
                g.composite = resetComposite
            } else {
                g.drawImage(it, 0, 0, null)
            }
        }

        // Render all entities in the entity list
        entityList.forEach { it.render(g, canvas.width, canvas.height) }

        if (showFPS) {
            g.font = FONT_ROBOTO_BOLD_ITALIC.deriveFont(20f)
            g.color = ColorManager.COLOR_PALETTE.green600
            g.drawString("%.1f FPS".format(loopManager.fps), 10, 20)
        }

        // Convert mouse coordinates from screen to field coordinates
        val mouseToFieldCoords = FieldUtil.screenCoordsToFieldCoords(
            Vector2d(canvasMouseX.toDouble(), canvasMouseY.toDouble())
        )

        // Draw the mouse coordinates
        g.font = FONT_ROBOTO_BOLD.deriveFont(14f)
        g.color =
                if (colorManager.isDarkMode) ColorManager.COLOR_PALETTE.gray100 else ColorManager.COLOR_PALETTE.gray800
        g.drawString(
            "(%.1f, %.1f)".format(mouseToFieldCoords.x, mouseToFieldCoords.y),
            mouseCoordinateDisplayX,
            mouseCoordinateDisplayY
        )

        // Dispose of the graphics context and show the buffer
        g.dispose()
        canvas.bufferStrat.show()
    }

    // Secondary constructor that initializes the window size with equal width and height
    constructor(windowSize: Int): this(windowSize, windowSize)

    /**
     * Updates the state of the application.
     *
     * This function processes the addition and removal of entities, sorts
     * the entity list by their z-index, and updates each entity based on the
     * provided delta time.
     *
     * @param deltaTime The time in milliseconds since the last update.
     *    milliseconds.
     * @see [Entity]
     * @see [ZIndexManager]
     */
    private val update: (deltaTime: Long) -> Unit = { deltaTime ->
        // Check if the entity list needs to be updated
        if (entityListDirty) {
            // Remove entities that are requested to be removed
            entityList.removeAll(requestedRemoveEntityList)
            requestedRemoveEntityList.clear()

            // Add entities that are requested to be added
            entityList.addAll(requestedAddEntityList)
            requestedAddEntityList.clear()

            // Sort the entity list by their z-index
            entityList.sortBy { it.zIndex }
            entityListDirty = false
        }

        // Update each entity in the entity list
        entityList.forEach { it.update(deltaTime) }
    }

    /**
     * Manages the application loop with the specified fps and update and
     * render functions.
     */
    private val loopManager = LoopManager(fps, update, render)

    /**
     * Starts the MeepMeep application.
     *
     * This method initializes the background, sets the window visibility,
     * resets the theme for all entities, resizes the canvas, and starts the
     * application loop.
     *
     * @return The [MeepMeep] instance.
     */
    fun start(): MeepMeep {
        // Set the default background if none is set
        if (bg == null) setBackground(Background.GRID_BLUE)
        windowFrame.isVisible = true

        // Default added entities are initialized before color schemes are set
        // Thus make sure to reset them
        entityList.forEach {
            if (it is ThemedEntity) it.switchScheme(colorManager.theme)
            if (it is RoadRunnerBotEntity) it.start()
        }

        // Adjust the canvas size
        onCanvasResize()

        // Start the application loop
        loopManager.start()

        return this
    }

    fun setBackground(background: Background): MeepMeep {
        val classLoader = Thread.currentThread().contextClassLoader

        val backgroundMap = Background.values().associateWith { bg ->
            val path = when (bg) {
                Background.GRID_BLUE -> "background/misc/field-grid-blue.jpg"
                Background.GRID_GREEN -> "background/misc/field-grid-green.jpg"
                Background.GRID_GRAY -> "background/misc/field-grid-gray.jpg"
                Background.FIELD_SKYSTONE_OFFICIAL -> "background/season-2019-skystone/field-2019-skystone-official.png"
                Background.FIELD_SKYSTONE_GF_DARK -> "background/season-2019-skystone/field-2019-skystone-gf-dark.png"
                Background.FIELD_SKYSTONE_INNOV8RZ_LIGHT -> "background/season-2019-skystone/field-2019-skystone-innov8rz-light.jpg"
                Background.FIELD_SKYSTONE_INNOV8RZ_DARK -> "background/season-2019-skystone/field-2019-skystone-innov8rz-dark.jpg"
                Background.FIELD_SKYSTONE_STARWARS_DARK -> "background/season-2019-skystone/field-2019-skystone-starwars.png"
                Background.FIELD_ULTIMATEGOAL_INNOV8RZ_DARK -> "background/season-2020-ultimategoal/field-2020-innov8rz-dark.jpg"
                Background.FIELD_FREIGHTFRENZY_OFFICIAL -> "background/season-2021-freightfrenzy/field-2021-official.png"
                Background.FIELD_FREIGHTFRENZY_ADI_DARK -> "background/season-2021-freightfrenzy/field-2021-adi-dark.png"
                Background.FIELD_POWERPLAY_OFFICIAL -> "background/season-2022-powerplay/field-2022-official.png"
                Background.FIELD_POWERPLAY_KAI_DARK -> "background/season-2022-powerplay/field-2022-kai-dark.png"
                Background.FIELD_POWERPLAY_KAI_LIGHT -> "background/season-2022-powerplay/field-2022-kai-light.png"
                Background.FIELD_CENTERSTAGE_OFFICIAL -> "background/season-2023-centerstage/field-2023-official.png"
                Background.FIELD_CENTERSTAGE_JUICE_DARK -> "background/season-2023-centerstage/field-2023-juice-dark.png"
                Background.FIELD_CENTERSTAGE_JUICE_LIGHT -> "background/season-2023-centerstage/field-2023-juice-light.png"
                Background.FIELD_INTOTHEDEEP_OFFICIAL -> "background/season-2024-intothedeep/field-2024-official.png"
                Background.FIELD_INTOTHEDEEP_JUICE_DARK -> "background/season-2024-intothedeep/field-2024-juice-dark.png"
                Background.FIELD_INTOTHEDEEP_JUICE_LIGHT -> "background/season-2024-intothedeep/field-2024-juice-light.png"
                Background.FIELD_INTOTHEDEEP_JUICE_GREYSCALE -> "background/season-2024-intothedeep/field-2024-juice-greyscale.png"
                Background.FIELD_INTOTHEDEEP_JUICE_BLACK -> "background/season-2024-intothedeep/field-2024-juice-black.png"
            }

            val isDarkMode = path.contains("dark", ignoreCase = true)
            Pair(path, isDarkMode)
        }

        // Get the path and dark mode boolean from the background map
        val (path, isDarkMode) = backgroundMap[background]!!
        colorManager.isDarkMode = isDarkMode
        bg = ImageIO.read(classLoader.getResourceAsStream(path))
            .getScaledInstance(windowX, windowY, Image.SCALE_SMOOTH)

        // Refresh the theme for all entities
        refreshTheme()

        return this
    }

    /**
     * Sets the background image for the MeepMeep application.
     *
     * This method scales the provided [Image] to fit the dimensions of the
     * application window and sets it as the background image. The background
     * image is rendered during the application's render cycle.
     *
     * @param image The [Image] to be set as the background.
     * @return The [MeepMeep] instance for method chaining.
     */
    fun setBackground(image: Image): MeepMeep {
        // Scale the provided image to fit the window dimensions
        bg = image.getScaledInstance(windowX, windowY, Image.SCALE_SMOOTH)

        // Return the current instance for method chaining
        return this
    }

    /**
     * Sets the display position for the mouse coordinates on the canvas.
     *
     * This method updates the x and y coordinates where the mouse coordinates
     * will be displayed on the canvas. The coordinates are used in the
     * [MeepMeep.render] function to draw the mouse position.
     *
     * @param x The x-coordinate for displaying the mouse coordinates.
     * @param y The y-coordinate for displaying the mouse coordinates.
     */
    fun setMouseCoordinateDisplayPosition(x: Int, y: Int) {
        // Update the x-coordinate for the mouse coordinate display
        mouseCoordinateDisplayX = x

        // Update the y-coordinate for the mouse coordinate display
        mouseCoordinateDisplayY = y
    }

    /**
     * Sets the visibility of the FPS display.
     *
     * @param showFPS A boolean indicating whether the FPS display should be
     *    shown.
     * @return The [MeepMeep] instance for method chaining.
     */
    fun setShowFPS(showFPS: Boolean): MeepMeep {
        // Update the showFPS property
        this.showFPS = showFPS

        return this
    }

    /**
     * Sets the theme for the MeepMeep application.
     *
     * This method updates the color scheme of the application by setting the
     * light and dark themes using the [ColorManager]. It then refreshes the
     * theme for all entities and UI components.
     *
     * @param schemeLight The [ColorScheme] to be used for the light theme.
     * @param schemeDark The [ColorScheme] to be used for the dark theme.
     *    Defaults to the light theme if not provided.
     * @return The [MeepMeep] instance for method chaining.
     * @see [ColorManager]
     * @see [refreshTheme]
     */
    @JvmOverloads
    fun setTheme(schemeLight: ColorScheme, schemeDark: ColorScheme = schemeLight): MeepMeep {
        // Set the light and dark themes in the ColorManager
        colorManager.setTheme(schemeLight, schemeDark)

        // Refresh the theme for all entities and UI components
        refreshTheme()

        // Return the current instance for method chaining
        return this
    }

    /**
     * Refreshes the theme for the MeepMeep application.
     *
     * This method updates the color scheme of the application by switching the
     * theme for all entities that implement [ThemedEntity]. It also updates
     * the background colors of the main content pane, canvas panel, middle
     * button panel, and each button in the middle button list.
     *
     * @see [ColorManager]
     * @see [ThemedEntity]
     */
    private fun refreshTheme() {
        // Core Refresh: Update the theme for all entities that implement ThemedEntity
        entityList.forEach {
            if (it is ThemedEntity) it.switchScheme(colorManager.theme)
        }

        // Update the background color of the main content pane and canvas panel
        windowFrame.contentPane.background = colorManager.theme.uiMainBG
        windowFrame.canvasPanel.background = colorManager.theme.uiMainBG

        // Road Runner Refresh: Update the background color of the middle button panel
        middleButtonPanel.background = colorManager.theme.uiMainBG

        // Update the background color of each button in the middle button list
        middleButtonList.forEach {
            it.background = colorManager.theme.uiMainBG
        }
    }

    /**
     * Sets the dark mode for the MeepMeep application.
     *
     * This method updates the [ColorManager.isDarkMode] property to enable or
     * disable dark mode. It returns the current [MeepMeep] instance for method
     * chaining.
     *
     * @param isDarkMode A boolean indicating whether dark mode should be
     *    enabled.
     * @return The [MeepMeep] instance for method chaining.
     * @see [ColorManager]
     */
    fun setDarkMode(isDarkMode: Boolean): MeepMeep {
        // Update the dark mode setting in the ColorManager
        colorManager.isDarkMode = isDarkMode

        // Return the current instance for method chaining
        return this
    }

    /**
     * Adjusts the canvas dimensions to match the window size and updates all
     * entities with the new dimensions.
     *
     * This method sets the canvas width and height in [FieldUtil] to the
     * current window dimensions. It then iterates through the [entityList]
     * and calls [Entity.setCanvasDimensions] on each entity to update their
     * dimensions accordingly.
     *
     * @see FieldUtil
     * @see Entity.setCanvasDimensions
     */
    private fun onCanvasResize() {
        // Set the canvas width and height in FieldUtil to the current window dimensions
        FieldUtil.CANVAS_WIDTH = windowX.toDouble()
        FieldUtil.CANVAS_HEIGHT = windowY.toDouble()

        // Update the canvas dimensions for each entity in the entityList
        entityList.forEach {
            it.setCanvasDimensions(FieldUtil.CANVAS_WIDTH, FieldUtil.CANVAS_HEIGHT)
        }
    }

    /**
     * Sets the interval for the [AxesEntity] in the [entityList].
     *
     * This method updates the interval of the default [AxesEntity] if it is
     * present in the [entityList]. The interval determines the spacing between
     * the axes lines on the canvas.
     *
     * @param interval The interval to set for the [AxesEntity].
     * @return The [MeepMeep] instance for method chaining.
     */
    fun setAxesInterval(interval: Int): MeepMeep {
        // Check if the default [AxesEntity] is in the [entityList]
        if (DEFAULT_AXES_ENTITY in entityList) {
            // Set the interval for the default [AxesEntity]
            DEFAULT_AXES_ENTITY.setInterval(interval)
        }

        // Return the current instance for method chaining
        return this
    }

    /**
     * Adds an [Entity] to the MeepMeep application.
     *
     * This method adds the specified [Entity] to the [entityList],
     * updates the z-index manager, and registers the entity for
     * mouse events if applicable. It also adds the entity to the
     * [TrajectoryProgressSliderMaster] panel if it is a [RoadRunnerBotEntity],
     * and triggers the [EntityEventListener.onAddToEntityList]
     * method if the entity implements [EntityEventListener].
     *
     * @param entity The [Entity] to be added to the application.
     * @return The [MeepMeep] instance for method chaining.
     */
    fun addEntity(entity: Entity): MeepMeep {
        // Add the entity to the z-index manager
        zIndexManager.addEntity(entity)

        // Add the entity to the entity list and mark the list as dirty
        entityList.add(entity)
        entityListDirty = true

        // Register the entity for mouse events if it implements MouseListener
        if (entity is MouseListener) canvas.addMouseListener(entity)

        // Register the entity for mouse motion events if it implements MouseMotionListener
        if (entity is MouseMotionListener) canvas.addMouseMotionListener(entity)

        // Add the entity to the progress slider panel if it is a RoadRunnerBotEntity
        if (entity is RoadRunnerBotEntity) progressSliderMasterPanel.addRoadRunnerBot(entity)

        // Trigger the onAddToEntityList method if the entity implements EntityEventListener
        if (entity is EntityEventListener) entity.onAddToEntityList()

        // Return the current instance for method chaining
        return this
    }

    /**
     * Removes an [Entity] from the MeepMeep application.
     *
     * This method removes the specified [Entity] from the [entityList],
     * unregisters it from mouse events if applicable, and removes it from the
     * [TrajectoryProgressSliderMaster] panel if it is a [RoadRunnerBotEntity].
     * It also triggers the [EntityEventListener.onRemoveFromEntityList]
     * method if the entity implements [EntityEventListener].
     *
     * @param entity The [Entity] to be removed from the application.
     * @return The [MeepMeep] instance for method chaining.
     */
    fun removeEntity(entity: Entity): MeepMeep {
        // Remove the entity from the entity list
        entityList.remove(entity)

        // Remove the entity from the requested add entity list
        requestedAddEntityList.remove(entity)

        // Mark the entity list as dirty to indicate it needs to be sorted
        entityListDirty = true

        // Unregister the entity from mouse events if it implements MouseListener
        if (entity is MouseListener) canvas.removeMouseListener(entity)

        // Unregister the entity from mouse motion events if it implements MouseMotionListener
        if (entity is MouseMotionListener) canvas.removeMouseMotionListener(entity)

        // Remove the entity from the progress slider panel if it is a RoadRunnerBotEntity
        if (entity is RoadRunnerBotEntity) progressSliderMasterPanel.removeRoadRunnerBot(entity)

        // Trigger the onRemoveFromEntityList method if the entity implements EntityEventListener
        if (entity is EntityEventListener) entity.onRemoveFromEntityList()

        // Return the current instance for method chaining
        return this
    }

    /**
     * Requests to add an [Entity] to the MeepMeep application.
     *
     * This method adds the specified [Entity] to the [requestedAddEntityList],
     * marks the [entityList] as dirty to indicate it needs to be sorted,
     * and returns the current [MeepMeep] instance for method chaining.
     *
     * @param entity The [Entity] to be added to the application.
     * @return The [MeepMeep] instance for method chaining.
     */
    fun requestToAddEntity(entity: Entity): MeepMeep {
        // Add the entity to the requested add entity list
        requestedAddEntityList.add(entity)

        // Mark the entity list as dirty to indicate it needs to be sorted
        entityListDirty = true

        // Return the current instance for method chaining
        return this
    }

    /**
     * Requests to remove an [Entity] from the MeepMeep application.
     *
     * This method adds the specified [Entity] to the
     * [requestedRemoveEntityList], marks the [entityList] as dirty to indicate
     * it needs to be sorted, and returns the current [MeepMeep] instance for
     * method chaining.
     *
     * @param entity The [Entity] to be removed from the application.
     * @return The [MeepMeep] instance for method chaining.
     * @see [requestToAddEntity]
     * @see [removeEntity]
     */
    fun requestToRemoveEntity(entity: Entity): MeepMeep {
        // Add the entity to the requested remove entity list
        requestedRemoveEntityList.add(entity)

        // Mark the entity list as dirty to indicate it needs to be sorted
        entityListDirty = true

        // Return the current instance for method chaining
        return this
    }

    /**
     * Sets the alpha transparency level for the background image.
     *
     * This method adjusts the alpha transparency level of the background image
     * used in the [MeepMeep] application. The alpha value should be between
     * 0.0 (completely transparent) and 1.0 (completely opaque).
     *
     * @param alpha The alpha transparency level to set for the background
     *    image.
     * @return The [MeepMeep] instance for method chaining.
     * @see [MeepMeep.setBackground]
     */
    fun setBackgroundAlpha(alpha: Float): MeepMeep {
        // Set the alpha transparency level for the background image
        bgAlpha = alpha

        // Return the current instance for method chaining
        return this
    }

    /**
     * Enum class representing various background options for the MeepMeep
     * application.
     *
     * Each enum constant corresponds to a specific background image that
     * can be set using the [MeepMeep.setBackground] method. The backgrounds
     * include grid patterns, official field images from different seasons, and
     * custom themes.
     *
     * @see [MeepMeep.setBackground]
     * @see [MeepMeep.setBackgroundAlpha]
     */
    enum class Background {
        GRID_BLUE,
        GRID_GREEN,
        GRID_GRAY,
        FIELD_SKYSTONE_OFFICIAL,
        FIELD_SKYSTONE_GF_DARK,
        FIELD_SKYSTONE_INNOV8RZ_LIGHT,
        FIELD_SKYSTONE_INNOV8RZ_DARK,
        FIELD_SKYSTONE_STARWARS_DARK,
        FIELD_ULTIMATEGOAL_INNOV8RZ_DARK,
        FIELD_FREIGHTFRENZY_OFFICIAL,
        FIELD_FREIGHTFRENZY_ADI_DARK,
        FIELD_POWERPLAY_OFFICIAL,
        FIELD_POWERPLAY_KAI_DARK,
        FIELD_POWERPLAY_KAI_LIGHT,
        FIELD_CENTERSTAGE_OFFICIAL,
        FIELD_CENTERSTAGE_JUICE_DARK,
        FIELD_CENTERSTAGE_JUICE_LIGHT,
        FIELD_INTOTHEDEEP_OFFICIAL,
        FIELD_INTOTHEDEEP_JUICE_DARK,
        FIELD_INTOTHEDEEP_JUICE_LIGHT,
        FIELD_INTOTHEDEEP_JUICE_GREYSCALE,
        FIELD_INTOTHEDEEP_JUICE_BLACK
    }
}
