package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.roadrunner.trajectorysequence.*
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TurnSegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.WaitSegment
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class TrajectorySequenceEntity(
    override val meepMeep: MeepMeep,
    private val trajectorySequence: TrajectorySequence,
    private var colorScheme: ColorScheme
) : ThemedEntity {
    companion object {
        const val PATH_INNER_STROKE_WIDTH = 0.5
        const val PATH_OUTER_STROKE_WIDTH = 2.0

        const val PATH_OUTER_OPACITY = 0.4

        const val PATH_UNFOCUSED_OPACTIY = 0.3

        const val SAMPLE_RESOLUTION = 1.2
    }

    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    override val tag = "TRAJECTORY_SEQUENCE_ENTITY"

    override var zIndex: Int = 0

    private val turnEntityList = mutableListOf<TurnIndicatorEntity>()
    val markerEntityList = mutableListOf<MarkerIndicatorEntity>()

    private lateinit var baseBufferedImage: BufferedImage

    private var currentSegmentImage: BufferedImage? = null

    private var lastSegment: TrajectorySegment? = null
    private var currentSegment: TrajectorySegment? = null

    var trajectoryProgress: Double? = null

    init {
        redrawPath()
    }

    private fun redrawPath() {
        // Request to clear previous turn indicator entities
        turnEntityList.forEach {
            meepMeep.requestToRemoveEntity(it)
        }
        turnEntityList.clear()

        // Request to clear previous marker indicator entities
        markerEntityList.forEach {
            meepMeep.requestToRemoveEntity(it)
        }
        markerEntityList.clear()

        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        baseBufferedImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )
        val gfx = baseBufferedImage.createGraphics()

        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val trajectoryDrawnPath = Path2D.Double()

        val innerStroke = BasicStroke(
            FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )
//        val outerStroke = BasicStroke(
//            FieldUtil.scaleInchesToPixel(PATH_OUTER_STROKE_WIDTH).toFloat(),
//            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
//        )

        var currentEndPose = trajectorySequence.start()

        val firstVec = trajectorySequence.start().vec().toScreenCoord()
        trajectoryDrawnPath.moveTo(firstVec.x, firstVec.y)

        for (i in 0 until trajectorySequence.size()) {
            when (val step = trajectorySequence.get(i)) {
                is TrajectorySegment -> {
                    val traj = step.trajectory

                    val displacementSamples = (traj.path.length() / SAMPLE_RESOLUTION).roundToInt()

                    val displacements = (0..displacementSamples).map {
                        it / displacementSamples.toDouble() * traj.path.length()
                    }

                    val poses = displacements.map { traj.path[it] }

                    for (pose in poses.drop(1)) {
                        val coord = pose.vec().toScreenCoord()
                        trajectoryDrawnPath.lineTo(coord.x, coord.y)
                    }

                    currentEndPose = step.trajectory.end()
                }

                is TurnSegment -> {
                    val turnEntity = TurnIndicatorEntity(
                        meepMeep, colorScheme, currentEndPose.vec(), currentEndPose.heading,
                        currentEndPose.heading + step.totalRotation
                    )
                    turnEntityList.add(turnEntity)
                    meepMeep.requestToAddEntity(turnEntity)
                }
                is WaitSegment -> {
                }
            }
        }

        var currentTime = 0.0

        for (i in 0 until trajectorySequence.size()) {
            val segment = trajectorySequence.get(i)
            if (segment is WaitSegment || segment is TurnSegment) {
                segment.markers.forEach { marker ->
                    val pose = when (segment) {
                        is WaitSegment -> segment.startPose
                        is TurnSegment -> segment.startPose.copy(heading = segment.motionProfile[marker.time].x)
                        else -> Pose2d()
                    }

                    val markerEntity =
                        MarkerIndicatorEntity(meepMeep, colorScheme, pose, marker.callback, currentTime + marker.time)
                    markerEntityList.add(markerEntity)
                    meepMeep.requestToAddEntity(markerEntity)
                }
            } else if (segment is TrajectorySegment) {
                segment.trajectory.markers.forEach { marker ->
                    val pose = segment.trajectory[marker.time]

                    val markerEntity =
                        MarkerIndicatorEntity(meepMeep, colorScheme, pose, marker.callback, currentTime + marker.time)
                    markerEntityList.add(markerEntity)
                    meepMeep.requestToAddEntity(markerEntity)
                }
            }

            currentTime += segment.duration
        }

//        gfx.stroke = outerStroke
//        gfx.color = Color(
//                colorScheme.TRAJCETORY_PATH_COLOR.red, colorScheme.TRAJCETORY_PATH_COLOR.green,
//                colorScheme.TRAJCETORY_PATH_COLOR.blue, (PATH_OUTER_OPACITY * 255).toInt()
//        )
//        gfx.draw(trajectoryDrawnPath)

        gfx.stroke = innerStroke
        gfx.color = colorScheme.TRAJCETORY_PATH_COLOR
        gfx.color = Color(
            colorScheme.TRAJCETORY_PATH_COLOR.red, colorScheme.TRAJCETORY_PATH_COLOR.green,
            colorScheme.TRAJCETORY_PATH_COLOR.blue, (PATH_UNFOCUSED_OPACTIY * 255).toInt()

        )
        gfx.draw(trajectoryDrawnPath)
    }

    private fun redrawCurrentSegment() {
        if (currentSegment == null) {
            currentSegmentImage = null
            return
        }

        val environment = GraphicsEnvironment.getLocalGraphicsEnvironment()
        val device = environment.defaultScreenDevice
        val config = device.defaultConfiguration

        currentSegmentImage = config.createCompatibleImage(
            canvasWidth.toInt(), canvasHeight.toInt(), Transparency.TRANSLUCENT
        )
        val gfx = currentSegmentImage!!.createGraphics()

        gfx.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        gfx.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)

        val trajectoryDrawnPath = Path2D.Double()

        val outerStroke = BasicStroke(
            FieldUtil.scaleInchesToPixel(PATH_OUTER_STROKE_WIDTH).toFloat(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )
        val innerStroke = BasicStroke(
            FieldUtil.scaleInchesToPixel(PATH_INNER_STROKE_WIDTH).toFloat(),
            BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )

        val traj = currentSegment!!.trajectory

        val firstVec = currentSegment!!.startPose.vec().toScreenCoord()
        trajectoryDrawnPath.moveTo(firstVec.x, firstVec.y)

        val displacementSamples = (traj.path.length() / SAMPLE_RESOLUTION).roundToInt()

        val displacements = (0..displacementSamples).map {
            it / displacementSamples.toDouble() * traj.path.length()
        }

        val poses = displacements.map { traj.path[it] }

        for (pose in poses.drop(1)) {
            val coord = pose.vec().toScreenCoord()
            trajectoryDrawnPath.lineTo(coord.x, coord.y)
        }

        gfx.stroke = outerStroke
        gfx.color = Color(
            colorScheme.TRAJCETORY_PATH_COLOR.red, colorScheme.TRAJCETORY_PATH_COLOR.green,
            colorScheme.TRAJCETORY_PATH_COLOR.blue, (PATH_OUTER_OPACITY * 255).toInt()
        )
        gfx.draw(trajectoryDrawnPath)

        gfx.stroke = innerStroke
        gfx.color = colorScheme.TRAJCETORY_PATH_COLOR
        gfx.draw(trajectoryDrawnPath)
    }

    override fun update(deltaTime: Long) {
        if (trajectoryProgress == null) {
            currentSegment = null
        } else {
            var currentTime = 0.0
            for (i in 0 until trajectorySequence.size()) {
                val seg = trajectorySequence.get(i)

                if (currentTime + seg.duration > trajectoryProgress!!) {
                    if (seg is TrajectorySegment) currentSegment = seg

                    break
                } else {
                    currentTime += seg.duration
                }
            }
        }

        if (lastSegment != currentSegment) {
            redrawCurrentSegment()
        }

        lastSegment = currentSegment
    }

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        gfx.drawImage(baseBufferedImage, null, 0, 0)

        if (currentSegmentImage != null) gfx.drawImage(currentSegmentImage, null, 0, 0)
    }

    override fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double) {
        if (this.canvasWidth != canvasWidth || this.canvasHeight != canvasHeight) redrawPath()
        this.canvasWidth = canvasWidth
        this.canvasHeight = canvasHeight
    }

    override fun switchScheme(scheme: ColorScheme) {
        if (this.colorScheme != scheme) {
            this.colorScheme = scheme
            redrawPath()
        }
    }
}