package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumConstraints
import com.acmerobotics.roadrunner.trajectory.constraints.TankConstraints

class DriveShim(driveTrainType: DriveTrainType, trajectoryConstraints: DriveConstraints, trackWidth: Double) {
    private val constraints = when (driveTrainType) {
        DriveTrainType.MECANUM -> MecanumConstraints(trajectoryConstraints, trackWidth)
        DriveTrainType.TANK -> TankConstraints(trajectoryConstraints, trackWidth)
    }

    fun trajectorySequenceBuilder(startPose: Pose2d): TrajectorySequenceBuilder {
        return TrajectorySequenceBuilder(startPose, constraints)
    }

    @JvmOverloads
    fun trajectoryBuilder(startPose: Pose2d, startHeading: Double = startPose.heading): TrajectoryBuilder {
        return TrajectoryBuilder(startPose, startHeading, constraints)
    }

    fun trajectoryBuilder(startPose: Pose2d, reversed: Boolean): TrajectoryBuilder {
        return TrajectoryBuilder(startPose, reversed, constraints)
    }
}