package com.noahbres.meepmeep.core.colorscheme

import java.awt.Color

/** Class that represents a color in hexadecimal format */
class ColorHex(color: String) :
    Color(
        // Extract the red, green, and blue values from the hexadecimal string
        Integer.valueOf(color.substring(1, 3), 16), // Red
        Integer.valueOf(color.substring(3, 5), 16), // Green
        Integer.valueOf(color.substring(5, 7), 16) // Blue
    )
