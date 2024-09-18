package com.noahbres.meepmeep.roadrunner.trajectorysequence;

import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.acmerobotics.roadrunner.path.PathContinuityViolationException;
import com.acmerobotics.roadrunner.profile.MotionProfile;
import com.acmerobotics.roadrunner.profile.MotionProfileGenerator;
import com.acmerobotics.roadrunner.profile.MotionState;
import com.acmerobotics.roadrunner.trajectory.DisplacementMarker;
import com.acmerobotics.roadrunner.trajectory.DisplacementProducer;
import com.acmerobotics.roadrunner.trajectory.MarkerCallback;
import com.acmerobotics.roadrunner.trajectory.SpatialMarker;
import com.acmerobotics.roadrunner.trajectory.TemporalMarker;
import com.acmerobotics.roadrunner.trajectory.TimeProducer;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.acmerobotics.roadrunner.trajectory.TrajectoryBuilder;
import com.acmerobotics.roadrunner.trajectory.TrajectoryMarker;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryAccelerationConstraint;
import com.acmerobotics.roadrunner.trajectory.constraints.TrajectoryVelocityConstraint;
import com.acmerobotics.roadrunner.util.Angle;
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.SequenceSegment;
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TrajectorySegment;
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.TurnSegment;
import com.noahbres.meepmeep.roadrunner.trajectorysequence.sequencesegment.WaitSegment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import kotlin.Deprecated;

/**
 * Builder class for creating a sequence of trajectory segments.
 */
@SuppressWarnings("unused")
public class TrajectorySequenceBuilder {

    /**
     * Base velocity constraint for the trajectory.
     */
    private final TrajectoryVelocityConstraint baseVelConstraint;

    /**
     * Base acceleration constraint for the trajectory.
     */
    private final TrajectoryAccelerationConstraint baseAccelConstraint;

    /**
     * Maximum angular velocity for turns.
     */
    private final double baseTurnConstraintMaxAngVel;

    /**
     * Maximum angular acceleration for turns.
     */
    private final double baseTurnConstraintMaxAngAccel;

    /**
     * List of sequence segments in the trajectory.
     */
    private final List<SequenceSegment> sequenceSegments;

    /**
     * List of temporal markers in the trajectory.
     */
    private final List<TemporalMarker> temporalMarkers;

    /**
     * List of displacement markers in the trajectory.
     */
    private final List<DisplacementMarker> displacementMarkers;

    /**
     * List of spatial markers in the trajectory.
     */
    private final List<SpatialMarker> spatialMarkers;

    /**
     * Current velocity constraint for the trajectory.
     */
    private TrajectoryVelocityConstraint currentVelConstraint;

    /**
     * Current acceleration constraint for the trajectory.
     */
    private TrajectoryAccelerationConstraint currentAccelConstraint;

    /**
     * Current maximum angular velocity for turns.
     */
    private double currentTurnConstraintMaxAngVel;

    /**
     * Current maximum angular acceleration for turns.
     */
    private double currentTurnConstraintMaxAngAccel;

    /**
     * Last pose in the trajectory.
     */
    private Pose2d lastPose;

    /**
     * Tangent offset for the trajectory.
     */
    private double tangentOffset;

    /**
     * Flag indicating if the tangent is set absolutely.
     */
    private boolean setAbsoluteTangent;

    /**
     * Absolute tangent value.
     */
    private double absoluteTangent;

    /**
     * Current trajectory builder.
     */
    private TrajectoryBuilder currentTrajectoryBuilder;

    /**
     * Current duration of the trajectory.
     */
    private double currentDuration;

    /**
     * Current displacement of the trajectory.
     */
    private double currentDisplacement;

    /**
     * Last duration of the trajectory segment.
     */
    private double lastDurationTraj;

    /**
     * Last displacement of the trajectory segment.
     */
    private double lastDisplacementTraj;

    /**
     * Constructs a new TrajectorySequenceBuilder.
     *
     * @param startPose                     The starting pose of the trajectory.
     * @param startTangent                  The starting tangent of the trajectory, or null if not set.
     * @param baseVelConstraint             The base velocity constraint for the trajectory.
     * @param baseAccelConstraint           The base acceleration constraint for the trajectory.
     * @param baseTurnConstraintMaxAngVel   The maximum angular velocity for turns.
     * @param baseTurnConstraintMaxAngAccel The maximum angular acceleration for turns.
     */
    public TrajectorySequenceBuilder(
            Pose2d startPose,
            Double startTangent,
            TrajectoryVelocityConstraint baseVelConstraint,
            TrajectoryAccelerationConstraint baseAccelConstraint,
            double baseTurnConstraintMaxAngVel,
            double baseTurnConstraintMaxAngAccel
    ) {
        this.baseVelConstraint = baseVelConstraint;
        this.baseAccelConstraint = baseAccelConstraint;

        this.currentVelConstraint = baseVelConstraint;
        this.currentAccelConstraint = baseAccelConstraint;

        this.baseTurnConstraintMaxAngVel = baseTurnConstraintMaxAngVel;
        this.baseTurnConstraintMaxAngAccel = baseTurnConstraintMaxAngAccel;

        this.currentTurnConstraintMaxAngVel = baseTurnConstraintMaxAngVel;
        this.currentTurnConstraintMaxAngAccel = baseTurnConstraintMaxAngAccel;

        sequenceSegments = new ArrayList<>();

        temporalMarkers = new ArrayList<>();
        displacementMarkers = new ArrayList<>();
        spatialMarkers = new ArrayList<>();

        lastPose = startPose;

        tangentOffset = 0.0;

        setAbsoluteTangent = (startTangent != null);
        absoluteTangent = startTangent != null ? startTangent : 0.0;

        currentTrajectoryBuilder = null;

        currentDuration = 0.0;
        currentDisplacement = 0.0;

        lastDurationTraj = 0.0;
        lastDisplacementTraj = 0.0;
    }

    /**
     * Constructs a new `TrajectorySequenceBuilder` with the specified parameters.
     *
     * @param startPose                     The starting pose of the trajectory.
     * @param baseVelConstraint             The base velocity constraint for the trajectory.
     * @param baseAccelConstraint           The base acceleration constraint for the trajectory.
     * @param baseTurnConstraintMaxAngVel   The maximum angular velocity for turns.
     * @param baseTurnConstraintMaxAngAccel The maximum angular acceleration for turns.
     */
    public TrajectorySequenceBuilder(
            Pose2d startPose,
            TrajectoryVelocityConstraint baseVelConstraint,
            TrajectoryAccelerationConstraint baseAccelConstraint,
            double baseTurnConstraintMaxAngVel,
            double baseTurnConstraintMaxAngAccel
    ) {
        this(
                startPose, null,
                baseVelConstraint, baseAccelConstraint,
                baseTurnConstraintMaxAngVel, baseTurnConstraintMaxAngAccel
        );
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineTo(Vector2d endPosition) {
        return addPath(() -> currentTrajectoryBuilder.lineTo(endPosition, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineTo(
            Vector2d endPosition,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.lineTo(endPosition, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineToConstantHeading(Vector2d endPosition) {
        return addPath(() -> currentTrajectoryBuilder.lineToConstantHeading(endPosition, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineToConstantHeading(
            Vector2d endPosition,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.lineToConstantHeading(endPosition, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineToLinearHeading(Pose2d endPose) {
        return addPath(() -> currentTrajectoryBuilder.lineToLinearHeading(endPose, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineToLinearHeading(
            Pose2d endPose,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.lineToLinearHeading(endPose, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineToSplineHeading(Pose2d endPose) {
        return addPath(() -> currentTrajectoryBuilder.lineToSplineHeading(endPose, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder lineToSplineHeading(
            Pose2d endPose,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.lineToSplineHeading(endPose, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder strafeTo(Vector2d endPosition) {
        return addPath(() -> currentTrajectoryBuilder.strafeTo(endPosition, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder strafeTo(
            Vector2d endPosition,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.strafeTo(endPosition, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder forward(double distance) {
        return addPath(() -> currentTrajectoryBuilder.forward(distance, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder forward(
            double distance,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.forward(distance, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder back(double distance) {
        return addPath(() -> currentTrajectoryBuilder.back(distance, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder back(
            double distance,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.back(distance, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder strafeLeft(double distance) {
        return addPath(() -> currentTrajectoryBuilder.strafeLeft(distance, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder strafeLeft(
            double distance,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.strafeLeft(distance, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder strafeRight(double distance) {
        return addPath(() -> currentTrajectoryBuilder.strafeRight(distance, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder strafeRight(
            double distance,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.strafeRight(distance, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineTo(Vector2d endPosition, double endHeading) {
        return addPath(() -> currentTrajectoryBuilder.splineTo(endPosition, endHeading, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineTo(
            Vector2d endPosition,
            double endHeading,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.splineTo(endPosition, endHeading, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineToConstantHeading(Vector2d endPosition, double endHeading) {
        return addPath(() -> currentTrajectoryBuilder.splineToConstantHeading(endPosition, endHeading, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineToConstantHeading(
            Vector2d endPosition,
            double endHeading,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.splineToConstantHeading(endPosition, endHeading, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineToLinearHeading(Pose2d endPose, double endHeading) {
        return addPath(() -> currentTrajectoryBuilder.splineToLinearHeading(endPose, endHeading, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineToLinearHeading(
            Pose2d endPose,
            double endHeading,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.splineToLinearHeading(endPose, endHeading, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineToSplineHeading(Pose2d endPose, double endHeading) {
        return addPath(() -> currentTrajectoryBuilder.splineToSplineHeading(endPose, endHeading, currentVelConstraint, currentAccelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder splineToSplineHeading(
            Pose2d endPose,
            double endHeading,
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        return addPath(() -> currentTrajectoryBuilder.splineToSplineHeading(endPose, endHeading, velConstraint, accelConstraint));
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder setTangent(double tangent) {
        setAbsoluteTangent = true;
        absoluteTangent = tangent;

        pushPath();

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    private TrajectorySequenceBuilder setTangentOffset(double offset) {
        setAbsoluteTangent = false;

        this.tangentOffset = offset;
        this.pushPath();

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder setReversed(boolean reversed) {
        return reversed ? this.setTangentOffset(Math.toRadians(180.0)) : this.setTangentOffset(0.0);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder setConstraints(
            TrajectoryVelocityConstraint velConstraint,
            TrajectoryAccelerationConstraint accelConstraint
    ) {
        this.currentVelConstraint = velConstraint;
        this.currentAccelConstraint = accelConstraint;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder resetConstraints() {
        this.currentVelConstraint = this.baseVelConstraint;
        this.currentAccelConstraint = this.baseAccelConstraint;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder setVelConstraint(TrajectoryVelocityConstraint velConstraint) {
        this.currentVelConstraint = velConstraint;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder resetVelConstraint() {
        this.currentVelConstraint = this.baseVelConstraint;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder setAccelConstraint(TrajectoryAccelerationConstraint accelConstraint) {
        this.currentAccelConstraint = accelConstraint;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder resetAccelConstraint() {
        this.currentAccelConstraint = this.baseAccelConstraint;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder setTurnConstraint(double maxAngVel, double maxAngAccel) {
        this.currentTurnConstraintMaxAngVel = maxAngVel;
        this.currentTurnConstraintMaxAngAccel = maxAngAccel;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder resetTurnConstraint() {
        this.currentTurnConstraintMaxAngVel = baseTurnConstraintMaxAngVel;
        this.currentTurnConstraintMaxAngAccel = baseTurnConstraintMaxAngAccel;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addTemporalMarker(MarkerCallback callback) {
        return this.addTemporalMarker(currentDuration, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    @Deprecated(message = "This be an experiment.")
    public TrajectorySequenceBuilder addTemporalMarkerOffset(double offset, MarkerCallback callback) {
        return this.addTemporalMarker(currentDuration + offset, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addTemporalMarker(double time, MarkerCallback callback) {
        return this.addTemporalMarker(0.0, time, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addTemporalMarker(double scale, double offset, MarkerCallback callback) {
        return this.addTemporalMarker(time -> scale * time + offset, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addTemporalMarker(TimeProducer time, MarkerCallback callback) {
        this.temporalMarkers.add(new TemporalMarker(time, callback));
        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addSpatialMarker(Vector2d point, MarkerCallback callback) {
        this.spatialMarkers.add(new SpatialMarker(point, callback));
        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addDisplacementMarker(MarkerCallback callback) {
        return this.addDisplacementMarker(currentDisplacement, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder UNSTABLE_addDisplacementMarkerOffset(double offset, MarkerCallback callback) {
        return this.addDisplacementMarker(currentDisplacement + offset, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addDisplacementMarker(double displacement, MarkerCallback callback) {
        return this.addDisplacementMarker(0.0, displacement, callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addDisplacementMarker(double scale, double offset, MarkerCallback callback) {
        return addDisplacementMarker((displacement -> scale * displacement + offset), callback);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addDisplacementMarker(DisplacementProducer displacement, MarkerCallback callback) {
        displacementMarkers.add(new DisplacementMarker(displacement, callback));

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder turn(double angle) {
        return turn(angle, currentTurnConstraintMaxAngVel, currentTurnConstraintMaxAngAccel);
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder turn(double angle, double maxAngVel, double maxAngAccel) {
        pushPath();

        MotionProfile turnProfile = MotionProfileGenerator.generateSimpleMotionProfile(
                new MotionState(lastPose.getHeading(), 0.0, 0.0, 0.0),
                new MotionState(lastPose.getHeading() + angle, 0.0, 0.0, 0.0),
                maxAngVel,
                maxAngAccel
        );

        sequenceSegments.add(new TurnSegment(lastPose, angle, turnProfile, Collections.emptyList()));

        lastPose = new Pose2d(
                lastPose.getX(), lastPose.getY(),
                Angle.norm(lastPose.getHeading() + angle)
        );

        currentDuration += turnProfile.duration();

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder waitSeconds(double seconds) {
        pushPath();
        sequenceSegments.add(new WaitSegment(lastPose, seconds, Collections.emptyList()));
        currentDuration += seconds;

        return this;
    }

    /**
     * For documentation on this function, see the <a href="https://learnroadrunner.com">Roadrunner documentation</a>.
     */
    public TrajectorySequenceBuilder addTrajectory(Trajectory trajectory) {
        pushPath();
        sequenceSegments.add(new TrajectorySegment(trajectory));

        return this;
    }

    /**
     * Pushes the current trajectory path to the sequence segments.
     * If there is a current trajectory builder, it builds the trajectory
     * and adds it to the sequence segments. Then, it resets the current
     * trajectory builder to null.
     */
    private void pushPath() {
        if (currentTrajectoryBuilder != null) {
            Trajectory builtTraj = currentTrajectoryBuilder.build();
            sequenceSegments.add(new TrajectorySegment(builtTraj));
        }

        currentTrajectoryBuilder = null;
    }

    private void newPath() {
        final double resolution = 0.25;
        double tangent;

        if (currentTrajectoryBuilder != null) {
            pushPath();
        }

        lastDurationTraj = 0.0;
        lastDisplacementTraj = 0.0;
        tangent = setAbsoluteTangent ? absoluteTangent : Angle.norm(lastPose.getHeading() + tangentOffset);
        currentTrajectoryBuilder = new TrajectoryBuilder(lastPose, tangent, currentVelConstraint, currentAccelConstraint, resolution);
    }

    /**
     * Initializes a new path for the trajectory sequence.
     * <p>
     * This method resets the last duration and displacement of the trajectory segment,
     * calculates the tangent for the new path, and creates a new `TrajectoryBuilder`
     * with the current pose, tangent, velocity constraint, and acceleration constraint.
     * If there is an existing trajectory builder, it pushes the current path to the sequence segments.
     */
    public TrajectorySequence build() {
        pushPath();

        List<TrajectoryMarker> globalMarkers = convertMarkersToGlobal(
                sequenceSegments,
                temporalMarkers, displacementMarkers, spatialMarkers
        );

        return new TrajectorySequence(projectGlobalMarkersToLocalSegments(globalMarkers, sequenceSegments));
    }

    /**
     * Adds a path to the trajectory sequence using the provided callback.
     * If the current trajectory builder is null, a new path is initialized.
     * If a `PathContinuityViolationException` is thrown, a new path is initialized
     * and the callback is run again.
     *
     * @param callback The callback to add the path.
     * @return The `TrajectorySequenceBuilder` instance.
     */
    private TrajectorySequenceBuilder addPath(AddPathCallback callback) {
        if (currentTrajectoryBuilder == null) newPath();

        try {
            callback.run();
        } catch (PathContinuityViolationException e) {
            newPath();
            callback.run();
        }

        Trajectory builtTraj = currentTrajectoryBuilder.build();

        double durationDifference = builtTraj.duration() - lastDurationTraj;
        double displacementDifference = builtTraj.getPath().length() - lastDisplacementTraj;

        lastPose = builtTraj.end();
        currentDuration += durationDifference;
        currentDisplacement += displacementDifference;

        lastDurationTraj = builtTraj.duration();
        lastDisplacementTraj = builtTraj.getPath().length();

        return this;
    }

    /**
     * Converts the provided markers (temporal, displacement, and spatial) to global trajectory markers.
     *
     * @param sequenceSegments    The list of sequence segments in the trajectory.
     * @param temporalMarkers     The list of temporal markers to be converted.
     * @param displacementMarkers The list of displacement markers to be converted.
     * @param spatialMarkers      The list of spatial markers to be converted.
     * @return A list of global trajectory markers.
     */
    private List<TrajectoryMarker> convertMarkersToGlobal(
            List<SequenceSegment> sequenceSegments,
            List<TemporalMarker> temporalMarkers,
            List<DisplacementMarker> displacementMarkers,
            List<SpatialMarker> spatialMarkers
    ) {
        ArrayList<TrajectoryMarker> trajectoryMarkers = new ArrayList<>();

        // Convert temporal markers.
        for (TemporalMarker marker : temporalMarkers) {
            trajectoryMarkers.add(
                    new TrajectoryMarker(marker.getProducer().produce(currentDuration), marker.getCallback())
            );
        }

        // Convert displacement markers.
        for (DisplacementMarker marker : displacementMarkers) {
            double time = displacementToTime(
                    sequenceSegments,
                    marker.getProducer().produce(currentDisplacement)
            );

            trajectoryMarkers.add(
                    new TrajectoryMarker(
                            time,
                            marker.getCallback()
                    )
            );
        }

        // Convert spatial markers.
        for (SpatialMarker marker : spatialMarkers) {
            trajectoryMarkers.add(
                    new TrajectoryMarker(
                            pointToTime(sequenceSegments, marker.getPoint()),
                            marker.getCallback()
                    )
            );
        }

        return trajectoryMarkers;
    }

    /**
     * Projects global trajectory markers to local sequence segments.
     * <p>
     * This method takes a list of global trajectory markers and projects them onto the local sequence segments.
     * It iterates through the sequence segments and calculates the appropriate segment and offset time for each marker.
     * The markers are then added to the corresponding segment, creating new segments with the updated markers.
     *
     * @param markers          The list of global trajectory markers.
     * @param sequenceSegments The list of sequence segments in the trajectory.
     * @return A list of sequence segments with the projected markers.
     */
    private List<SequenceSegment> projectGlobalMarkersToLocalSegments(List<TrajectoryMarker> markers, List<SequenceSegment> sequenceSegments) {
        if (sequenceSegments.isEmpty()) return Collections.emptyList();

        double totalSequenceDuration = 0;

        for (SequenceSegment segment : sequenceSegments) {
            totalSequenceDuration += segment.getDuration();
        }

        for (TrajectoryMarker marker : markers) {
            SequenceSegment segment = null;
            int segmentIndex = 0;
            double segmentOffsetTime = 0;

            double currentTime = 0;
            for (int i = 0; i < sequenceSegments.size(); i++) {
                SequenceSegment seg = sequenceSegments.get(i);

                double markerTime = Math.min(marker.getTime(), totalSequenceDuration);

                if (currentTime + seg.getDuration() >= markerTime) {
                    segment = seg;
                    segmentIndex = i;
                    segmentOffsetTime = markerTime - currentTime;

                    break;
                } else {
                    currentTime += seg.getDuration();
                }
            }

            SequenceSegment newSegment = null;

            if (segment instanceof WaitSegment thisSegment) {
                List<TrajectoryMarker> newMarkers = new ArrayList<>(segment.getMarkers());

                newMarkers.addAll(sequenceSegments.get(segmentIndex).getMarkers());
                newMarkers.add(new TrajectoryMarker(segmentOffsetTime, marker.getCallback()));

                newSegment = new WaitSegment(thisSegment.getStartPose(), thisSegment.getDuration(), newMarkers);
            } else if (segment instanceof TurnSegment thisSegment) {
                List<TrajectoryMarker> newMarkers = new ArrayList<>(segment.getMarkers());

                newMarkers.addAll(sequenceSegments.get(segmentIndex).getMarkers());
                newMarkers.add(new TrajectoryMarker(segmentOffsetTime, marker.getCallback()));

                newSegment = new TurnSegment(thisSegment.getStartPose(), thisSegment.getTotalRotation(), thisSegment.getMotionProfile(), newMarkers);
            } else if (segment instanceof TrajectorySegment thisSegment) {

                List<TrajectoryMarker> newMarkers = new ArrayList<>(thisSegment.getTrajectory().getMarkers());
                newMarkers.add(new TrajectoryMarker(segmentOffsetTime, marker.getCallback()));

                newSegment = new TrajectorySegment(new Trajectory(thisSegment.getTrajectory().getPath(), thisSegment.getTrajectory().getProfile(), newMarkers));
            }

            sequenceSegments.set(segmentIndex, newSegment);
        }

        return sequenceSegments;
    }

    // Taken from Road Runner's TrajectoryGenerator.displacementToTime() since it's private
    // note: this assumes that the profile position is monotonic increasing

    /**
     * Converts a displacement value to a time value using a binary search algorithm.
     * This method assumes that the profile position is monotonically increasing.
     *
     * @param profile The motion profile to search within.
     * @param s       The displacement value to convert to time.
     * @return The time value corresponding to the given displacement.
     */
    private Double motionProfileDisplacementToTime(MotionProfile profile, double s) {
        double tLo = 0.0;
        double tHi = profile.duration();
        while (!(Math.abs(tLo - tHi) < 1e-6)) {
            double tMid = 0.5 * (tLo + tHi);
            if (profile.get(tMid).getX() > s) {
                tHi = tMid;
            } else {
                tLo = tMid;
            }
        }
        return 0.5 * (tLo + tHi);
    }

    /**
     * Converts a displacement value to a time value based on the sequence segments.
     * This method iterates through the sequence segments and calculates the time
     * corresponding to the given displacement.
     *
     * @param sequenceSegments The list of sequence segments in the trajectory.
     * @param s                The displacement value to convert to time.
     * @return The time value corresponding to the given displacement.
     */
    private Double displacementToTime(List<SequenceSegment> sequenceSegments, double s) {
        double currentTime = 0.0;
        double currentDisplacement = 0.0;

        for (SequenceSegment segment : sequenceSegments) {
            if (segment instanceof TrajectorySegment thisSegment) {

                double segmentLength = thisSegment.getTrajectory().getPath().length();

                if (currentDisplacement + segmentLength > s) {
                    double target = s - currentDisplacement;
                    double timeInSegment = motionProfileDisplacementToTime(
                            thisSegment.getTrajectory().getProfile(),
                            target
                    );

                    return currentTime + timeInSegment;
                } else {
                    currentDisplacement += segmentLength;
                    currentTime += thisSegment.getTrajectory().duration();
                }
            } else {
                currentTime += segment.getDuration();
            }
        }

        return 0.0;
    }

    /**
     * Converts a point to a time value based on the sequence segments.
     * This method iterates through the sequence segments and calculates the time
     * corresponding to the given point by projecting the point onto the path
     * and finding the closest point.
     *
     * @param sequenceSegments The list of sequence segments in the trajectory.
     * @param point            The point to convert to time.
     * @return The time value corresponding to the given point.
     */
    private Double pointToTime(List<SequenceSegment> sequenceSegments, Vector2d point) {
        class ComparingPoints {
            private final double distanceToPoint;
            private final double totalDisplacement;
            private final double thisPathDisplacement;

            public ComparingPoints(double distanceToPoint, double totalDisplacement, double thisPathDisplacement) {
                this.distanceToPoint = distanceToPoint;
                this.totalDisplacement = totalDisplacement;
                this.thisPathDisplacement = thisPathDisplacement;
            }
        }

        List<ComparingPoints> projectedPoints = new ArrayList<>();

        for (SequenceSegment segment : sequenceSegments) {
            if (segment instanceof TrajectorySegment thisSegment) {

                double displacement = thisSegment.getTrajectory().getPath().project(point, 0.25);
                Vector2d projectedPoint = thisSegment.getTrajectory().getPath().get(displacement).vec();
                double distanceToPoint = point.minus(projectedPoint).norm();

                double totalDisplacement = 0.0;

                for (ComparingPoints comparingPoint : projectedPoints) {
                    totalDisplacement += comparingPoint.totalDisplacement;
                }

                totalDisplacement += displacement;

                projectedPoints.add(new ComparingPoints(distanceToPoint, displacement, totalDisplacement));
            }
        }

        ComparingPoints closestPoint = null;

        for (ComparingPoints comparingPoint : projectedPoints) {
            if (closestPoint == null) {
                closestPoint = comparingPoint;
                continue;
            }

            if (comparingPoint.distanceToPoint < closestPoint.distanceToPoint)
                closestPoint = comparingPoint;
        }

        assert closestPoint != null;
        return displacementToTime(sequenceSegments, closestPoint.thisPathDisplacement);
    }

    /**
     * Callback interface for adding a path to the trajectory sequence.
     * The `run` method is called to execute the path addition logic.
     */
    private interface AddPathCallback {
        void run();
    }
}