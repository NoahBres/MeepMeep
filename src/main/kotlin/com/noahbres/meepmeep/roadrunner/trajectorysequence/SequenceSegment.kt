package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.profile.MotionProfile
import com.acmerobotics.roadrunner.trajectory.Trajectory

sealed class SequenceSegment(open val duration: Double)

data class TrajectorySegment(
        val trajectory: Trajectory,
        override val duration: Double
) : SequenceSegment(duration)

data class TurnSegment(
        val startPose: Pose2d,
        val totalRotation: Double,
        val motionProfile: MotionProfile,
        override val duration: Double
) : SequenceSegment(duration)

data class WaitSegment(
        val pose: Pose2d,
        val seconds: Double
) : SequenceSegment(seconds)
