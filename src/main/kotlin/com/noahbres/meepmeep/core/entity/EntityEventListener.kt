package com.noahbres.meepmeep.core.entity

/** Interface for listening to entity events. */
interface EntityEventListener {
    /** Called when an entity is added to the entity list. */
    fun onAddToEntityList()

    /** Called when an entity is removed from the entity list. */
    fun onRemoveFromEntityList()
}