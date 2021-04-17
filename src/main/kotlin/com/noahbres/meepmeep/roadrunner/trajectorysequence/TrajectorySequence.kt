package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d

class TrajectorySequence(private val sequenceList: List<SequenceSegment>) {
    val size: Int
        get() = sequenceList.size

    val duration: Double
        get() = sequenceList.sumByDouble { it.duration }

    val start: Pose2d
        get() = sequenceList.first().startPose

    val end: Pose2d
        get() = sequenceList.last().endPose

    val list: List<SequenceSegment>
        get() = sequenceList

    init {
        if (sequenceList.isEmpty()) throw EmptySequenceException()
    }

    operator fun get(i: Int): SequenceSegment {
        return sequenceList[i]
    }
}