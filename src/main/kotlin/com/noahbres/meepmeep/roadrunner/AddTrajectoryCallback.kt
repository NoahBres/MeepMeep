package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.Trajectory

/**
 * [AddTrajectoryCallback] is a functional interface that defines a
 * callback for building a list of [Trajectory] objects using a provided
 * [DriveShim]. This interface is used to add custom trajectories to a
 * RoadRunnerBotEntity.
 */
@Suppress("unused")
fun interface AddTrajectoryCallback {
    /**
     * Builds a list of [Trajectory] objects using the provided [DriveShim].
     *
     * @param drive The [DriveShim] used to build the trajectories.
     * @return A list of [Trajectory] objects.
     */
    fun buildTrajectory(drive: DriveShim): List<Trajectory>
}