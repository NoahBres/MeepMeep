package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TurnSegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.WaitSegment
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.GraphicsEnvironment
import java.awt.RenderingHints
import java.awt.Transparency
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class TrajectorySequenceEntity(
    override val meepMeep: MeepMeep,
    private val trajectorySequence: TrajectorySequence,
    private var colorScheme: ColorScheme,
) : ThemedEntity {
    /** Tag for the trajectory sequence entity. */
    override val tag = "TRAJECTORY_SEQUENCE_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex: Int = 0

    /** List to store marker indicator entities */
    val markerEntityList = mutableListOf<MarkerIndicatorEntity>()

    /** Value representing the progress of the trajectory. */
    var trajectoryProgress: Double? = null

    /** Canvas width. */
    private var canvasWidth = FieldUtil.CANVAS_WIDTH

    /** Canvas height. */
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    /** Buffered image for rendering the trajectory sequence. */
    private lateinit var baseBufferedImage: BufferedImage

    /** Buffered image for rendering the current segment. */
    private var currentSegmentImage: BufferedImage? = null

    /** List to store turn indicator entities. */
    private val turnEntityList = mutableListOf<TurnIndicatorEntity>()

    /** Last segment of the trajectory sequence. */
    private var lastSegment: TrajectorySegment? = null

    /** Current segment of the trajectory sequence. */
    private var currentSegment: TrajectorySegment? = null

    /** Static values for the trajectory sequence entity. */
    companion object {
        /** Width of the inner stroke for the path. */
        const val PATH_INNER_STROKE_WIDTH = 0.5

        /** Width of the outer stroke for the path. */
        const val PATH_OUTER_STROKE_WIDTH = 2.0

        /** Opacity of the outer path. */
        const val PATH_OUTER_OPACITY = 0.4

        /** Opacity of the unfocused path. */
        const val PATH_UNFOCUSED_OPACITY = 0.3

        /** Resolution for sampling the path. */
        const val SAMPLE_RESOLUTION = 1.2
    }

    /** Initializes the trajectory sequence entity and draws the path. */
    init {
        redrawPath()
    }

    /** Redraws the entire trajectory path. */
    private fun redrawPath() {
        // Clear previous turn indicator entities
        turnEntityList.forEach {
            meepMeep.requestToRemoveEntity(it)
        }
        turnEntityList.clear()

        // Clear previous marker indicator entities
        markerEntityList.forEach {
            meepMeep.requestToRemoveEntity(it)
        }
        markerEntityList.clear()

        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the trajectory sequence
        baseBufferedImage =
            config.createCompatibleImage(
                canvasWidth.toInt(),
                canvasHeight.toInt(),
                Transparency.TRANSLUCENT,
            )
        val gfx = baseBufferedImage.createGraphics()

        // Set rendering hints for the graphics
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Create a path for the trajectory sequence
        val trajectoryDrawnPath = Path2D.Double()

        // Create strokes for the inner path
        val innerStroke =
            BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
            )

        var currentEndPose = trajectorySequence.start()
        val firstVec = trajectorySequence.start().vec().toScreenCoord()
        trajectoryDrawnPath.moveTo(firstVec.x, firstVec.y)

        // Draw the trajectory path
        for (i in 0 until trajectorySequence.size()) {
            when (val step = trajectorySequence.get(i)) {
                is TrajectorySegment -> {
                    // Get the trajectory from the segment
                    val trajectory = step.trajectory

                    // Calculate the number of samples based on the trajectory length and sample resolution
                    val displacementSamples =
                        (trajectory.path.length() / SAMPLE_RESOLUTION).roundToInt()

                    // Generate a list of displacements along the trajectory path
                    val displacements =
                        (0..displacementSamples).map {
                            it / displacementSamples.toDouble() * trajectory.path.length()
                        }

                    // Map the displacements to poses along the trajectory path
                    val poses = displacements.map { trajectory.path[it] }

                    // Draw the trajectory path by connecting the poses
                    for (pose in poses.drop(1)) {
                        val coord = pose.vec().toScreenCoord()
                        trajectoryDrawnPath.lineTo(coord.x, coord.y)
                    }

                    // Update the current end pose to the end of the trajectory
                    currentEndPose = step.trajectory.end()
                }

                is TurnSegment -> {
                    // Create a turn indicator entity for the turn segment
                    val turnEntity =
                        TurnIndicatorEntity(
                            meepMeep,
                            colorScheme,
                            currentEndPose.vec(),
                            currentEndPose.heading,
                            currentEndPose.heading + step.totalRotation,
                        )

                    // Add the turn entity to the list and request to add it to MeepMeep
                    turnEntityList.add(turnEntity)
                    meepMeep.requestToAddEntity(turnEntity)
                }

                is WaitSegment -> {
                    // No action needed for WaitSegment
                }
            }
        }

        var currentTime = 0.0

        // Draw the markers along the trajectory sequence
        for (i in 0 until trajectorySequence.size()) {
            val segment = trajectorySequence.get(i)
            if (segment is WaitSegment || segment is TurnSegment) {
                // Iterate through each marker in the segment
                segment.markers.forEach { marker ->
                    // Determine the pose based on the segment type
                    val pose =
                        when (segment) {
                            is WaitSegment -> segment.startPose
                            is TurnSegment -> segment.startPose.copy(heading = segment.motionProfile[marker.time].x)
                            else -> Pose2d()
                        }

                    // Create a new marker entity with the determined pose and add it to the list
                    val markerEntity =
                        MarkerIndicatorEntity(
                            meepMeep,
                            colorScheme,
                            pose,
                            marker.callback,
                            currentTime + marker.time,
                        )
                    markerEntityList.add(markerEntity)
                    meepMeep.requestToAddEntity(markerEntity)
                }
            } else if (segment is TrajectorySegment) {
                // Iterate through each marker in the trajectory segment
                segment.trajectory.markers.forEach { marker ->
                    // Get the pose at the marker time
                    val pose = segment.trajectory[marker.time]

                    // Create a new marker entity with the determined pose and add it to the list
                    val markerEntity =
                        MarkerIndicatorEntity(
                            meepMeep,
                            colorScheme,
                            pose,
                            marker.callback,
                            currentTime + marker.time,
                        )
                    markerEntityList.add(markerEntity)
                    meepMeep.requestToAddEntity(markerEntity)
                }
            }

            // Update the current time based on the segment duration
            currentTime += segment.duration
        }

        gfx.stroke = innerStroke
        gfx.color = colorScheme.trajectoryPathColor
        gfx.color =
            Color(
                colorScheme.trajectoryPathColor.red,
                colorScheme.trajectoryPathColor.green,
                colorScheme.trajectoryPathColor.blue,
                (PATH_UNFOCUSED_OPACITY * 255).toInt(),
            )
        gfx.draw(trajectoryDrawnPath)
    }

    /** Redraws the current segment of the trajectory. */
    private fun redrawCurrentSegment() {
        // If there is no current segment, clear the current segment image and return
        if (currentSegment == null) {
            currentSegmentImage = null
            return
        }

        // Get the default screen device and configuration
        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        // Create a compatible image for the current segment
        currentSegmentImage =
            config.createCompatibleImage(
                canvasWidth.toInt(),
                canvasHeight.toInt(),
                Transparency.TRANSLUCENT,
            )
        val gfx = currentSegmentImage!!.createGraphics()

        // Set rendering hints for the graphics
        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        // Create a path for the trajectory segment
        val trajectoryDrawnPath = Path2D.Double()

        // Create stroke for the outer and inner paths
        val outerStroke =
            BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_OUTER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
            )
        val innerStroke =
            BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_ROUND,
            )

        // Get the trajectory from the current segment
        val trajectory = currentSegment!!.trajectory

        // Move to the starting position of the trajectory
        val firstVec = currentSegment!!.startPose.vec().toScreenCoord()
        trajectoryDrawnPath.moveTo(firstVec.x, firstVec.y)

        // Calculate the number of samples based on the trajectory length and sample resolution
        val displacementSamples = (trajectory.path.length() / SAMPLE_RESOLUTION).roundToInt()

        // Generate a list of displacements along the trajectory path
        val displacements =
            (0..displacementSamples).map {
                it / displacementSamples.toDouble() * trajectory.path.length()
            }

        // Map the displacements to poses along the trajectory path
        val poses = displacements.map { trajectory.path[it] }

        // Draw the trajectory path by connecting the poses
        for (pose in poses.drop(1)) {
            val coord = pose.vec().toScreenCoord()
            trajectoryDrawnPath.lineTo(coord.x, coord.y)
        }

        // Draw the outer path with the specified opacity and color
        gfx.stroke = outerStroke
        gfx.color =
            Color(
                colorScheme.trajectoryPathColor.red,
                colorScheme.trajectoryPathColor.green,
                colorScheme.trajectoryPathColor.blue,
                (PATH_OUTER_OPACITY * 255).toInt(),
            )
        gfx.draw(trajectoryDrawnPath)

        // Draw the inner path with the full color
        gfx.stroke = innerStroke
        gfx.color = colorScheme.trajectoryPathColor
        gfx.draw(trajectoryDrawnPath)
    }

    /**
     * Updates the current segment of the trajectory sequence based on the
     * progress.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    override fun update(deltaTime: Long) {
        if (trajectoryProgress == null) {
            // If there is no trajectory progress, set the current segment to null
            currentSegment = null
        } else {
            var currentTime = 0.0
            for (i in 0 until trajectorySequence.size()) {
                val seg = trajectorySequence.get(i)

                if (currentTime + seg.duration > trajectoryProgress!!) {
                    // If the current time plus the segment duration exceeds the trajectory progress,
                    // set the current segment to this segment
                    if (seg is TrajectorySegment) currentSegment = seg

                    break
                } else {
                    // Otherwise, add the segment duration to the current time
                    currentTime += seg.duration
                }
            }
        }

        // If the last segment is different from the current segment, redraw the current segment
        if (lastSegment != currentSegment) {
            redrawCurrentSegment()
        }

        // Update the last segment to the current segment
        lastSegment = currentSegment
    }

    /**
     * Renders the trajectory sequence entity on the given graphics context.
     *
     * @param gfx The graphics context to render on.
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    override fun render(
        gfx: Graphics2D,
        canvasWidth: Int,
        canvasHeight: Int,
    ) {
        // Draw the base buffered image
        gfx.drawImage(baseBufferedImage, null, 0, 0)

        // Draw the current segment image if it exists
        if (currentSegmentImage != null) gfx.drawImage(currentSegmentImage, null, 0, 0)
    }

    /**
     * Sets the dimensions of the canvas and redraws the path if the dimensions
     * have changed.
     *
     * @param canvasWidth The new width of the canvas.
     * @param canvasHeight The new height of the canvas.
     */
    override fun setCanvasDimensions(
        canvasWidth: Double,
        canvasHeight: Double,
    ) {
        // Check if the canvas dimensions have changed
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) {
            // Redraw the path if the dimensions have changed
            redrawPath()
        }
        // Update the canvas dimensions
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    /**
     * Switches the color scheme of the trajectory sequence entity.
     *
     * @param scheme The new color scheme to be applied.
     */
    override fun switchScheme(scheme: ColorScheme) {
        // Check if the new color scheme is different from the current one
        if (this.colorScheme != scheme) {
            // Update the color scheme
            this.colorScheme = scheme
            // Redraw the path with the new color scheme
            redrawPath()
        }
    }
}
