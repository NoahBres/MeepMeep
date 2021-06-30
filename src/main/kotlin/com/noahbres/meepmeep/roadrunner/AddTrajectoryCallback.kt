package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.trajectory.Trajectory

fun interface AddTrajectoryCallback {
    fun buildTrajectory(drive: DriveShim): List<Trajectory>
}