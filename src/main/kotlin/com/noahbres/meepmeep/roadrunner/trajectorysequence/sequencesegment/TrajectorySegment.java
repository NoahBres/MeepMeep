package com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment;

import com.acmerobotics.roadrunner.trajectory.Trajectory;

import java.util.Collections;

/**
 * Represents a segment of a trajectory sequence that consists of a single trajectory.
 */
public final class TrajectorySegment extends SequenceSegment {
    /**
     * The trajectory associated with this segment.
     */
    private final Trajectory trajectory;

    /**
     * Constructs a new TrajectorySegment.
     *
     * @param trajectory The trajectory associated with this segment.
     */
    public TrajectorySegment(Trajectory trajectory) {
        // Note: Markers are already stored in the `Trajectory` itself.
        // This class should not hold any markers
        super(trajectory.duration(), trajectory.start(), trajectory.end(), Collections.emptyList());
        this.trajectory = trajectory;
    }

    /**
     * Gets the trajectory associated with this segment.
     *
     * @return The trajectory associated with this segment.
     */
    public Trajectory getTrajectory() {
        return this.trajectory;
    }
}