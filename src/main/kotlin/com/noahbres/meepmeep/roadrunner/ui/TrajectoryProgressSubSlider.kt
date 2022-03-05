package com.noahbres.meepmeep.roadrunner.ui

import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import java.awt.*
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import javax.swing.JPanel

class TrajectoryProgressSubSlider(
    private val entity: RoadRunnerBotEntity,
    var maxTrajectoryDuration: Double,
    sliderWidth: Int, sliderHeight: Int,
    var fg: Color, var bg: Color, var textColor: Color,
    font: Font? = null
) : JPanel() {
    private var _progress = 0.0
    var progress: Double
        get() = _progress
        set(value) {
            _progress = value
            redraw()
            _progress
        }

    private var image = BufferedImage(sliderWidth, sliderHeight, BufferedImage.TYPE_INT_ARGB)

    init {
        preferredSize = Dimension(sliderWidth, sliderHeight)
        maximumSize = Dimension(sliderWidth, sliderHeight)

        if (font != null) this.font = font
    }

    override fun paintComponent(gfx: Graphics?) {
        super.paintComponent(gfx)
        gfx?.drawImage(image, 0, 0, null)
    }

    fun redraw() {
        val g = image.graphics as Graphics2D
        g.setRenderingHint(
            RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON
        )

        g.color = bg
        g.fillRect(0, 0, width, height)

        g.color = fg
        g.fillRect(0, 0, (image.width * progress).toInt(), image.height)

        g.font = font.deriveFont(16f)
        g.color = textColor
        if (entity.currentTrajectorySequence != null) {
            val progressText = DecimalFormat("0.00").format(
                progress * maxTrajectoryDuration
            )
            val totalText = DecimalFormat("0.00").format(entity.currentTrajectorySequence!!.duration())
            val mainDrawString = "${progressText} / ${totalText}s"

            g.drawString(
                "${mainDrawString} ${if (entity.trajectoryPaused) "(paused)" else ""}",
                width / 2 - (g.fontMetrics.stringWidth(
                    mainDrawString
                ).toDouble() / 2.0).toInt(),
                height / 2 + g.fontMetrics.height / 4
            )
        }

        g.dispose()
        repaint()
    }
}