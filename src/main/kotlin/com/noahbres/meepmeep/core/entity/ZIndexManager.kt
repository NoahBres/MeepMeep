package com.noahbres.meepmeep.core.entity

class ZIndexManager {
    private var hierarchyMap = LinkedHashMap<String, MutableList<Entity>>()

    fun setTagHierarchy(vararg tags: String) {
        val reversedTags = tags.reversed()

        hierarchyMap["UNKNOWN_TAG_GROUP"] = mutableListOf()

        for (tag in reversedTags) {
            hierarchyMap[tag] = mutableListOf()
        }
    }

    fun addEntity(entity: Entity) {
        if (hierarchyMap.containsKey(entity.tag)) {
            hierarchyMap[entity.tag]!!.add(entity)
        } else {
            hierarchyMap["UNKNOWN_TAG_GROUP"]!!.add(entity)
        }

        setIndices()
    }

    private fun setIndices() {
        val entityList = mutableListOf<Entity>()

        hierarchyMap.forEach { (_, tagGroup) ->
            entityList.addAll(tagGroup)
        }

        entityList.forEachIndexed { index, entity ->
            entity.zIndex = index
        }
    }
}