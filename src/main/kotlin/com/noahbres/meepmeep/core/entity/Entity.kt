package com.noahbres.meepmeep.core.entity

import com.noahbres.meepmeep.MeepMeep
import java.awt.Graphics2D

interface Entity {
    val tag: String

    var zIndex: Int

    val meepMeep: MeepMeep

    fun update(deltaTime: Long)
    fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int)

    fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double)
}