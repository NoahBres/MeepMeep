package com.noahbres.meepmeep.roadrunner.ui

import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import java.awt.*
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import javax.swing.AbstractAction
import javax.swing.JPanel
import javax.swing.KeyStroke
import kotlin.math.max
import kotlin.math.min

class TrajectoryProgressSlider(
        private val entity: RoadRunnerBotEntity,
        width: Int, height: Int,
        var fg: Color, var bg: Color, var textColor: Color,
        font: Font? = null
) : JPanel(), MouseMotionListener, MouseListener {
    private var _progress = 0.0
    var progress: Double
        get() = _progress
        set(value) {
            _progress = value
            redraw()
            _progress
        }

    private var image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)

    init {
        preferredSize = Dimension(width, height)
        maximumSize = Dimension(width, height)

        addMouseMotionListener(this)
        addMouseListener(this)

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space_pressed")

        actionMap.put("space_pressed", object : AbstractAction() {
            override fun actionPerformed(p0: ActionEvent?) {
                entity.togglePause()
            }
        })

        if (font != null) this.font = font
    }

    override fun paintComponent(gfx: Graphics?) {
        super.paintComponent(gfx)
        gfx?.drawImage(image, 0, 0, null)
    }

    private fun redraw() {
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
                    progress * entity.currentTrajectorySequence!!.duration
            )
            g.drawString(
                    "${progressText}s${if (entity.trajectoryPaused) " (paused)" else ""}", width / 2 - (g.fontMetrics.stringWidth(
                    progressText
            ).toDouble() / 2.0).toInt(), height / 2 + g.fontMetrics.height / 4
            )
        }

        g.dispose()
        repaint()
    }

    override fun mouseReleased(me: MouseEvent?) {
        entity.unPause()
        redraw()
    }

    override fun mousePressed(me: MouseEvent?) {
        entity.pause()

        val clipped = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        entity.setTrajectoryProgress(clipped)

        redraw()
    }

    override fun mouseMoved(me: MouseEvent?) {}

    override fun mouseDragged(me: MouseEvent?) {
        val clipped = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        entity.setTrajectoryProgress(clipped)
    }

    override fun mouseEntered(me: MouseEvent?) {}

    override fun mouseClicked(me: MouseEvent?) {}

    override fun mouseExited(me: MouseEvent?) {}
}