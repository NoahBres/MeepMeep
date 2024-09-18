package com.noahbres.meepmeep.core.entity

/** Manages the Z-index hierarchy of entities. */
class ZIndexManager {
    // Map to store entities grouped by their tags in a specific hierarchy.
    private var hierarchyMap = LinkedHashMap<String, MutableList<Entity>>()

    /**
     * Sets the hierarchy of tags. Entities with tags not in this hierarchy
     * will be grouped under "UNKNOWN_TAG_GROUP".
     *
     * @param tags The tags in the desired hierarchy order.
     */
    fun setTagHierarchy(vararg tags: String) {
        // Reverse the tags to maintain hierarchy order
        val reversedTags = tags.reversed()

        // Initialize the unknown tag group
        hierarchyMap["UNKNOWN_TAG_GROUP"] = mutableListOf()

        // Initialize lists for each tag in the hierarchy
        for (tag in reversedTags) {
            hierarchyMap[tag] = mutableListOf()
        }
    }

    /**
     * Adds an entity to the appropriate tag group in the hierarchy.
     *
     * @param entity The entity to add.
     */
    fun addEntity(entity: Entity) {
        // Check if the entity's tag exists in the hierarchy map
        if (hierarchyMap.containsKey(entity.tag)) {
            // Add the entity to the corresponding tag group
            hierarchyMap[entity.tag]!!.add(entity)
        } else {
            // Add the entity to the unknown tag group
            hierarchyMap["UNKNOWN_TAG_GROUP"]!!.add(entity)
        }

        // Update the Z-indices of all entities
        setIndices()
    }

    /**
     * Sets the Z-index for all entities based on their position in the
     * hierarchy.
     */
    private fun setIndices() {
        // List to store all entities in the order of their Z-index
        val entityList = mutableListOf<Entity>()

        // Add all entities from each tag group to the entity list
        hierarchyMap.forEach { (_, tagGroup) ->
            entityList.addAll(tagGroup)
        }

        // Assign Z-index to each entity based on its position in the list
        entityList.forEachIndexed { index, entity ->
            entity.zIndex = index
        }
    }
}