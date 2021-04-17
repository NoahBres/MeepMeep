package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.constraints.*

class SampleTankDrive {
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
                    TankVelocityConstraint(maxVel, trackWidth)
                )
            )
        }

        @JvmStatic
        fun getAccelerationConstraint(maxAccel: Double): TrajectoryAccelerationConstraint? {
            return ProfileAccelerationConstraint(maxAccel)
        }
    }
}