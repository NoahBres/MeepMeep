package com.noahbres.meepmeep.roadrunner

import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence

fun interface AddTrajectorySequenceCallback {
    fun buildTrajectorySequence(drive: DriveShim): TrajectorySequence
}