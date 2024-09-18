package com.noahbres.meepmeep.roadrunner.ui

import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import javax.swing.JPanel

/**
 * `TrajectoryProgressSubSlider` is a custom `JPanel` that visually
 * represents the progress of a `RoadRunnerBotEntity`'s trajectory. It
 * displays a progress bar and text indicating the current progress and
 * total duration of the trajectory.
 *
 * @param entity The `RoadRunnerBotEntity` whose trajectory progress is
 *    being displayed.
 * @param maxTrajectoryDuration The maximum duration of the trajectory.
 * @param sliderWidth The width of the slider.
 * @param sliderHeight The height of the slider.
 * @param fg The foreground color of the progress bar.
 * @param bg The background color of the progress bar.
 * @param textColor The color of the text displaying the progress.
 * @param font The font used for the text displaying the progress.
 */
class TrajectoryProgressSubSlider(
    private val entity: RoadRunnerBotEntity,
    var maxTrajectoryDuration: Double,
    sliderWidth: Int,
    sliderHeight: Int,
    private var fg: Color,
    private var bg: Color,
    private var textColor: Color,
    font: Font? = null
) : JPanel() {
    /**
     * The progress of the trajectory, represented as a value between 0.0 and
     * 1.0.
     */
    private var _progress = 0.0

    /**
     * The public accessor for the progress of the trajectory. Setting this
     * value will update the internal progress and trigger a redraw of the
     * slider.
     */
    var progress: Double
        get() = _progress
        set(value) {
            _progress = value
            redraw()
            _progress
        }

    /** The image buffer used to draw the slider. */
    private var image = BufferedImage(sliderWidth, sliderHeight, BufferedImage.TYPE_INT_ARGB)

    init {
        // Set the preferred size of the slider
        preferredSize = Dimension(sliderWidth, sliderHeight)

        // Set the maximum size of the slider
        maximumSize = Dimension(sliderWidth, sliderHeight)

        // Set the font if it is not null
        if (font != null) this.font = font
    }

    /**
     * Paints the component. This method is called whenever the component needs
     * to be rendered. It draws the buffered image onto the component.
     *
     * @param gfx The Graphics context in which to paint.
     */
    override fun paintComponent(gfx: Graphics?) {
        // Call the superclass' paintComponent method to ensure proper painting behavior
        super.paintComponent(gfx)

        // Draw the buffered image onto the component
        gfx?.drawImage(image, 0, 0, null)
    }

    /**
     * Redraws the slider to reflect the current progress of the trajectory.
     * This method updates the graphical representation of the progress
     * bar and the text displaying the progress and total duration.
     */
    fun redraw() {
        // Get the graphics context from the buffered image
        val g = image.graphics as Graphics2D

        // Enable text anti-aliasing for smoother text rendering
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )

        // Draw the background of the slider
        g.color = bg
        g.fillRect(0, 0, width, height)

        // Draw the foreground progress bar
        g.color = fg
        g.fillRect(0, 0, (image.width * progress).toInt(), image.height)

        // Set the font for the progress text
        g.font = font.deriveFont(16f)
        g.color = textColor

        // If the entity has a current trajectory sequence, draw the progress text
        if (entity.currentTrajectorySequence != null) {
            val progressText = DecimalFormat("0.00").format(
                progress * maxTrajectoryDuration
            )
            val totalText =
                DecimalFormat("0.00").format(entity.currentTrajectorySequence!!.duration())
            val mainDrawString = "$progressText / ${totalText}s"

            // Draw the progress text centered in the slider
            g.drawString(
                "$mainDrawString ${if (entity.isTrajectoryPaused) "(paused)" else ""}",
                width / 2 - (g.fontMetrics.stringWidth(
                    mainDrawString
                ).toDouble() / 2.0).toInt(),
                height / 2 + g.fontMetrics.height / 4
            )
        }

        // Dispose of the graphics context to free up resources
        g.dispose()

        // Repaint the component to reflect the updated image
        repaint()
    }
}