package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.ThemedEntity
import com.noahbres.meepmeep.core.exhaustive
import com.noahbres.meepmeep.core.toScreenCoord
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.roadrunner.trajectorysequence.*
import java.awt.*
import java.awt.geom.Path2D
import java.awt.image.BufferedImage
import kotlin.math.roundToInt

class TrajectorySequenceEntity(
        override val meepMeep: MeepMeep,
        private val trajectorySequence: TrajectorySequence,
        private var colorScheme: ColorScheme
) : ThemedEntity {
    private var canvasWidth = FieldUtil.CANVAS_WIDTH
    private var canvasHeight = FieldUtil.CANVAS_HEIGHT

    override val tag = "TRAJECTORY_SEQUENCE_ENTITY"

    override var zIndex: Int = 0

    private val turnEntityList = mutableListOf<TurnIndicatorEntity>()
    val markerEntityList = mutableListOf<MarkerIndicatorEntity>()

    private val PATH_INNER_STROKE_WIDTH = 0.5
    private val PATH_OUTER_STROKE_WIDTH = 2.0

    private val PATH_OUTER_OPACITY = 0.4

    private val SAMPLE_RESOLUTION = 1.2

    private lateinit var baseBufferedImage: BufferedImage

    init {
        redrawPath()
    }

    private fun redrawPath() {
        // Request to clear previous turn indicator entities
        turnEntityList.forEach {
            meepMeep.requestToClearEntity(it)
        }
        turnEntityList.clear()

        // Request to clear previous marker indicator entities
        markerEntityList.forEach {
            meepMeep.requestToClearEntity(it)
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
        val outerStroke = BasicStroke(
                FieldUtil.scaleInchesToPixel(PATH_OUTER_STROKE_WIDTH).toFloat(),
                BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND
        )

        var currentEndPose = trajectorySequence.start()

        val firstVec = trajectorySequence.start().vec().toScreenCoord()
        trajectoryDrawnPath.moveTo(firstVec.x, firstVec.y)

        trajectorySequence.forEach { step ->
            when (step) {
                is TrajectorySegment -> {
                    val traj = step.trajectory

                    val displacementSamples = (traj.path.length() / SAMPLE_RESOLUTION).roundToInt()

                    val displacements = (0..displacementSamples).map {
                        it / displacementSamples.toDouble() * traj.path.length()
                    }

                    val poses = displacements.map { traj.path[it] }

                    for(pose in poses.drop(1)) {
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
                is WaitSegment -> {}
            }
        }

//        trajectorySequence.markers.forEach {
//            val markerEntity = MarkerIndicatorEntity(meepMeep, colorScheme, trajectorySequence[it.time], it.time)
//            markerEntityList.add(markerEntity)
//            meepMeep.requestToAddEntity(markerEntity)
//        }

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

    override fun update(deltaTime: Long) {}

    override fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int) {
        gfx.drawImage(baseBufferedImage, null, 0, 0)
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