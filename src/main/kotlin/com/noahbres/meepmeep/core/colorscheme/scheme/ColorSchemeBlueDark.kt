package com.noahbres.meepmeep.core.colorscheme.scheme

import com.noahbres.meepmeep.core.colorscheme.ColorManager
import java.awt.Color

/** Class representing a dark blue color scheme. */
open class ColorSchemeBlueDark: ColorSchemeBlueLight() {
    /** Indicates that this is a dark color scheme. */
    override val isDark: Boolean = true

    /** Color for the X-axis. */
    override val axisXColor: Color = ColorManager.COLOR_PALETTE.gray300

    /** Color for the Y-axis. */
    override val axisYColor: Color = ColorManager.COLOR_PALETTE.gray300

    /** Opacity for the axis lines in normal state. */
    override val axisNormalOpacity: Double = 0.2

    /** Background color for the trajectory slider. */
    override val trajectorySliderBG: Color = ColorManager.COLOR_PALETTE.gray800

    /** Text color for the trajectory slider. */
    override val trajectoryTextColor: Color = ColorManager.COLOR_PALETTE.gray100
}