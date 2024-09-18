package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TankVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint

/**
 * Utility class for creating velocity and acceleration constraints for a
 * Tank drive.
 *
 * This class provides methods to create [TrajectoryVelocityConstraint]
 * and [TrajectoryAccelerationConstraint] specific
 * to a Tank drive. It combines constraints such as
 * [AngularVelocityConstraint] and [TankVelocityConstraint]
 * to ensure the bot adheres to the specified constraints.
 *
 * @see [SampleMecanumDrive]
 * @see [DefaultBotBuilder]
 */
class SampleTankDrive {
    companion object {
        /**
         * Creates a velocity constraint for a Tank drive.
         *
         * This function combines an [AngularVelocityConstraint] and a
         * [TankVelocityConstraint] into a [MinVelocityConstraint] to ensure the
         * bot adheres to both constraints.
         *
         * @param maxVel The maximum linear velocity.
         * @param maxAngularVel The maximum angular velocity.
         * @param trackWidth The track width of the bot.
         * @return A [TrajectoryVelocityConstraint] that enforces the specified
         *    constraints.
         * @see [SampleMecanumDrive.getVelocityConstraint]
         * @see [DefaultBotBuilder]
         */
        fun getVelocityConstraint(
            maxVel: Double,
            maxAngularVel: Double,
            trackWidth: Double
        ): TrajectoryVelocityConstraint {
            // Create a list of velocity constraints
            val constraints = listOf(
                // Constraint for maximum angular velocity
                AngularVelocityConstraint(maxAngularVel),
                // Constraint for maximum linear velocity specific to Tank drive
                TankVelocityConstraint(maxVel, trackWidth)
            )

            // Return a MinVelocityConstraint that enforces the minimum of the provided constraints
            return MinVelocityConstraint(constraints)
        }

        /**
         * Creates an acceleration constraint for a Tank drive.
         *
         * This function returns a [ProfileAccelerationConstraint] that enforces
         * the specified maximum acceleration.
         *
         * @param maxAccel The maximum acceleration.
         * @return A [TrajectoryAccelerationConstraint] that enforces the specified
         *    acceleration constraint.
         * @see [SampleMecanumDrive.getAccelerationConstraint]
         * @see [DefaultBotBuilder]
         */
        fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint {
            // Return a ProfileAccelerationConstraint that enforces the specified maximum acceleration
            return ProfileAccelerationConstraint(maxAccel)
        }
    }
}