package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.Trajectory

interface AddTrajectoryCallback {
    fun buildTrajectory(drive: DriveShim): List<Trajectory>
}