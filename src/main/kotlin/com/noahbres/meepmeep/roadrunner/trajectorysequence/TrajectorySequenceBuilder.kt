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
import kotlin.math.hypot

class TrajectorySequenceBuilder(
        startPose: Pose2d,
        startTangent: Double,
        private var constraints: DriveConstraints,
        private val resolution: Double = 0.25
) {

    @JvmOverloads
    constructor(
            startPose: Pose2d,
            constraints: DriveConstraints,
            resolution: Double = 0.25
    ) : this(startPose, startPose.heading, constraints, resolution)
    
    private val sequenceSegments = mutableListOf<SequenceSegment>()

    private val temporalMarkers = mutableListOf<TemporalMarker>()
    private val displacementMarkers = mutableListOf<DisplacementMarker>()
    private val spatialMarkers = mutableListOf<SpatialMarker>()

    private var lastPose = startPose
    private var lastTangent = startTangent

    private var tangentOffset = 0.0

    private var currentTrajectoryBuilder: TrajectoryBuilder? = null

    private var currentDuration = 0.0
    private var currentDisplacement = 0.0

    @JvmOverloads
    fun lineTo(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.lineTo(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun lineToConstantHeading(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.lineToConstantHeading(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun lineToLinearHeading(endPose: Pose2d, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.lineToLinearHeading(endPose, constraintsOverride)
    }

    @JvmOverloads
    fun lineToSplineHeading(endPose: Pose2d, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.lineToSplineHeading(endPose, constraintsOverride)
    }

    @JvmOverloads
    fun strafeTo(endPosition: Vector2d, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.strafeTo(endPosition, constraintsOverride)
    }

    @JvmOverloads
    fun forward(distance: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.forward(distance, constraintsOverride)
    }

    @JvmOverloads
    fun back(distance: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.back(distance, constraintsOverride)
    }

    @JvmOverloads
    fun strafeLeft(distance: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.strafeLeft(distance, constraintsOverride)
    }

    @JvmOverloads
    fun strafeRight(distance: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.strafeRight(distance, constraintsOverride)
    }

    @JvmOverloads
    fun splineTo(endPosition: Vector2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.splineTo(endPosition, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToConstantHeading(endPosition: Vector2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.splineToConstantHeading(endPosition, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToLinearHeading(endPose: Pose2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
        currentTrajectoryBuilder!!.splineToLinearHeading(endPose, endHeading, constraintsOverride)
    }

    @JvmOverloads
    fun splineToSplineHeading(endPose: Pose2d, endHeading: Double, constraintsOverride: TrajectoryConstraints = constraints) = addPath {
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

        val builtTraj = currentTrajectoryBuilder!!.build()

        lastPose = builtTraj.end()
        currentDuration += builtTraj.duration()
        currentDisplacement += builtTraj.path.length()

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
        this.constraints = constraints

        return this
    }

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

    fun addDisplacementMarker(callback: MarkerCallback): TrajectorySequenceBuilder = addDisplacementMarker(currentDisplacement, callback)

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
                constraints.maxAngVel,
                constraints.maxAngAccel,
                constraints.maxAngJerk
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
        }

        currentTrajectoryBuilder = null
    }

    private fun newPath() {
        if (currentTrajectoryBuilder != null)
            pushPath()

        currentTrajectoryBuilder = TrajectoryBuilder(lastPose, Angle.norm(lastPose.heading + tangentOffset), constraints)
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
        } + spatialMarkers.map { (point, callback) -> TrajectoryMarker(pointToTime(sequenceSegments, point), callback) }
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
                    currentTime += it.trajectory.duration()
                }
            } else {
                currentTime += it.duration
            }
        }

        return 0.0
    }

    private fun pointToTime(sequenceSegments: List<SequenceSegment>, point: Vector2d): Double {
        val justTrajectories = sequenceSegments.filterIsInstance<TrajectorySegment>()

        val projectedPoints = justTrajectories.fold(listOf<Triple<Double, Double, Double>>()) { segmentList, it ->
            val displacement = it.trajectory.path.project(point)
            val projectedPoint = it.trajectory.path[displacement].vec()
            val distanceToPoint = hypot((point - projectedPoint).x, (point - projectedPoint).y)

            val totalDisplacement = segmentList.fold(0.0) { acc, segment ->
                acc + segment.second
            } + displacement

            segmentList + Triple(distanceToPoint, displacement, totalDisplacement)
        }

        val closestPoint = projectedPoints.minByOrNull { it.first }

        return displacementToTime(sequenceSegments, closestPoint!!.third)
    }
}