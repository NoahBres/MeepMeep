package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.constraints.*

class SampleMecanumDrive {
    companion object {
        @JvmStatic
        fun getVelocityConstraint(
            maxVel: Double,
            maxAngularVel: Double,
            trackWidth: Double
        ): TrajectoryVelocityConstraint {
            return MinVelocityConstraint(
                listOf(
                    AngularVelocityConstraint(maxAngularVel),
                    MecanumVelocityConstraint(maxVel, trackWidth)
                )
            )
        }

        @JvmStatic
        fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint? {
            return ProfileAccelerationConstraint(maxAccel)
        }
    }
}