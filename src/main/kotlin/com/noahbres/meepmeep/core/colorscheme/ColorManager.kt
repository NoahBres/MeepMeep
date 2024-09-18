package com.noahbres.meepmeep.core.colorscheme

import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedDark
import com.noahbres.meepmeep.core.colorscheme.scheme.ColorSchemeRedLight

/** Class that represents a manager for color schemes. */
class ColorManager {
    companion object {
        /** Default color palette. */
        @JvmField
        val COLOR_PALETTE = ColorPalette.DEFAULT_PALETTE

        /** Default light color scheme. */
        @JvmField
        val DEFAULT_THEME_LIGHT: ColorScheme = ColorSchemeRedLight()

        /** Default dark color scheme. */
        @JvmField
        val DEFAULT_THEME_DARK: ColorScheme = ColorSchemeRedDark()
    }

    /** Flag to indicate if dark mode is enabled. */
    var isDarkMode = false

    /** Current light color scheme. */
    private var lightTheme: ColorScheme = DEFAULT_THEME_LIGHT

    /** Current dark color scheme. */
    private var darkTheme: ColorScheme = DEFAULT_THEME_DARK

    /** Returns the current color scheme based on the dark mode flag. */
    val theme: ColorScheme
        get() {
            return if (!isDarkMode) lightTheme else darkTheme
        }

    /**
     * Sets light and dark color schemes.
     *
     * @param themeLight The light color scheme to set.
     * @param themeDark The dark color scheme to set. Defaults to the light
     *    color scheme if not provided.
     */
    @JvmOverloads
    fun setTheme(themeLight: ColorScheme, themeDark: ColorScheme = themeLight) {
        lightTheme = themeLight
        darkTheme = themeDark
    }
}
