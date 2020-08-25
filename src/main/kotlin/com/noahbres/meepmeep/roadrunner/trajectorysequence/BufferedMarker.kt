package com.noahbres.meepmeep.roadrunner.trajectorysequence

import com.acmerobotics.roadrunner.geometry.Vector2d
import com.acmerobotics.roadrunner.trajectory.MarkerCallback

sealed class BufferedMarker(open val callback: MarkerCallback)

data class BufferedSpatialMarker(
        val point: Vector2d,
        override val callback: MarkerCallback
) : BufferedMarker(callback)

data class BufferedDisplacementMarker(
        val displacement: (Double) -> Double,
        override val callback: MarkerCallback
) : BufferedMarker(callback)

data class BufferedTemporalMarker(
        val time: (Double) -> Double,
        override val callback: MarkerCallback
) : BufferedMarker(callback)