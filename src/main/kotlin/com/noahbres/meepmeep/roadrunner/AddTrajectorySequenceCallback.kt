package com.noahbres.meepmeep.roadrunner

interface AddTrajectorySequenceCallback {
    fun buildTrajectorySequence(drive: DriveShim): TrajectorySequence
}