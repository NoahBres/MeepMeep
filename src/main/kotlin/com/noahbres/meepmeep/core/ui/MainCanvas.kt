package com.noahbres.meepmeep.core.ui

import java.awt.Canvas
import java.awt.Dimension
import java.awt.image.BufferStrategy

/**
 * MainCanvas is a custom Canvas that sets up a double buffer strategy and
 * manages the canvas dimensions.
 *
 * @property internalWidth The width of the canvas.
 * @property internalHeight The height of the canvas.
 */
class MainCanvas(private var internalWidth: Int, private var internalHeight: Int) : Canvas() {
    // Buffer strategy for the canvas
    lateinit var bufferStrat: BufferStrategy

    init {
        // Set the bounds and preferred size of the canvas
        setBounds(0, 0, internalWidth, internalHeight)
        preferredSize = Dimension(internalWidth, internalHeight)
        ignoreRepaint = true
    }

    /** Initializes the buffer strategy and requests focus for the canvas. */
    fun start() {
        createBufferStrategy(2)
        bufferStrat = bufferStrategy

        requestFocus()
    }

    /**
     * Returns the preferred size of the canvas.
     *
     * @return The preferred size as a Dimension object.
     */
    override fun getPreferredSize(): Dimension {
        return Dimension(internalWidth, internalHeight)
    }
}
