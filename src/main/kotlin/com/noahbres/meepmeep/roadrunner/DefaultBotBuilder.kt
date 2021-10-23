package com.noahbres.meepmeep.roadrunner

import com.acmerobotics.roadrunner.geometry.Pose2d
import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import com.noahbres.meepmeep.roadrunner.trajectorysequence.TrajectorySequence

class DefaultBotBuilder(private val meepMeep: MeepMeep) {

    private var constraints = Constraints(
        30.0, 30.0, Math.toRadians(60.0), Math.toRadians(60.0), 15.0
    )

    private var width = 18.0
    private var height = 18.0

    private var startPose = Pose2d()
    private var colorScheme: ColorScheme? = null
    private var opacity = 0.8

    private var driveTrainType = DriveTrainType.MECANUM

    fun setDimensions(width: Double, height: Double): DefaultBotBuilder {
        this.width = width
        this.height = height

        return this
    }

    fun setStartPose(pose: Pose2d): DefaultBotBuilder {
        this.startPose = pose

        return this
    }

    fun setConstraints(constraints: Constraints): DefaultBotBuilder {
        this.constraints = constraints

        return this
    }

    fun setConstraints(
        maxVel: Double,
        maxAccel: Double,
        maxAngVel: Double,
        maxAngAccel: Double,
        trackWidth: Double
    ): DefaultBotBuilder {
        constraints = Constraints(maxVel, maxAccel, maxAngVel, maxAngAccel, trackWidth)

        return this
    }

    fun setDriveTrainType(driveTrainType: DriveTrainType): DefaultBotBuilder {
        this.driveTrainType = driveTrainType

        return this
    }

    fun setColorScheme(scheme: ColorScheme): DefaultBotBuilder {
        this.colorScheme = scheme

        return this
    }

    fun build(): RoadRunnerBotEntity {
        return RoadRunnerBotEntity(
            meepMeep,
            constraints,
            width, height,
            startPose, colorScheme ?: meepMeep.colorManager.theme, opacity,
            driveTrainType, false
        )
    }

    fun followTrajectorySequence(trajectorySequence: TrajectorySequence): RoadRunnerBotEntity {
        val bot = this.build()
        bot.followTrajectorySequence(trajectorySequence)

        return bot
    }

    fun followTrajectorySequence(callback: AddTrajectorySequenceCallback): RoadRunnerBotEntity {
        return followTrajectorySequence(callback.buildTrajectorySequence(build().drive))
    }
}