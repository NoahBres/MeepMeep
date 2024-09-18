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

/**
 * The [TrajectoryProgressSliderMaster] class is a custom JPanel that
 * manages the progress sliders for multiple [RoadRunnerBotEntity]
 * instances. It handles user interactions such as mouse events to update
 * the progress of the trajectories and provides methods to add or remove
 * bots.
 *
 * @param meepMeep The main MeepMeep instance.
 * @param sliderWidth The width of the slider.
 * @param sliderHeight The height of the slider.
 */
class TrajectoryProgressSliderMaster(
    private val meepMeep: MeepMeep,
    private val sliderWidth: Int,
    private val sliderHeight: Int
): JPanel(), MouseMotionListener, MouseListener {
    /**
     * List of pairs containing [RoadRunnerBotEntity] instances and their
     * corresponding [TrajectoryProgressSubSlider].
     */
    private val botList = mutableListOf<Pair<RoadRunnerBotEntity, TrajectoryProgressSubSlider>>()

    /** The maximum duration of the trajectory among all bots. */
    private var maxTrajectoryDuration = 0.0

    /** The index of the bot with the maximum trajectory duration. */
    private var maxTrajectoryIndex = 0

    /** Indicates whether the trajectory is currently paused. */
    private var internalIsPaused = false

    /**
     * Indicates whether the trajectory was paused before the mouse was pressed
     * down.
     */
    private var wasPausedBeforeMouseDown = false

    init {
        // Set the layout manager for this JPanel to a vertical BoxLayout
        layout = BoxLayout(this, BoxLayout.Y_AXIS)

        // Add mouse listeners to handle mouse events
        addMouseListener(this)
        addMouseMotionListener(this)

        // Map the space key to the action "space_pressed"
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("SPACE"), "space_pressed")

        // Define the action for "space_pressed" to toggle the pause state
        actionMap.put("space_pressed", object: AbstractAction() {
            override fun actionPerformed(p0: ActionEvent?) {
                // Toggle the internal pause state
                internalIsPaused = !internalIsPaused

                // Pause or unpause all bots based on the new pause state
                for ((bot, _) in botList) {
                    if (internalIsPaused)
                        bot.pause()
                    else
                        bot.unpause()
                }
            }
        })
    }

    /**
     * Adds a [RoadRunnerBotEntity] to the [TrajectoryProgressSliderMaster].
     * This method updates the maximum trajectory duration, adjusts the pause
     * state, and creates a new [TrajectoryProgressSubSlider] for the bot.
     *
     * @param bot The [RoadRunnerBotEntity] instance to add.
     * @throws Exception if the [RoadRunnerBotEntity] instance has already been
     *    added.
     */
    fun addRoadRunnerBot(bot: RoadRunnerBotEntity) {
        // Check if the bot has already been added to the list
        if (botList.indexOfFirst { it.first == bot } != -1) throw Exception("RoadRunnerBotEntity instance has already been added")

        // Get the duration of the current trajectory sequence of the bot
        val currSeqDuration = bot.currentTrajectorySequence?.duration() ?: 0.0

        // Update the maximum trajectory duration and index if the current sequence duration is greater
        if (currSeqDuration >= maxTrajectoryDuration) {
            maxTrajectoryDuration = currSeqDuration
            maxTrajectoryIndex = botList.size
        }

        // Ensure the maximum trajectory duration is updated
        maxTrajectoryDuration =
                max(bot.currentTrajectorySequence?.duration() ?: 0.0, maxTrajectoryDuration)

        // Update the maximum trajectory duration for all sliders
        for ((_, slider) in botList) {
            slider.maxTrajectoryDuration = maxTrajectoryDuration
        }

        // Set the internal pause state based on the first bot's trajectory pause state
        if (botList.isEmpty())
            internalIsPaused = bot.isTrajectoryPaused
        else {
            // Pause or unpause the bot based on the internal pause state
            if (internalIsPaused) bot.pause() else bot.unpause()

            // Disable looping for the first bot and the new bot
            botList[0].first.looping = false
        }

        // Create a new TrajectoryProgressSubSlider for the bot
        val subSlider = TrajectoryProgressSubSlider(
            bot,
            maxTrajectoryDuration,
            sliderWidth,
            sliderHeight,
            bot.colorScheme.trajectorySliderFG,
            bot.colorScheme.trajectorySliderBG,
            bot.colorScheme.trajectoryTextColor,
            MeepMeep.FONT_ROBOTO_BOLD
        )

        // Set the TrajectoryProgressSliderMaster for the bot
        bot.setTrajectoryProgressSliderMaster(this, botList.size)

        // Add the bot and its sub-slider to the list and the panel
        botList.add(Pair(bot, subSlider))
        add(subSlider)

        // Repack the window frame to accommodate the new sub-slider
        meepMeep.windowFrame.pack()
    }

    /**
     * Removes a [RoadRunnerBotEntity] from the
     * [TrajectoryProgressSliderMaster]. This method updates
     * the list of bots and repacks the window frame.
     *
     * @param bot The [RoadRunnerBotEntity] instance to remove.
     * @throws Exception if the [RoadRunnerBotEntity] instance is not found.
     */
    fun removeRoadRunnerBot(bot: RoadRunnerBotEntity) {
        // Find the index of the bot in the list
        val indexOfBot = botList.indexOfFirst { it.first == bot }

        // If the bot is found, remove it and repack the window frame
        if (indexOfBot != -1) {
            remove(botList[indexOfBot].second)
            botList.removeAt(indexOfBot)
            meepMeep.windowFrame.pack()
        } else {
            // Throw an exception if the bot is not found
            throw Exception("RoadRunnerBotEntity instance not found")
        }
    }

    /**
     * Reports that the bot at the given index has completed its trajectory. If
     * the bot is the one with the maximum trajectory duration, it starts all
     * bots.
     *
     * @param index The index of the bot that has completed its trajectory.
     */
    fun reportDone(index: Int) {
        // Check if the bot at the given index has the maximum trajectory duration
        if (index == maxTrajectoryIndex) {
            // Start all bots
            for ((bot, slider) in botList) {
                // Check if bot looping is set to true
                if (bot.looping) {
                    bot.start()
                } else {
                    bot.pause()

                    // Set the slider progress to 100% (1.0)
                    slider.progress = 1.0
                    slider.redraw()
                }
            }
        }
    }

    /**
     * Reports the progress of the bot at the given index. This method updates
     * the progress of the bot's trajectory based on the elapsed time.
     *
     * @param index The index of the bot in the botList.
     * @param elapsedTime The elapsed time to report progress for.
     */
    fun reportProgress(index: Int, elapsedTime: Double) {
        // Check if the index is valid
        if (index != -1) {
            // Update the progress of the bot's trajectory
            botList[index].second.progress = elapsedTime / maxTrajectoryDuration
        }
    }

    /**
     * Handles the mouse released event. This method resumes the trajectory for
     * all bots and redraws their respective sliders.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mouseReleased(me: MouseEvent?) {
        // Iterate through each bot and its corresponding slider in the botList
        for ((bot, slider) in botList) {
            // If the trajectory was not paused before the mouse was pressed, unpause the bot
            if (!wasPausedBeforeMouseDown) bot.unpause()

            // Resume the bot's trajectory
            bot.resume()

            // Redraw the slider to reflect the updated state
            slider.redraw()
        }
    }

    /**
     * Handles the mouse pressed event. This method pauses all bots, calculates
     * the clipped input time based on the mouse position, and updates the
     * trajectory progress for each bot.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mousePressed(me: MouseEvent?) {
        // Store the current pause state before the mouse is pressed
        wasPausedBeforeMouseDown = internalIsPaused

        // Calculate the clipped input percentage based on the mouse x position
        val clippedInputPercentage = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        // Calculate the clipped input time based on the maximum trajectory duration
        val clippedInputTime = clippedInputPercentage * maxTrajectoryDuration

        // Iterate through each bot and its corresponding slider in the botList
        for ((bot, slider) in botList) {
            // Pause the bot
            bot.pause()

            // Set the trajectory progress for the bot based on the clipped input time
            bot.setTrajectoryProgressSeconds(
                min(
                    clippedInputTime,
                    bot.currentTrajectorySequence?.duration() ?: 0.0
                )
            )

            // Redraw the slider to reflect the updated state
            slider.redraw()
        }
    }

    /**
     * Handles the mouse dragged event. This method updates the trajectory
     * progress for each bot based on the mouse position and redraws their
     * respective sliders.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mouseDragged(me: MouseEvent?) {
        // Calculate the clipped input percentage based on the mouse x position
        val clippedInputPercentage = min(max(me!!.x.toDouble() / width.toDouble(), 0.0), 1.0)
        // Calculate the clipped input time based on the maximum trajectory duration
        val clippedInputTime = clippedInputPercentage * maxTrajectoryDuration

        // Iterate through each bot and its corresponding slider in the botList
        for ((bot, slider) in botList) {
            // Set the trajectory progress for the bot based on the clipped input time
            bot.setTrajectoryProgressSeconds(
                min(
                    clippedInputTime,
                    bot.currentTrajectorySequence?.duration() ?: 0.0
                )
            )

            // Redraw the slider to reflect the updated state
            slider.redraw()
        }
    }

    /**
     * Handles the mouse moved event. This method is currently not implemented.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mouseMoved(me: MouseEvent?) {}

    /**
     * Handles the mouse clicked event. This method is currently not
     * implemented.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mouseClicked(me: MouseEvent?) {}

    /**
     * Handles the mouse entered event. This method is currently not
     * implemented.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mouseEntered(me: MouseEvent?) {}

    /**
     * Handles the mouse exited event. This method is currently not
     * implemented.
     *
     * @param me The MouseEvent that triggered this method.
     */
    override fun mouseExited(me: MouseEvent?) {}
}