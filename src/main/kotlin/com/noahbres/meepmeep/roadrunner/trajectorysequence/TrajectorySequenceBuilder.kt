package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.path.PathContinuityViolationException
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator
import com.acmerobotics.roadrunner.profile.MotionState
import com.acmerobotics.roadrunner.trajectory.*
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint
import com.acmerobotics.roadrunner.util.Angle
import com.acmerobotics.roadrunner.util.epsilonEquals
import com.noahbres.meepmeep.roadrunner.Constraints
import kotlin.math.min

class TrajectorySequenceBuilder(
    startPose: Pose2d,
    startTangent: Double?,
    private var velConstraint: TrajectoryVelocityConstraint,
    private var accelConstraint: TrajectoryAccelerationConstraint,
    private var constraints: Constraints,

    private val resolution: Double = 0.25,
) {

    @JvmOverloads
    constructor(
        startPose: Pose2d,
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint,
        constraints: Constraints,

        resolution: Double = 0.25
    ) : this(startPose, null, velConstraint, accelConstraint, constraints, resolution)

    private val sequenceSegments = mutableListOf<SequenceSegment>()

    private val temporalMarkers = mutableListOf<TemporalMarker>()
    private val displacementMarkers = mutableListOf<DisplacementMarker>()
    private val spatialMarkers = mutableListOf<SpatialMarker>()

    private var lastPose = startPose

    private var tangentOffset = 0.0

    private var setAbsoluteTangent = startTangent != null
    private var absoluteTangent = startTangent ?: 0.0

    private var currentTrajectoryBuilder: TrajectoryBuilder? = null

    private var currentDuration = 0.0
    private var currentDisplacement = 0.0

    private var lastDurationTraj = 0.0
    private var lastDisplacementTraj = 0.0

    @JvmOverloads
    fun lineTo(
        endPosition: Vector2d,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.lineTo(endPosition, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun lineToConstantHeading(
        endPosition: Vector2d,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) =
        addPath {
            currentTrajectoryBuilder!!.lineToConstantHeading(endPosition, velConstraint, accelConstraint)
        }

    @JvmOverloads
    fun lineToLinearHeading(
        endPose: Pose2d,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.lineToLinearHeading(endPose, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun lineToSplineHeading(
        endPose: Pose2d,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.lineToSplineHeading(endPose, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun strafeTo(
        endPosition: Vector2d,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.strafeTo(endPosition, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun forward(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.forward(distance, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun back(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.back(distance, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun strafeLeft(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.strafeLeft(distance, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun strafeRight(
        distance: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.strafeRight(distance, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun splineTo(
        endPosition: Vector2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) =
        addPath {
            currentTrajectoryBuilder!!.splineTo(endPosition, endHeading, velConstraint, accelConstraint)
        }

    @JvmOverloads
    fun splineToConstantHeading(
        endPosition: Vector2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.splineToConstantHeading(endPosition, endHeading, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun splineToLinearHeading(
        endPose: Pose2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.splineToLinearHeading(endPose, endHeading, velConstraint, accelConstraint)
    }

    @JvmOverloads
    fun splineToSplineHeading(
        endPose: Pose2d,
        endHeading: Double,
        velConstraint: TrajectoryVelocityConstraint = this.velConstraint,
        accelConstraint: TrajectoryAccelerationConstraint = this.accelConstraint
    ) = addPath {
        currentTrajectoryBuilder!!.splineToSplineHeading(endPose, endHeading, velConstraint, accelConstraint)
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

        val durationDifference = builtTraj.duration() - lastDurationTraj
        val displacementDifference = builtTraj.path.length() - lastDisplacementTraj

        lastPose = builtTraj.end()
        currentDuration += durationDifference
        currentDisplacement += displacementDifference

        lastDurationTraj = builtTraj.duration()
        lastDisplacementTraj = builtTraj.path.length()

        return this
    }

    fun setTangent(tangent: Double): TrajectorySequenceBuilder {
        setAbsoluteTangent = true
        absoluteTangent = tangent

        pushPath()

        return this
    }

    fun setTangentOffset(offset: Double): TrajectorySequenceBuilder {
        setAbsoluteTangent = false

        this.tangentOffset = offset

        pushPath()

        return this
    }

    fun setReversed(reversed: Boolean): TrajectorySequenceBuilder {
        return if (reversed) setTangentOffset(Math.toRadians(180.0)) else setTangentOffset(0.0)
    }

    fun setConstraints(
        velConstraint: TrajectoryVelocityConstraint,
        accelConstraint: TrajectoryAccelerationConstraint
    ): TrajectorySequenceBuilder {
        this.velConstraint = velConstraint
        this.accelConstraint = accelConstraint

        return this
    }

    fun addTemporalMarker(callback: MarkerCallback) = addTemporalMarker(currentDuration, callback)

    fun UNSTABLE_addTemporalMarkerOffset(offset: Double, callback: MarkerCallback) =
        addTemporalMarker(currentDuration + offset, callback)

    fun addTemporalMarker(time: Double, callback: MarkerCallback) = addTemporalMarker(0.0, time, callback)

    fun addTemporalMarker(scale: Double, offset: Double, callback: MarkerCallback) =
        addTemporalMarker({ scale * it + offset }, callback)

    fun addTemporalMarker(time: (Double) -> Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        temporalMarkers.add(TemporalMarker(time, callback))

        return this
    }

    fun addSpatialMarker(point: Vector2d, callback: MarkerCallback): TrajectorySequenceBuilder {
        spatialMarkers.add(SpatialMarker(point, callback))

        return this
    }

    fun addDisplacementMarker(callback: MarkerCallback): TrajectorySequenceBuilder =
        addDisplacementMarker(currentDisplacement, callback)

    fun UNSTABLE_addDisplacementMarkerOffset(offset: Double, callback: MarkerCallback) =
        addDisplacementMarker(currentDisplacement + offset, callback)

    fun addDisplacementMarker(displacement: Double, callback: MarkerCallback) =
        addDisplacementMarker(0.0, displacement, callback)

    fun addDisplacementMarker(scale: Double, offset: Double, callback: MarkerCallback) =
        addDisplacementMarker({ scale * it + offset }, callback)

    fun addDisplacementMarker(displacement: (Double) -> Double, callback: MarkerCallback): TrajectorySequenceBuilder {
        displacementMarkers.add(DisplacementMarker(displacement, callback))

        return this
    }

    fun turn(angle: Double): TrajectorySequenceBuilder {
        pushPath()

        val turnProfile = MotionProfileGenerator.generateSimpleMotionProfile(
            MotionState(lastPose.heading, 0.0, 0.0, 0.0),
            MotionState(lastPose.heading + angle, 0.0, 0.0, 0.0),
            constraints.maxVel,
            constraints.maxAngAccel,
        )

        sequenceSegments.add(TurnSegment(lastPose, angle, turnProfile, emptyList()))

        lastPose = lastPose.copy(heading = lastPose.heading + angle)

        currentDuration += turnProfile.duration()
        return this
    }

    fun waitSeconds(seconds: Double): TrajectorySequenceBuilder {
        pushPath()
        sequenceSegments.add(WaitSegment(lastPose, seconds, emptyList()))

        currentDuration += seconds
        return this
    }

    fun addTrajectory(trajectory: Trajectory): TrajectorySequenceBuilder {
        pushPath()

        sequenceSegments.add(TrajectorySegment(trajectory))
        return this
    }

    private fun pushPath() {
        if (currentTrajectoryBuilder != null)
            sequenceSegments.add(TrajectorySegment(currentTrajectoryBuilder!!.build()))

        currentTrajectoryBuilder = null
    }

    private fun newPath() {
        if (currentTrajectoryBuilder != null)
            pushPath()

        lastDurationTraj = 0.0
        lastDisplacementTraj = 0.0

        val tangent = if (setAbsoluteTangent) absoluteTangent else Angle.norm(lastPose.heading + tangentOffset)

        currentTrajectoryBuilder = TrajectoryBuilder(lastPose, tangent, velConstraint, accelConstraint, resolution)
    }

    fun build(): TrajectorySequence {
        pushPath()

        val globalMarkers = convertMarkersToGlobal(
            sequenceSegments,
            temporalMarkers, displacementMarkers, spatialMarkers
        )

        return projectGlobalMarkersToLocalSegments(globalMarkers, sequenceSegments)
    }

    // Marker conversion
    private fun convertMarkersToGlobal(
        sequenceSegments: TrajectorySequence,
        temporalMarkers: List<TemporalMarker>,
        displacementMarkers: List<DisplacementMarker>,
        spatialMarkers: List<SpatialMarker>
    ): List<TrajectoryMarker> {
        return temporalMarkers.map { (time, callback) ->
            TrajectoryMarker(time.produce(currentDuration), callback)
        } + displacementMarkers.map { (displacement, callback) ->
            TrajectoryMarker(
                displacementToTime(
                    sequenceSegments,
                    displacement.produce(sequenceTotalDisplacement(sequenceSegments))
                ), callback
            )
        } + spatialMarkers.map { (point, callback) -> TrajectoryMarker(pointToTime(sequenceSegments, point), callback) }
    }

    private fun projectGlobalMarkersToLocalSegments(
        markers: List<TrajectoryMarker>,
        sequenceSegments: TrajectorySequence
    ): TrajectorySequence {
        val newSegmentList = sequenceSegments.toMutableList()

        if (sequenceSegments.isEmpty()) return emptyList()

        markers.forEach {
            var segment: SequenceSegment? = null
            var segmentIndex = 0
            var segmentOffsetTime = 0.0

            var currentTime = 0.0
            for (index in sequenceSegments.indices) {
                val seg = sequenceSegments[index]

                val markerTime = min(it.time, sequenceSegments.duration())

                if (currentTime + seg.duration >= markerTime) {
                    segment = seg
                    segmentIndex = index
                    segmentOffsetTime = markerTime - currentTime

                    break
                } else {
                    currentTime += seg.duration
                }
            }

            val newSegment = when (segment) {
                is WaitSegment -> {
                    val newMarkers = segment.markers.toMutableList()
                    newMarkers.add(TrajectoryMarker(segmentOffsetTime, it.callback))

                    segment.copy(markers = newMarkers)
                }
                is TurnSegment -> {
                    val newMarkers = segment.markers.toMutableList()
                    newMarkers.add(TrajectoryMarker(segmentOffsetTime, it.callback))

                    segment.copy(markers = newMarkers)
                }
                is TrajectorySegment -> {
                    val newMarkers =
                        (newSegmentList[segmentIndex] as TrajectorySegment).trajectory.markers.toMutableList()
                    newMarkers.add(TrajectoryMarker(segmentOffsetTime, it.callback))

                    TrajectorySegment(Trajectory(segment.trajectory.path, segment.trajectory.profile, newMarkers))
                }
                else -> WaitSegment(Pose2d(), 0.0, emptyList())
            }

            newSegmentList[segmentIndex] = newSegment
        }

        return newSegmentList
    }

    private fun sequenceTotalDisplacement(sequenceSegments: TrajectorySequence): Double {
        return sequenceSegments
            .filterIsInstance<TrajectorySegment>()
            .sumByDouble { it.trajectory.path.length() }
    }

    private fun displacementToTime(sequenceSegments: TrajectorySequence, s: Double): Double {
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

    private fun pointToTime(sequenceSegments: TrajectorySequence, point: Vector2d): Double {
        data class ComparingPoints(
            val distanceToPoint: Double,
            val totalDisplacement: Double,
            val thisPathDisplacement: Double
        )

        val justTrajectories = sequenceSegments.filterIsInstance<TrajectorySegment>()

        val projectedPoints = justTrajectories.fold(listOf<ComparingPoints>()) { segmentList, it ->
            val displacement = it.trajectory.path.project(point)
            val projectedPoint = it.trajectory.path[displacement].vec()
            val distanceToPoint = (point - projectedPoint).norm()

            val totalDisplacement = segmentList.sumByDouble { it.totalDisplacement } + displacement

            segmentList + ComparingPoints(distanceToPoint, displacement, totalDisplacement)
        }

        val closestPoint = projectedPoints.minByOrNull { it.distanceToPoint }

        return displacementToTime(sequenceSegments, closestPoint!!.thisPathDisplacement)
    }
}