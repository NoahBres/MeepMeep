package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.BotEntity
import com.noahbres.meepmeep.core.exhaustive
import com.noahbres.meepmeep.core.util.FieldUtil
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.DriveTrainType
import com.noahbres.meepmeep.roadrunner.trajectorysequence.*
import com.noahbres.meepmeep.roadrunner.ui.TrajectoryProgressSlider

class RoadRunnerBotEntity(
        meepMeep: MeepMeep,
        private var constraints: DriveConstraints,
        width: Double, height: Double,
        private var trackWidth: Double,
        pose: Pose2d,
        private val colorScheme: ColorScheme,
        opacity: Double
) : BotEntity(meepMeep, width, height, pose, colorScheme, opacity) {
    override val zIndex: Int = 6

    private var driveTrainType = DriveTrainType.MECANUM
    var drive = DriveShim(driveTrainType, constraints, trackWidth)

    private var followMode = FollowMode.TRAJECTORY_LIST

    private var currentTrajectoryList = emptyList<Trajectory>()
    var currentTrajectorySequence: TrajectorySequence? = null

    private var trajectorySequenceEntity: TrajectorySequenceEntity? = null

    private var looping = true
    private var running = false

    private var trajectorySequenceElapsedTime = 0.0
    var trajectoryPaused = false

    private val SKIP_LOOPS = 2
    private var skippedLoops = 0

    private val PROGRESS_SLIDER_HEIGHT = 20
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
//        if (!running) return
//
//        if (skippedLoops++ < SKIP_LOOPS) return
//
//        if (followMode == FollowMode.TRAJECTORY_LIST) {
//
//        } else if (followMode == FollowMode.TRAJECTORY_SEQUENCE && currentTrajectorySequence != null) {
//            if (!trajectoryPaused) trajectorySequenceElapsedTime += deltaTime / 1000.0
//
//            when {
//                trajectorySequenceElapsedTime <= currentTrajectorySequence!!.duration -> {
//                    val (currentStateStep, currentStateOffset) = currentTrajectorySequence!!.getCurrentState(
//                            trajectorySequenceElapsedTime
//                    )
//                    when (currentStateStep) {
//                        is TrajectorySegment -> {
//                            pose = currentStateStep.trajectory[currentStateOffset]
//
//                            trajectorySequenceEntity!!.markerEntityList.forEach {
//                                if (it.trajectoryStep == currentStateStep) {
//                                    if (currentStateOffset >= it.time) it.passed()
//                                }
//                            }
//                        }
//                        is TurnSegment -> {
//                            val currVec = currentStateStep.pos
//                            pose = Pose2d(
//                                    currVec.x, currVec.y,
//                                    currentStateStep.motionProfile[currentStateOffset].x
//                            )
//                        }
//                        is WaitStep,
//                        is WaitConditionalStep -> {}
//                    }.exhaustive
//
//                    progressSlider.progress = (trajectorySequenceElapsedTime / currentTrajectorySequence!!.duration)
//                }
//
//                looping -> {
//                    trajectorySequenceEntity!!.markerEntityList.forEach {
//                        it.reset()
//                    }
//                    trajectorySequenceElapsedTime = 0.0
//                }
//
//                else -> {
//                    trajectorySequenceElapsedTime = 0.0
//                    currentTrajectorySequence = null
//                }
//            }.exhaustive
//        }
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
        followMode = FollowMode.TRAJECTORY_SEQUENCE

        currentTrajectorySequence = sequence

        trajectorySequenceEntity = TrajectorySequenceEntity(meepMeep, sequence, colorScheme)
        meepMeep.addEntity(trajectorySequenceEntity!!)
    }

    fun followTrajectoryList(trajectoryList: List<Trajectory>) {
        followMode = FollowMode.TRAJECTORY_LIST

        currentTrajectoryList = trajectoryList
    }

    fun setTrackWidth(trackWidth: Double) {
        this.trackWidth = trackWidth

        drive = DriveShim(driveTrainType, constraints, trackWidth)
    }

    fun setConstraints(constraints: DriveConstraints) {
        this.constraints = constraints

        drive = DriveShim(driveTrainType, constraints, trackWidth)
    }

    fun setDriveTrainType(driveTrainType: DriveTrainType) {
        this.driveTrainType = driveTrainType

        drive = DriveShim(driveTrainType, constraints, trackWidth)
    }

    override fun switchScheme(scheme: ColorScheme) {
        super.switchScheme(scheme)

        this.progressSlider.fg = scheme.TRAJECTORY_SLIDER_FG
        this.progressSlider.bg = scheme.TRAJECTORY_SLIDER_BG
        this.progressSlider.textColor = scheme.TRAJECTORY_TEXT_COLOR
    }

    enum class FollowMode {
        TRAJECTORY_LIST,
        TRAJECTORY_SEQUENCE
    }
}