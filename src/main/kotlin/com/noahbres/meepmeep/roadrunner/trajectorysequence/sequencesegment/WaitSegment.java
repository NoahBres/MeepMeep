package com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;

import java.util.List;

/**
 * Represents a segment in a trajectory sequence where the robot waits for a specified duration.
 */
public final class WaitSegment extends SequenceSegment {

    /**
     * Constructs a new WaitSegment.
     *
     * @param pose    The pose of the robot during the wait.
     * @param seconds The duration of the wait in seconds.
     * @param markers A list of trajectory markers to be executed during the wait.
     */
    public WaitSegment(Pose2d pose, double seconds, List<TrajectoryMarker> markers) {
        super(seconds, pose, pose, markers);
    }
}