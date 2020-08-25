package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d

class TrajectorySequence : List<SequenceStep> {
    private val sequenceList = mutableListOf<SequenceStep>()

    private var cachedFirstPose = Pose2d()
    private var firstPoseCacheDirty = false

    private var cachedDuration = 0.0
    private var durationCacheDirty = false

    val firstPose: Pose2d
        get() {
            if (!firstPoseCacheDirty) return cachedFirstPose

            this.forEach {
                if (it is TrajectoryStep) {
                    cachedFirstPose = it.trajectory.start()
                    return cachedFirstPose
                }
            }

            return Pose2d()
        }

    val duration: Double
        get() {
            if (!durationCacheDirty) return cachedDuration

            cachedDuration = 0.0
            this.forEach {
                cachedDuration += when (it) {
                    is TrajectoryStep -> it.trajectory.duration()
                    is TurnStep -> it.motionProfile.duration()
                    is WaitStep -> it.seconds
                    else -> 0.0
                }
            }

            return cachedDuration
        }

    fun getCurrentState(time: Double): Pair<SequenceStep, Double> {
        var currentStep: SequenceStep? = null
        var currentOffset = 0.0

        this.forEach {
            if(time >= it.startTime) {
                currentStep = it
                currentOffset = time - it.startTime
            }
        }

        return Pair(currentStep!!, currentOffset)
    }

    fun add(step: SequenceStep) {
        sequenceList.add(step)

        firstPoseCacheDirty = true
        durationCacheDirty = true
    }

    override fun contains(element: SequenceStep) = sequenceList.contains(element)

    override fun containsAll(elements: Collection<SequenceStep>) = sequenceList.containsAll(elements)

    override fun indexOf(element: SequenceStep) = sequenceList.indexOf(element)

    override fun isEmpty() = sequenceList.isEmpty()

    override fun iterator() = sequenceList.iterator()

    override fun lastIndexOf(element: SequenceStep) = sequenceList.lastIndexOf(element)

    override fun listIterator() = sequenceList.listIterator()

    override fun listIterator(index: Int) = sequenceList.listIterator(index)

    override fun subList(fromIndex: Int, toIndex: Int) = sequenceList.subList(fromIndex, toIndex)

    override val size: Int
        get() = sequenceList.size

    override fun get(index: Int): SequenceStep {
        return sequenceList[index]
    }
}