package com.noahbres.meepmeep.roadrunner

/**
 * Data class representing the constraints for a trajectory.
 *
 * @property maxVel The maximum velocity.
 * @property maxAccel The maximum acceleration.
 * @property maxAngVel The maximum angular velocity.
 * @property maxAngAccel The maximum angular acceleration.
 * @property trackWidth The track width.
 */
data class Constraints(
    val maxVel: Double, val maxAccel: Double,
    val maxAngVel: Double, val maxAngAccel: Double,
    val trackWidth: Double
)
