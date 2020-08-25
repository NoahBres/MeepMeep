package com.noahbres.meepmeep.roadrunner

import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence

interface AddTrajectorySequenceCallback {
    fun buildTrajectorySequence(drive: DriveShim): TrajectorySequence
}