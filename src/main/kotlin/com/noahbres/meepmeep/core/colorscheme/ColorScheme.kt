package com.noahbres.meepmeep.core.colorscheme

import java.awt.Color


abstract class ColorScheme {
    abstract val isDark: Boolean

    abstract val BOT_BODY_COLOR: Color
    abstract val BOT_WHEEL_COLOR: Color
    abstract val BOT_DIRECTION_COLOR: Color

    abstract val AXIS_X_COLOR: Color
    abstract val AXIS_Y_COLOR: Color
    abstract val AXIS_NORMAL_OPACITY: Double
    abstract val AXIS_HOVER_OPACITY: Double

    abstract val TRAJCETORY_PATH_COLOR: Color
    abstract val TRAJECTORY_TURN_COLOR: Color
    abstract val TRAJECTORY_MARKER_COLOR: Color

    abstract val TRAJECTORY_SLIDER_BG: Color
    abstract val TRAJECTORY_SLIDER_FG: Color
    abstract val TRAJECTORY_TEXT_COLOR: Color

    abstract val UI_MAIN_BG: Color
}