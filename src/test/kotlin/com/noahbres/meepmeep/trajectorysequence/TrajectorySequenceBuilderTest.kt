package com.noahbres.meepmeep.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.acmerobotics.roadrunner.trajectory.constraints.DriveConstraints
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequenceBuilder
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TrajectorySequenceBuilderTest {
    @Test
    fun testBasicConstructor() {
        val builder = TrajectorySequenceBuilder(Pose2d(), DriveConstraints(0.0, 0.0, 0.0, 0.0, 0.0, 0.0))
    }
}