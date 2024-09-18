package com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;

import java.util.List;


/**
 * Abstract class representing a segment of a trajectory sequence.
 */
public abstract class SequenceSegment {
    /**
     * The duration of the segment.
     */
    private final double duration;

    /**
     * The starting pose of the segment.
     */
    private final Pose2d startPose;

    /**
     * The ending pose of the segment.
     */
    private final Pose2d endPose;

    /**
     * The list of trajectory markers associated with the segment.
     */
    private final List<TrajectoryMarker> markers;

    /**
     * Constructs a new SequenceSegment.
     *
     * @param duration  The duration of the segment.
     * @param startPose The starting pose of the segment.
     * @param endPose   The ending pose of the segment.
     * @param markers   The list of trajectory markers associated with the segment.
     */
    protected SequenceSegment(
            double duration,
            Pose2d startPose, Pose2d endPose,
            List<TrajectoryMarker> markers
    ) {
        this.duration = duration;
        this.startPose = startPose;
        this.endPose = endPose;
        this.markers = markers;
    }

    /**
     * Gets the duration of the segment.
     *
     * @return The duration of the segment.
     */
    public double getDuration() {
        return this.duration;
    }

    /**
     * Gets the starting pose of the segment.
     *
     * @return The starting pose of the segment.
     */
    public Pose2d getStartPose() {
        return startPose;
    }

    /**
     * Gets the ending pose of the segment.
     *
     * @return The ending pose of the segment.
     */
    public Pose2d getEndPose() {
        return endPose;
    }

    /**
     * Gets the list of trajectory markers associated with the segment.
     *
     * @return The list of trajectory markers.
     */
    public List<TrajectoryMarker> getMarkers() {
        return markers;
    }
}