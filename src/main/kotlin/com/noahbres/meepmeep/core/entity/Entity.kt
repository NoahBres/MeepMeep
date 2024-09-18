package com.noahbres.meepmeep.core.entity

import com.noahbres.meepmeep.MeepMeep
import java.awt.Graphics2D

/** Represents a generic entity in the MeepMeep application. */
interface Entity {
    /** The tag associated with the entity. */
    val tag: String

    /** The Z-index of the entity, used for rendering order */
    var zIndex: Int

    /** The MeepMeep instance associated with the entity. */
    val meepMeep: MeepMeep

    /**
     * Updates the entity.
     *
     * @param deltaTime The time elapsed since the last update.
     */
    fun update(deltaTime: Long)

    /**
     * Renders the entity on the given graphics context.
     *
     * @param gfx The graphics context.
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    fun render(gfx: Graphics2D, canvasWidth: Int, canvasHeight: Int)

    /**
     * Sets the canvas dimensions.
     *
     * @param canvasWidth The width of the canvas.
     * @param canvasHeight The height of the canvas.
     */
    fun setCanvasDimensions(canvasWidth: Double, canvasHeight: Double)
}