package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.BotEntity
import com.noahbres.meepmeep.core.exhaustive
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.roadrunner.Constraints
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.DriveTrainType
import com.noahbres.meepmeep.roadrunner.trajectorysequence.*
import com.noahbres.meepmeep.roadrunner.ui.TrajectoryProgressSlider

class RoadRunnerBotEntity(
    meepMeep: MeepMeep,
    private var constraints: Constraints,

    width: Double, height: Double,
    pose: Pose2d,

    private val colorScheme: ColorScheme,
    opacity: Double
) : BotEntity(meepMeep, width, height, pose, colorScheme, opacity) {
    companion object {
        const val SKIP_LOOPS = 2
        const val PROGRESS_SLIDER_HEIGHT = 20
    }

    override val tag = "RR_BOT_ENTITY"

    override var zIndex: Int = 0

    private var driveTrainType = DriveTrainType.MECANUM
    var drive = DriveShim(driveTrainType, constraints, pose)

    var currentTrajectorySequence: TrajectorySequence? = null

    private var trajectorySequenceEntity: TrajectorySequenceEntity? = null

    private var looping = true
    private var running = false

    private var trajectorySequenceElapsedTime = 0.0
        set(value) {
            trajectorySequenceEntity?.trajectoryProgress = value
            field = value
        }

    var trajectoryPaused = false

    private var skippedLoops = 0

    private val progressSlider = TrajectoryProgressSlider(
        this,
        FieldUtil.CANVAS_WIDTH.toInt(),
        PROGRESS_SLIDER_HEIGHT,
        colorScheme.TRAJECTORY_SLIDER_FG,
        colorScheme.TRAJECTORY_SLIDER_BG,
        colorScheme.TRAJECTORY_TEXT_COLOR,
        MeepMeep.FONT_CMU_BOLD
    )

    init {
        progressSlider.progress = 0.0
        meepMeep.sliderPanel.add(progressSlider)
        meepMeep.windowFrame.pack()

        meepMeep.windowFrame
    }

    override fun update(deltaTime: Long) {
        if (!running) return

        if (skippedLoops++ < SKIP_LOOPS) return

        if (!trajectoryPaused) trajectorySequenceElapsedTime += deltaTime / 1000.0 / 1000.0 / 1000.0

        when {
            trajectorySequenceElapsedTime <= currentTrajectorySequence!!.duration -> {
                var segment: SequenceSegment? = null
                var segmentOffsetTime = 0.0

                var currentTime = 0.0

                for (seg in currentTrajectorySequence!!.list) {
                    if (currentTime + seg.duration > trajectorySequenceElapsedTime) {
                        segmentOffsetTime = trajectorySequenceElapsedTime - currentTime
                        segment = seg

                        break
                    } else {
                        currentTime += seg.duration
                    }
                }

                pose = when (segment) {
                    is WaitSegment -> segment.startPose
                    is TurnSegment -> segment.startPose.copy(heading = segment.motionProfile[segmentOffsetTime].x)
                    is TrajectorySegment -> segment.trajectory[segmentOffsetTime]
                    else -> currentTrajectorySequence!!.end
                }

                drive.poseEstimate = pose;

                trajectorySequenceEntity!!.markerEntityList.forEach { if (trajectorySequenceElapsedTime >= it.time) it.passed() }

                progressSlider.progress = (trajectorySequenceElapsedTime / currentTrajectorySequence!!.duration)
            }

            looping -> {
                trajectorySequenceEntity!!.markerEntityList.forEach {
                    it.reset()
                }
                trajectorySequenceElapsedTime = 0.0
            }

            else -> {
                trajectorySequenceElapsedTime = 0.0
                currentTrajectorySequence = null
            }
        }.exhaustive
    }

    fun start() {
        running = true
        trajectorySequenceElapsedTime = 0.0
    }

    fun pause() {
        trajectoryPaused = true
    }

    fun unPause() {
        trajectoryPaused = false
    }

    fun togglePause() {
        trajectoryPaused = !trajectoryPaused
    }

    fun setTrajectoryProgress(progress: Double) {
        if (currentTrajectorySequence != null)
            trajectorySequenceElapsedTime = progress * currentTrajectorySequence!!.duration
    }

    fun followTrajectorySequence(sequence: TrajectorySequence) {
        currentTrajectorySequence = sequence

        trajectorySequenceEntity = TrajectorySequenceEntity(meepMeep, sequence, colorScheme)
        meepMeep.addEntity(trajectorySequenceEntity!!)
    }

    fun setConstraints(constraints: Constraints) {
        this.constraints = constraints

        drive = DriveShim(driveTrainType, constraints, pose)
    }

    fun setDriveTrainType(driveTrainType: DriveTrainType) {
        this.driveTrainType = driveTrainType

        drive = DriveShim(driveTrainType, constraints, pose)
    }

    override fun switchScheme(scheme: ColorScheme) {
        super.switchScheme(scheme)

        this.progressSlider.fg = scheme.TRAJECTORY_SLIDER_FG
        this.progressSlider.bg = scheme.TRAJECTORY_SLIDER_BG
        this.progressSlider.textColor = scheme.TRAJECTORY_TEXT_COLOR
    }
}