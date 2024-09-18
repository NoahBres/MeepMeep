package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.constraints.AngularVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MecanumVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.MinVelocityConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.ProfileAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint

/**
 * Utility class for creating velocity and acceleration constraints for a
 * Mecanum drive.
 */
class SampleMecanumDrive {
    companion object {
        /**
         * Creates a velocity constraint for a Mecanum drive.
         *
         * This function combines an [AngularVelocityConstraint] and a
         * [MecanumVelocityConstraint] into a [MinVelocityConstraint]
         * to ensure the bot adheres to both constraints.
         *
         * @param maxVel The maximum linear velocity.
         * @param maxAngularVel The maximum angular velocity.
         * @param trackWidth The track width of the bot.
         * @return A [TrajectoryVelocityConstraint] that enforces the specified
         *    constraints.
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
                // Constraint for maximum linear velocity specific to Mecanum drive
                MecanumVelocityConstraint(maxVel, trackWidth)
            )

            // Return a MinVelocityConstraint that enforces the minimum of the provided constraints
            return MinVelocityConstraint(constraints)
        }

        /**
         * Creates an acceleration constraint for a Mecanum drive.
         *
         * This function returns a [ProfileAccelerationConstraint] that enforces
         * the specified maximum acceleration.
         *
         * @param maxAccel The maximum acceleration.
         * @return A [TrajectoryAccelerationConstraint] that enforces the specified
         *    acceleration constraint.
         */
        fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint {
            return ProfileAccelerationConstraint(maxAccel)
        }
    }
}