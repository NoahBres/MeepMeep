package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker

class TrajectorySequence(
        val sequenceSegments: List<SequenceSegment>,
        val duration: Double,
        val markers: List<TrajectoryMarker> = emptyList()
) {
    operator fun get(time: Double): Pose2d {
        val (currentSegment, segmentTime) = getCurrentState(time)

        return when (currentSegment) {
            is TrajectorySegment -> currentSegment.trajectory[segmentTime]
            is TurnSegment -> currentSegment.startPose.copy(heading = currentSegment.motionProfile[segmentTime].x)
            is WaitSegment -> currentSegment.pose
            null -> Pose2d()
        }
    }

    fun velocity(time: Double): Pose2d {
        val (currentSegment, segmentTime) = getCurrentState(time)

        return when (currentSegment) {
            is TrajectorySegment -> currentSegment.trajectory.velocity(segmentTime)
            is TurnSegment -> Pose2d(0.0, 0.0, currentSegment.motionProfile[segmentTime].v)
            is WaitSegment -> Pose2d()
            null -> Pose2d()
        }
    }

    fun acceleration(time: Double): Pose2d {
        val (currentSegment, segmentTime) = getCurrentState(time)

        return when (currentSegment) {
            is TrajectorySegment -> currentSegment.trajectory.acceleration(segmentTime)
            is TurnSegment -> Pose2d(0.0, 0.0, currentSegment.motionProfile[segmentTime].a)
            is WaitSegment -> Pose2d()
            null -> Pose2d()
        }
    }

    private fun getCurrentState(time: Double): Pair<SequenceSegment?, Double> {
        var currentTime = 0.0

        sequenceSegments.forEach {
            if (currentTime + it.duration > time) {
                val segmentTime = time - currentTime

                return Pair(it, segmentTime)
            } else {
                currentTime += it.duration
            }
        }

        return Pair(null, 0.0)
    }

    fun start() = get(0.0)

    fun end() = get(duration)
}