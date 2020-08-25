package com.noahbres.meepmeep.core.colorscheme

import java.awt.Color

class ColorHex(color: String):
        Color(
                Integer.valueOf(color.substring(1, 3), 16),
                Integer.valueOf(color.substring(3, 5), 16),
                Integer.valueOf(color.substring(5, 7), 16)
        ) {
}
