package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TankVelocityConstraint
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequenceBuilder

/**
 * A shim class for the drive system, providing velocity and acceleration
 * constraints based on the type of drive train and the given constraints.
 *
 * @param driveTrainType The type of drive train (e.g., MECANUM, TANK).
 * @param constraints The constraints for the drive system, including
 *    maximum velocities and accelerations.
 * @param poseEstimate The initial pose estimate of the bot.
 */
class DriveShim(
    driveTrainType: DriveTrainType,
    private val constraints: Constraints,
    var poseEstimate: Pose2d
) {
    /**
     * The velocity constraint for the drive system, determined by the type of
     * drive train.
     */
    private val velConstraint = when (driveTrainType) {
        DriveTrainType.MECANUM -> MinVelocityConstraint(
            listOf(
                // Constraint for angular velocity
                AngularVelocityConstraint(constraints.maxAngVel),
                // Constraint for mecanum drive velocity
                MecanumVelocityConstraint(constraints.maxVel, constraints.trackWidth)
            )
        )

        DriveTrainType.TANK -> MinVelocityConstraint(
            listOf(
                // Constraint for angular velocity
                AngularVelocityConstraint(constraints.maxAngVel),
                // Constraint for tank drive velocity
                TankVelocityConstraint(constraints.maxVel, constraints.trackWidth)
            )
        )
    }

    /**
     * The acceleration constraint for the drive system, based on the profile
     * acceleration constraint.
     */
    private val accelConstraint = ProfileAccelerationConstraint(constraints.maxAccel)

    /**
     * Creates a new [TrajectorySequenceBuilder] starting from the given
     * [Pose2d].
     *
     * @param startPose The starting pose for the trajectory sequence.
     * @return A new instance of [TrajectorySequenceBuilder].
     */
    fun trajectorySequenceBuilder(startPose: Pose2d): TrajectorySequenceBuilder {
        return TrajectorySequenceBuilder(
            startPose,
            velConstraint,
            accelConstraint,
            constraints.maxAngVel,
            constraints.maxAngAccel,
        )
    }
}