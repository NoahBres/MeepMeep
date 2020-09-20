package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.trajectory.Trajectory
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker

sealed class SequenceSegment(
        open val duration: Double,
        open val startPose: Pose2d,
        open val endPose: Pose2d,
        open val markers: List<TrajectoryMarker>
)

data class TrajectorySegment(
        val trajectory: Trajectory,
) : SequenceSegment(trajectory.duration(), trajectory.start(), trajectory.end(), emptyList())

data class TurnSegment(
        override val startPose: Pose2d,
        val totalRotation: Double,
        val motionProfile: MotionProfile,
        override val markers: List<TrajectoryMarker>
) : SequenceSegment(
        motionProfile.duration(),
        startPose,
        startPose.copy(heading = startPose.heading + totalRotation),
        markers
)

data class WaitSegment(
        private val pose: Pose2d,
        private val seconds: Double,
        override val markers: List<TrajectoryMarker>
) : SequenceSegment(seconds, pose, pose, markers)
