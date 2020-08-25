package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.PathContinuityViolationException
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.acmerobotics.roadrunner.trajectory.MarkerCallback
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryConstraints
import com.noahbres.meepmeep.core.exhaustive

class TrajectorySequenceBuilder(startPose: Pose2d, private val baseConstraints: DriveConstraints) {
    private val trajectorySequence = TrajectorySequence()

    private var currentTrajectoryBuilder: TrajectoryBuilder? = null

    private var reversed = false
    private var lastPose = startPose
    private var lastHeading = startPose.heading

    private var currentDuration = 0.0

    private val markerBuffer = mutableListOf<BufferedMarker>()

    @JvmOverloads
    fun lineTo(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.lineTo(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun lineToConstantHeading(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.lineToConstantHeading(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun lineToLinearHeading(endPose: Pose2d, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.lineToLinearHeading(endPose, constraintsOverride)
    }

    @JvmOverloads
    fun lineToSplineHeading(endPose: Pose2d, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.lineToSplineHeading(endPose, constraintsOverride)
    }

    @JvmOverloads
    fun strafeTo(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.strafeTo(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun forward(distance: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.forward(distance, constraintsOverride)
    }

    @JvmOverloads
    fun back(distance: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.back(distance, constraintsOverride)
    }

    @JvmOverloads
    fun strafeLeft(distance: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.strafeLeft(distance, constraintsOverride)
    }

    @JvmOverloads
    fun strafeRight(distance: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.strafeRight(distance, constraintsOverride)
    }

    @JvmOverloads
    fun splineTo(endPosition: Vector2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.splineTo(endPosition, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToConstantHeading(endPosition: Vector2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.splineToConstantHeading(endPosition, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToLinearHeading(endPose: Pose2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.splineToLinearHeading(endPose, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToSplineHeading(endPose: Pose2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = baseConstraints) = addPath {
        currentTrajectoryBuilder!!.splineToSplineHeading(endPose, endHeading, constraintsOverride)
    }

    private fun addPath(f: () -> Unit): TrajectorySequenceBuilder {
        if (currentTrajectoryBuilder == null) newPath()

        try {
            f()
        } catch (e: PathContinuityViolationException) {
            newPath()
            f()
        }

        lastPose = currentTrajectoryBuilder!!.build().end()
        lastHeading = lastPose.heading

        return this
    }

    fun addTemporalMarker(time: Double, callback: MarkerCallback) = addTemporalMarker(0.0, time, callback)

    fun addTemporalMarker(scale: Double, offset: Double, callback: MarkerCallback) = addTemporalMarker({ scale * it + offset }, callback)

    fun addTemporalMarker(time: (Double) -> Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        if (currentTrajectoryBuilder == null) markerBuffer.add(
                BufferedTemporalMarker(
                        time, callback
                )
        )
        else currentTrajectoryBuilder!!.addTemporalMarker(time, callback)

        return this
    }

    fun addSpatialMarker(point: Vector2d, callback: MarkerCallback): TrajectorySequenceBuilder {
        if (currentTrajectoryBuilder == null) markerBuffer.add(
                BufferedSpatialMarker(
                        point, callback
                )
        )
        else currentTrajectoryBuilder!!.addSpatialMarker(point, callback)

        return this
    }

    fun addDisplacementMarker(callback: MarkerCallback): TrajectorySequenceBuilder {
        if (currentTrajectoryBuilder == null) markerBuffer.add(
                BufferedDisplacementMarker(
                        { 0.0 }, callback)
        )
        else currentTrajectoryBuilder!!.addDisplacementMarker(callback)

        return this
    }

    fun addDisplacementMarker(displacement: Double, callback: MarkerCallback) = addDisplacementMarker(0.0, displacement, callback)

    fun addDisplacementMarker(scale: Double, offset: Double, callback: MarkerCallback) = addDisplacementMarker({ scale * it + offset }, callback)

    fun addDisplacementMarker(displacement: (Double) -> Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        if (currentTrajectoryBuilder == null) markerBuffer.add(
                BufferedDisplacementMarker(
                        displacement, callback
                )
        )
        else currentTrajectoryBuilder!!.addDisplacementMarker(displacement, callback)

        return this
    }

    fun turn(angle: Double): TrajectorySequenceBuilder {

        val turnProfile = MotionProfileGenerator.generateSimpleMotionProfile(
                MotionState(lastHeading, 0.0, 0.0, 0.0),
                MotionState(lastHeading + angle, 0.0, 0.0, 0.0),
                baseConstraints.maxAngVel,
                baseConstraints.maxAngAccel,
                baseConstraints.maxAngJerk
        )

        pushPath()
        trajectorySequence.add(
                TurnStep(
                        lastPose.vec(), angle, turnProfile, currentDuration, turnProfile.duration()
                )
        )

        currentDuration += turnProfile.duration()

        lastHeading += angle

        return this
    }

    fun waitSeconds(seconds: Double): TrajectorySequenceBuilder {
        pushPath()
        trajectorySequence.add(
                WaitStep(
                        seconds, currentDuration, seconds
                )
        )
        currentDuration += seconds

        return this
    }

    fun waitFor(callback: WaitCallback): TrajectorySequenceBuilder {
        pushPath()
        trajectorySequence.add(
                WaitConditionalStep(
                        callback, currentDuration, 0.0
                )
        )
        currentDuration += 0.0

        return this
    }

    fun setReversed(reversed: Boolean): TrajectorySequenceBuilder {
        this.reversed = reversed
        pushPath()

        return this
    }

    fun build(): TrajectorySequence {
        pushPath()

        return trajectorySequence
    }

    private fun pushPath() {
        if (currentTrajectoryBuilder != null) {
            markerBuffer.forEach {
                when (it) {
                    is BufferedDisplacementMarker -> currentTrajectoryBuilder!!.addDisplacementMarker(it.displacement, it.callback)
                    is BufferedSpatialMarker -> currentTrajectoryBuilder!!.addSpatialMarker(it.point, it.callback)
                    is BufferedTemporalMarker -> currentTrajectoryBuilder!!.addTemporalMarker(it.time, it.callback)
                }.exhaustive
            }
            markerBuffer.clear()

            val builtTraj = currentTrajectoryBuilder!!.build()
            trajectorySequence.add(
                    TrajectoryStep(
                            builtTraj, currentDuration, builtTraj.duration()
                    )
            )
            currentDuration += builtTraj.duration()
        }

        currentTrajectoryBuilder = null
    }

    private fun newPath() {
        if (currentTrajectoryBuilder != null) {
            pushPath()
        }

        lastPose = Pose2d(lastPose.x, lastPose.y, lastHeading)

        currentTrajectoryBuilder = TrajectoryBuilder(lastPose, reversed, baseConstraints)
    }
}