package com.noahbres.meepmeep.core.colorscheme

import java.awt.Color

/** Abstract class for representing a color scheme */
abstract class ColorScheme {
    /** Indicates if the color scheme is dark or not. */
    abstract val isDark: Boolean

    /** Color for the robot body. */
    abstract val botBodyColor: Color

    /** Color for the robot wheels. */
    abstract val botWheelColor: Color

    /** Color for the robot direction indicator. */
    abstract val botDirectionColor: Color

    /** Color for the X-axis. */
    abstract val axisXColor: Color

    /** Color for the Y-axis. */
    abstract val axisYColor: Color

    /** Opacity for the axis lines in normal state. */
    abstract val axisNormalOpacity: Double

    /** Opacity for the axis lines in hover state. */
    abstract val axisHoverOpacity: Double

    /** Color for the trajectory paths. */
    abstract val trajectoryPathColor: Color

    /** Color for the trajectory turns. */
    abstract val trajectoryTurnColor: Color

    /** Color for the trajectory markers. */
    abstract val trajectoryMarkerColor: Color

    /** Background color for the trajectory slider. */
    abstract val trajectorySliderBG: Color

    /** Foreground color for the trajectory slider. */
    abstract val trajectorySliderFG: Color

    /** Text color for the trajectory slider. */
    abstract val trajectoryTextColor: Color

    /** Main background color for the UI. */
    abstract val uiMainBG: Color
}