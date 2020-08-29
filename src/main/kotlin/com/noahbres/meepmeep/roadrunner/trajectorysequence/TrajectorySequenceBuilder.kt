package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.PathContinuityViolationException
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.acmerobotics.roadrunner.trajectory.*
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryConstraints
import com.acmerobotics.roadrunner.util.Angle
import com.acmerobotics.roadrunner.util.epsilonEquals
import kotlin.math.PI

class TrajectorySequenceBuilder(
        startPose: Pose2d,
        startTangent: Double,
        private val baseConstraints: DriveConstraints,
        private val resolution: Double = 0.25
) {

    @JvmOverloads
    constructor(
            startPose: Pose2d,
            baseConstraints: DriveConstraints,
            resolution: Double = 0.25
    ) : this(startPose, startPose.heading, baseConstraints, resolution)

    @JvmOverloads
    constructor(
            startPose: Pose2d,
            reversed: Boolean,
            baseConstraints: DriveConstraints,
            resolution: Double = 0.25
    ) : this(startPose, Angle.norm(startPose.heading + if (reversed) PI else 0.0), baseConstraints, resolution)

    private val sequenceSegments = mutableListOf<SequenceSegment>()

    private val temporalMarkers = mutableListOf<TemporalMarker>()
    private val displacementMarkers = mutableListOf<DisplacementMarker>()
    private val spatialMarkers = mutableListOf<SpatialMarker>()

    private var lastPose = startPose
    private var lastTangent = startTangent

    private var tangentOffset = 0.0

    private var currentTrajectoryBuilder: TrajectoryBuilder? = null

    private var currentDuration = 0.0

    private var currentConstraints = baseConstraints

    @JvmOverloads
    fun lineTo(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.lineTo(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun lineToConstantHeading(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.lineToConstantHeading(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun lineToLinearHeading(endPose: Pose2d, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.lineToLinearHeading(endPose, constraintsOverride)
    }

    @JvmOverloads
    fun lineToSplineHeading(endPose: Pose2d, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.lineToSplineHeading(endPose, constraintsOverride)
    }

    @JvmOverloads
    fun strafeTo(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.strafeTo(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun forward(distance: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.forward(distance, constraintsOverride)
    }

    @JvmOverloads
    fun back(distance: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.back(distance, constraintsOverride)
    }

    @JvmOverloads
    fun strafeLeft(distance: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.strafeLeft(distance, constraintsOverride)
    }

    @JvmOverloads
    fun strafeRight(distance: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.strafeRight(distance, constraintsOverride)
    }

    @JvmOverloads
    fun splineTo(endPosition: Vector2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.splineTo(endPosition, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToConstantHeading(endPosition: Vector2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.splineToConstantHeading(endPosition, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToLinearHeading(endPose: Pose2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
        currentTrajectoryBuilder!!.splineToLinearHeading(endPose, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToSplineHeading(endPose: Pose2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = currentConstraints) = addPath {
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

        return this
    }

    fun setTangentOffset(offset: Double): TrajectorySequenceBuilder {
        this.tangentOffset = offset

        pushPath()

        return this
    }

    fun setReversed(reversed: Boolean): TrajectorySequenceBuilder {
        return if (reversed) setTangentOffset(Math.toRadians(180.0)) else setTangentOffset(0.0)
    }

    fun setConstraints(constraints: DriveConstraints): TrajectorySequenceBuilder {
        currentConstraints = constraints

        return this
    }

    fun resetConstraints(constraints: DriveConstraints): TrajectorySequenceBuilder {
        currentConstraints = baseConstraints

        return this
    }

    fun addTemporalMarker(callback: MarkerCallback) = addTemporalMarker(currentDuration, callback)

    fun addTemporalMarker(time: Double, callback: MarkerCallback) = addTemporalMarker(0.0, time, callback)

    fun addTemporalMarker(scale: Double, offset: Double, callback: MarkerCallback) = addTemporalMarker({ scale * it + offset }, callback)

    fun addTemporalMarker(time: (Double) -> Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        temporalMarkers.add(TemporalMarker(time, callback))

        return this
    }

    fun addSpatialMarker(point: Vector2d, callback: MarkerCallback): TrajectorySequenceBuilder {
        spatialMarkers.add(SpatialMarker(point, callback))

        return this
    }

    fun addDisplacementMarker(callback: MarkerCallback): TrajectorySequenceBuilder = addDisplacementMarker(sequenceTotalDisplacement(sequenceSegments), callback)

    fun addDisplacementMarker(displacement: Double, callback: MarkerCallback) = addDisplacementMarker(0.0, displacement, callback)

    fun addDisplacementMarker(scale: Double, offset: Double, callback: MarkerCallback) = addDisplacementMarker({ scale * it + offset }, callback)

    fun addDisplacementMarker(displacement: (Double) -> Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        displacementMarkers.add(DisplacementMarker(displacement, callback))

        return this
    }

    fun turn(angle: Double): TrajectorySequenceBuilder {
        pushPath()
        val turnProfile = MotionProfileGenerator.generateSimpleMotionProfile(
                MotionState(lastPose.heading, 0.0, 0.0, 0.0),
                MotionState(lastPose.heading + angle, 0.0, 0.0, 0.0),
                currentConstraints.maxAngVel,
                currentConstraints.maxAngAccel,
                currentConstraints.maxAngJerk
        )

        sequenceSegments.add(TurnSegment(lastPose, angle, turnProfile, turnProfile.duration()))

        lastPose = lastPose.copy(heading = lastPose.heading + angle)

        currentDuration += turnProfile.duration()
        return this
    }

    fun waitSeconds(seconds: Double): TrajectorySequenceBuilder {
        pushPath()
        sequenceSegments.add(WaitSegment(lastPose, seconds))

        currentDuration += seconds
        return this
    }

    private fun pushPath() {
        if (currentTrajectoryBuilder != null) {
            val builtTraj = currentTrajectoryBuilder!!.build()
            sequenceSegments.add(
                    TrajectorySegment(builtTraj, builtTraj.duration())
            )

            currentDuration += builtTraj.duration()
        }

        currentTrajectoryBuilder = null
    }

    private fun newPath() {
        if (currentTrajectoryBuilder != null)
            pushPath()

        currentTrajectoryBuilder = TrajectoryBuilder(lastPose, Angle.norm(lastPose.heading + tangentOffset), currentConstraints)
    }

    fun build(): TrajectorySequence {
        pushPath()

        // TODO: CONVERT MARKERS TO GLOBAL

        return TrajectorySequence(
                sequenceSegments, currentDuration,
                convertMarkers(
                        sequenceSegments,
                        temporalMarkers, displacementMarkers, spatialMarkers
                )
        )
    }

    // Marker conversion
    private fun convertMarkers(
            sequenceSegments: List<SequenceSegment>,
            temporalMarkers: List<TemporalMarker>,
            displacementMarkers: List<DisplacementMarker>,
            spatialMarkers: List<SpatialMarker>
    ): List<TrajectoryMarker> {
        return temporalMarkers.map { (time, callback) ->
            TrajectoryMarker(time(currentDuration), callback)
        } + displacementMarkers.map { (displacement, callback) ->
            TrajectoryMarker(displacementToTime(sequenceSegments, displacement(sequenceTotalDisplacement(sequenceSegments))), callback)
        }
    }

    private fun sequenceTotalDisplacement(sequenceSegments: List<SequenceSegment>): Double {
        return sequenceSegments
                .filterIsInstance<TrajectorySegment>()
                .fold(0.0) { displacementTotal, segment ->
                    displacementTotal + segment.trajectory.path.length()
                }
    }

    private fun displacementToTime(sequenceSegments: List<SequenceSegment>, s: Double): Double {
        // Taken from Road Runner's TrajectoryGenerator.displacementToTime() since it's private
        // note: this assumes that the profile position is monotonic increasing
        fun motionProfileDisplacementToTime(profile: MotionProfile, s: Double): Double {
            var tLo = 0.0
            var tHi = profile.duration()
            while (!(tLo epsilonEquals tHi)) {
                val tMid = 0.5 * (tLo + tHi)
                if (profile[tMid].x > s) {
                    tHi = tMid
                } else {
                    tLo = tMid
                }
            }
            return 0.5 * (tLo + tHi)
        }

        var currentTime = 0.0
        var currentDisplacement = 0.0

        sequenceSegments.forEach {
            if (it is TrajectorySegment) {
                val segmentLength = it.trajectory.path.length()
                if (currentDisplacement + segmentLength > s) {
                    val target = s - currentDisplacement
                    val timeInSegment = motionProfileDisplacementToTime(it.trajectory.profile, target)

                    return currentTime + timeInSegment
                } else {
                    currentDisplacement += segmentLength
                }
            } else {
                currentTime += it.duration
            }
        }

        return 0.0
    }
}