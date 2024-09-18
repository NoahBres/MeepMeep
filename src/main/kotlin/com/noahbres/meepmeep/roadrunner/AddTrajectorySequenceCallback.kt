package com.noahbres.meepmeep.roadrunner

import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence

/**
 * [AddTrajectorySequenceCallback] is a functional interface that defines
 * a callback for building a [TrajectorySequence] using a provided
 * [DriveShim]. This interface is used to add custom trajectory sequences
 * to a RoadRunnerBotEntity.
 */
fun interface AddTrajectorySequenceCallback {
    /**
     * Builds a [TrajectorySequence] using the provided [DriveShim].
     *
     * @param drive The [DriveShim] used to build the trajectory sequence.
     * @return A [TrajectorySequence] object.
     */
    fun buildTrajectorySequence(drive: DriveShim): TrajectorySequence
}