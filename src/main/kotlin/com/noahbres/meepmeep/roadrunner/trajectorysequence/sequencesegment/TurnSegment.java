package com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;
import com.acmerobotics.roadrunner.util.Angle;

import java.util.List;

/**
 * Represents a segment of a trajectory sequence that involves a turn.
 */
public final class TurnSegment extends SequenceSegment {
    /**
     * The total rotation for the turn.
     */
    private final double totalRotation;

    /**
     * The motion profile for the turn.
     */
    private final MotionProfile motionProfile;

    /**
     * Constructs a new TurnSegment.
     *
     * @param startPose     The starting pose of the segment.
     * @param totalRotation The total rotation for the turn.
     * @param motionProfile The motion profile for the turn.
     * @param markers       The list of trajectory markers.
     */
    public TurnSegment(Pose2d startPose, double totalRotation, MotionProfile motionProfile, List<TrajectoryMarker> markers) {
        super(motionProfile.duration(), startPose, new Pose2d(startPose.getX(), startPose.getY(), Angle.norm(startPose.getHeading() + totalRotation)), markers);

        this.totalRotation = totalRotation;
        this.motionProfile = motionProfile;
    }

    /**
     * Returns the total rotation for the turn.
     *
     * @return The total rotation.
     */
    public double getTotalRotation() {
        return this.totalRotation;
    }

    /**
     * Returns the motion profile for the turn.
     *
     * @return The motion profile.
     */
    public MotionProfile getMotionProfile() {
        return this.motionProfile;
    }
}