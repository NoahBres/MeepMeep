package com.noahbres.meepmeep.roadrunner.trajectorysequence;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.SequenceSegment;

import java.util.Collections;
import java.util.List;

/**
 * Represents a sequence of trajectory segments.
 */
public class TrajectorySequence {
    /**
     * The list of sequence segments in the trajectory sequence.
     */
    private final List<SequenceSegment> sequenceList;

    /**
     * Constructs a new TrajectorySequence.
     *
     * @param sequenceList The list of sequence segments.
     * @throws EmptySequenceException if the sequence list is empty.
     */
    public TrajectorySequence(List<SequenceSegment> sequenceList) {
        if (sequenceList.isEmpty()) throw new EmptySequenceException();
        this.sequenceList = Collections.unmodifiableList(sequenceList);
    }

    /**
     * Returns the starting pose of the trajectory sequence.
     *
     * @return The starting pose.
     */
    public Pose2d start() {
        return sequenceList.get(0).getStartPose();
    }

    /**
     * Returns the ending pose of the trajectory sequence.
     *
     * @return The ending pose.
     */
    public Pose2d end() {
        return sequenceList.get(sequenceList.size() - 1).getEndPose();
    }

    /**
     * Returns the total duration of the trajectory sequence.
     *
     * @return The total duration in seconds.
     */
    public double duration() {
        double total = 0.0;

        // Sum the duration of each segment in the sequence
        for (SequenceSegment segment : sequenceList) {
            total += segment.getDuration();
        }

        return total;
    }

    /**
     * Returns the sequence segment at the specified index.
     *
     * @param i The index of the sequence segment.
     * @return The sequence segment at the specified index.
     */
    public SequenceSegment get(int i) {
        return sequenceList.get(i);
    }

    /**
     * Returns the number of sequence segments in the trajectory sequence.
     *
     * @return The number of sequence segments.
     */
    public int size() {
        return sequenceList.size();
    }
}