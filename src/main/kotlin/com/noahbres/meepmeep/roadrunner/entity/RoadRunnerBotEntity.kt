package com.noahbres.meepmeep.roadrunner.entity

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.roadrunner.Constraints
import com.noahbres.meepmeep.roadrunner.DriveShim
import com.noahbres.meepmeep.roadrunner.DriveTrainType
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.SequenceSegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TurnSegment
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.WaitSegment
import com.noahbres.meepmeep.roadrunner.ui.TrajectoryProgressSliderMaster
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.core.entity.BotEntity
import com.noahbres.meepmeep.core.entity.EntityEventListener
import com.noahbres.meepmeep.core.exhaustive
import kotlin.math.min

/**
 * Represents a RoadRunner bot entity in the MeepMeep simulation.
 *
 * @param meepMeep The MeepMeep instance.
 * @param constraints The motion constraints for the bot.
 * @param width The width of the bot.
 * @param height The height of the bot.
 * @param pose The initial pose of the bot.
 * @param colorScheme The color scheme for the bot.
 * @param opacity The opacity of the bot.
 * @param driveTrainType The type of drivetrain used by the bot.
 * @param listenToSwitchThemeRequest Flag to listen to theme switch
 *    requests.
 */
class RoadRunnerBotEntity(
    meepMeep: MeepMeep,
    private var constraints: Constraints,
    width: Double, height: Double,
    pose: Pose2d,
    val colorScheme: ColorScheme,
    opacity: Double,
    private var driveTrainType: DriveTrainType = DriveTrainType.MECANUM,
    private var listenToSwitchThemeRequest: Boolean = false
) : BotEntity(meepMeep, width, height, pose, colorScheme, opacity), EntityEventListener {
    /** Tag for the bot entity. */
    override val tag = "RR_BOT_ENTITY"

    /** Z-index for rendering order. */
    override var zIndex: Int = 0

    companion object {
        /** Number of loops to skip initially to avoid startup issues. */
        const val SKIP_LOOPS = 2
    }

    /**
     * Drive shim for the bot, initialized with the drivetrain type,
     * constraints, and initial pose.
     */
    var drive = DriveShim(driveTrainType, constraints, pose)

    /** The current trajectory sequence the bot is following. */
    var currentTrajectorySequence: TrajectorySequence? = null

    /** Entity representing the trajectory sequence. */
    private var trajectorySequenceEntity: TrajectorySequenceEntity? = null

    /** Flag indicating if the bot should loop the trajectory sequence. */
    var looping = true

    /** Flag indicating if the bot is currently running. */
    private var isExcutingTrajectory = false

    /** Elapsed time for the current trajectory sequence. */
    private var trajectorySequenceElapsedTime = 0.0
        set(value) {
            trajectorySequenceEntity?.trajectoryProgress = value
            field = value
        }

    /** Flag indicating if the trajectory sequence is paused. */
    var isTrajectoryPaused = false

    /** Counter for the number of skipped loops. */
    private var skippedLoops = 0

    /** Master controller for the trajectory progress slider. */
    private var sliderMaster: TrajectoryProgressSliderMaster? = null

    /** Index of the slider master. */
    private var sliderMasterIndex: Int? = null

    /**
     * Updates the bot entity.
     *
     * @param deltaTime The time since the last update.
     */
    override fun update(deltaTime: Long) {
        if (!isExcutingTrajectory) return

        // Skip initial loops to avoid startup issues
        if (skippedLoops++ < SKIP_LOOPS) return

        if (!isTrajectoryPaused) trajectorySequenceElapsedTime += deltaTime / 1e9

        when {
            trajectorySequenceElapsedTime <= currentTrajectorySequence!!.duration() -> {
                var segment: SequenceSegment? = null
                var segmentOffsetTime = 0.0
                var currentTime = 0.0

                // Find the current segment based on elapsed time
                for (i in 0 until currentTrajectorySequence!!.size()) {
                    val seg = currentTrajectorySequence!!.get(i)

                    if (currentTime + seg.duration > trajectorySequenceElapsedTime) {
                        segmentOffsetTime = trajectorySequenceElapsedTime - currentTime
                        segment = seg

                        break
                    } else {
                        currentTime += seg.duration
                    }
                }

                // Update the bot's pose based on the current segment
                pose = when (segment) {
                    is WaitSegment -> segment.startPose
                    is TurnSegment -> segment.startPose.copy(heading = segment.motionProfile[segmentOffsetTime].x)
                    is TrajectorySegment -> segment.trajectory[segmentOffsetTime]
                    else -> currentTrajectorySequence!!.end()
                }

                drive.poseEstimate = pose

                // Update marker entities
                trajectorySequenceEntity!!.markerEntityList.forEach { if (trajectorySequenceElapsedTime >= it.time) it.passed() }

                // Report progress to the slider master
                sliderMaster?.reportProgress(sliderMasterIndex ?: -1, trajectorySequenceElapsedTime)

                Unit
            }

            looping -> {
                // Reset markers and elapsed time for looping
                trajectorySequenceEntity!!.markerEntityList.forEach { it.reset() }
                trajectorySequenceElapsedTime = 0.0

                sliderMaster?.reportDone(sliderMasterIndex ?: -1)
            }

            else -> {
                // Stop running when the sequence is done
                trajectorySequenceElapsedTime = 0.0
                isExcutingTrajectory = false
                sliderMaster?.reportDone(sliderMasterIndex ?: -1)
            }
        }.exhaustive
    }

    /** Starts the trajectory sequence. */
    fun start() {
        isExcutingTrajectory = true
        trajectorySequenceElapsedTime = 0.0
    }

    /** Resumes the trajectory sequence. */
    fun resume() {
        isExcutingTrajectory = true
    }

    /** Pauses the trajectory sequence. */
    fun pause() {
        isTrajectoryPaused = true
    }

    /** Unpauses the trajectory sequence. */
    fun unpause() {
        isTrajectoryPaused = false
    }

    /**
     * Sets the trajectory progress in seconds.
     *
     * @param seconds The progress time in seconds.
     */
    fun setTrajectoryProgressSeconds(seconds: Double) {
        if (currentTrajectorySequence != null)
            trajectorySequenceElapsedTime = min(seconds, currentTrajectorySequence!!.duration())
    }

    /**
     * Follows the given trajectory sequence.
     *
     * @param sequence The trajectory sequence to follow.
     */
    fun followTrajectorySequence(sequence: TrajectorySequence) {
        currentTrajectorySequence = sequence

        trajectorySequenceEntity = TrajectorySequenceEntity(meepMeep, sequence, colorScheme)
    }

    /**
     * Sets the motion constraints for the bot.
     *
     * @param constraints The new motion constraints.
     */
    fun setConstraints(constraints: Constraints) {
        this.constraints = constraints

        drive = DriveShim(driveTrainType, constraints, pose)
    }

    /**
     * Sets the drivetrain type for the bot.
     *
     * @param driveTrainType The new drivetrain type.
     */
    fun setDriveTrainType(driveTrainType: DriveTrainType) {
        this.driveTrainType = driveTrainType

        drive = DriveShim(driveTrainType, constraints, pose)
    }

    /**
     * Switches the color scheme of the bot.
     *
     * @param scheme The new color scheme.
     */
    override fun switchScheme(scheme: ColorScheme) {
        if (listenToSwitchThemeRequest)
            super.switchScheme(scheme)
    }

    /**
     * Sets the trajectory progress slider master.
     *
     * @param master The slider master.
     * @param index The index of the slider master.
     */
    fun setTrajectoryProgressSliderMaster(master: TrajectoryProgressSliderMaster, index: Int) {
        sliderMaster = master
        sliderMasterIndex = index
    }

    /** Called when the bot is added to the entity list. */
    override fun onAddToEntityList() {
        if (trajectorySequenceEntity != null)
            meepMeep.requestToAddEntity(trajectorySequenceEntity!!)
    }

    /** Called when the bot is removed from the entity list. */
    override fun onRemoveFromEntityList() {
        if (trajectorySequenceEntity != null)
            meepMeep.requestToRemoveEntity(trajectorySequenceEntity!!)
    }
}