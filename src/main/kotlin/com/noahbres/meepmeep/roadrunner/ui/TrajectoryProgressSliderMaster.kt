package com.noahbres.meepmeep.roadrunner.ui

import com.noahbres.meepmeep.MeepMeep
import com.noahbres.meepmeep.roadrunner.entity.RoadRunnerBotEntity
import java.awt.event.ActionEvent
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import java.awt.event.MouseMotionListener
import javax.swing.AbstractAction
import javax.swing.BoxLayout
import javax.swing.JPanel
import javax.swing.KeyStroke
import kotlin.math.max
import kotlin.math.min

class TrajectoryProgressSliderMaster(
    private val meepMeep: MeepMeep,
    private val sliderWidth: Int,
    private val sliderHeight: Int
) : JPanel(), MouseMotionListener, MouseListener {
    private val botList = mutableListOf<Pair<RoadRunnerBotEntity, TrajectoryProgressSubSlider>>()

    private var maxTrajectoryDuration = 0.0
    private var maxTrajectoryIndex = 0

    private var internalIsPaused = false
    private var wasPausedBeforeMouseDown = false

    init {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        addMouseListener(this)
        addMouseMotionListener(this)

        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space_pressed")

        actionMap.put("space_pressed", object : AbstractAction() {
            override fun actionPerformed(p0: ActionEvent?) {
                internalIsPaused = !internalIsPaused

                for ((bot, _) in botList) {
                    if (internalIsPaused)
                        bot.pause()
                    else
                        bot.unpause()
                }
            }
        })
    }

    fun addRoadRunnerBot(bot: RoadRunnerBotEntity) {
        if (botList.indexOfFirst { it.first == bot } != -1) throw Exception("RoadRunnerBotEntity instance has already been added")

        val currSeqDuration = bot.currentTrajectorySequence?.duration() ?: 0.0
        if (currSeqDuration >= maxTrajectoryDuration) {
            maxTrajectoryDuration = currSeqDuration
            maxTrajectoryIndex = botList.size
        }

        maxTrajectoryDuration = max(bot.currentTrajectorySequence?.duration() ?: 0.0, maxTrajectoryDuration)
        for ((_, slider) in botList) {
            slider.maxTrajectoryDuration = maxTrajectoryDuration
        }

        if (botList.isEmpty())
            internalIsPaused = bot.trajectoryPaused
        else {
            if (internalIsPaused) bot.pause() else bot.unpause()
            botList[0].first.looping = false
            bot.looping = false
        }

        val subSlider = TrajectoryProgressSubSlider(
            bot,
            maxTrajectoryDuration,
            sliderWidth,
            sliderHeight,
            bot.colorScheme.TRAJECTORY_SLIDER_FG,
            bot.colorScheme.TRAJECTORY_SLIDER_BG,
            bot.colorScheme.TRAJECTORY_TEXT_COLOR,
            MeepMeep.FONT_CMU_BOLD
        )

        bot.setTrajectoryProgressSliderMaster(this, botList.size)

        botList.add(Pair(bot, subSlider))
        add(subSlider)

        meepMeep.windowFrame.pack()
    }

    fun removeRoadRunnerBot(bot: RoadRunnerBotEntity) {
        val indexOfBot = botList.indexOfFirst { it.first == bot }
        if (indexOfBot != -1) {
            remove(botList[indexOfBot].second)

            botList.removeAt(indexOfBot)
            meepMeep.windowFrame.pack()
        } else {
            throw Exception("RoadRunnerBotEntity instance not found")
        }
    }

    fun reportDone(index: Int) {
        if (index == maxTrajectoryIndex) {
            for ((bot, _) in botList) {
                bot.start()
            }
        }
    }

    fun reportProgress(index: Int, elapsedTime: Double) {
        if (index != -1) {
            botList[index].second.progress = elapsedTime / maxTrajectoryDuration
        }
    }

    override fun mouseReleased(me: MouseEvent?) {
        for ((bot, slider) in botList) {
            if (!wasPausedBeforeMouseDown) bot.unpause()

            bot.resume()

            slider.redraw()
        }
    }

    override fun mousePressed(me: MouseEvent?) {
        wasPausedBeforeMouseDown = internalIsPaused

        val clippedInputPercentage = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        val clippedInputTime = clippedInputPercentage * maxTrajectoryDuration

        for ((bot, slider) in botList) {
            bot.pause()

            bot.setTrajectoryProgressSeconds(min(clippedInputTime, bot.currentTrajectorySequence?.duration() ?: 0.0))

            slider.redraw()
        }
    }

    override fun mouseDragged(me: MouseEvent?) {
        val clippedInputPercentage = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        val clippedInputTime = clippedInputPercentage * maxTrajectoryDuration

        for ((bot, slider) in botList) {
            bot.setTrajectoryProgressSeconds(min(clippedInputTime, bot.currentTrajectorySequence?.duration() ?: 0.0))

            slider.redraw()
        }
    }

    override fun mouseMoved(me: MouseEvent?) {}

    override fun mouseClicked(me: MouseEvent?) {}

    override fun mouseEntered(me: MouseEvent?) {}

    override fun mouseExited(me: MouseEvent?) {}
}