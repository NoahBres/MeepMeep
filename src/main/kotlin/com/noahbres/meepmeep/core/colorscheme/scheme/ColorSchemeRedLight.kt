package com.noahbres.meepmeep.core.colorscheme.scheme

import com.noahbres.meepmeep.core.colorscheme.ColorManager
import com.noahbres.meepmeep.core.colorscheme.ColorScheme
import java.awt.Color

/** Class representing a light red color scheme. */
open class ColorSchemeRedLight: ColorScheme() {
    /** Indicates that this is a light color scheme. */
    override val isDark: Boolean = false

    /** Color for the robot body. */
    override val botBodyColor = ColorManager.COLOR_PALETTE.red600

    /** Color for the robot wheels. */
    override val botWheelColor = ColorManager.COLOR_PALETTE.red900

    /** Color for the robot direction indicator. */
    override val botDirectionColor = ColorManager.COLOR_PALETTE.red900

    /** Color for the X-axis. */
    override val axisXColor: Color = ColorManager.COLOR_PALETTE.gray900

    /** Color for the Y-axis. */
    override val axisYColor: Color = ColorManager.COLOR_PALETTE.gray900

    /** Color for the trajectory paths. */
    override val trajectoryPathColor: Color = ColorManager.COLOR_PALETTE.blue500

    /** Color for the trajectory turns. */
    override val trajectoryTurnColor: Color = ColorManager.COLOR_PALETTE.pink600

    /** Color for the trajectory markers. */
    override val trajectoryMarkerColor: Color = ColorManager.COLOR_PALETTE.orange600

    /** Opacity for the axis lines in normal state. */
    override val axisNormalOpacity: Double = 0.3

    /** Opacity for the axis lines in hover state. */
    override val axisHoverOpacity: Double = 0.8

    /** Background color for the trajectory slider. */
    override val trajectorySliderBG: Color = ColorManager.COLOR_PALETTE.gray100

    /** Foreground color for the trajectory slider. */
    override val trajectorySliderFG: Color = ColorManager.COLOR_PALETTE.red600

    /** Text color for the trajectory slider. */
    override val trajectoryTextColor: Color = ColorManager.COLOR_PALETTE.gray900

    /** Main background color for the UI. */
    override val uiMainBG: Color = ColorManager.COLOR_PALETTE.gray100
}