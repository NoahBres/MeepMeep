package com.noahbres.meepmeep.core.entity

import com.noahbres.meepmeep.core.colorscheme.ColorScheme

/**
 * Interface representing an entity that can switch between different color
 * schemes.
 */
interface ThemedEntity : Entity {
    /**
     * Switches the color scheme of the entity.
     *
     * @param scheme The new color scheme to apply.
     */
    fun switchScheme(scheme: ColorScheme)
}