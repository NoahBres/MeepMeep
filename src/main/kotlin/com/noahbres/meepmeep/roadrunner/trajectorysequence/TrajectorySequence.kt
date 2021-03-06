package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d

typealias TrajectorySequence = List<SequenceSegment>

fun TrajectorySequence.start(): Pose2d =
    if(this.isNotEmpty()) this[0].startPose else Pose2d()

fun TrajectorySequence.end(): Pose2d =
        if(this.isNotEmpty()) this.last().endPose else Pose2d()

fun TrajectorySequence.duration(): Double = this.sumByDouble { sequenceSegment -> sequenceSegment.duration }