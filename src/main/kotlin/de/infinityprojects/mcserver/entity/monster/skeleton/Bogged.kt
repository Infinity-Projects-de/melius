package de.infinityprojects.mcserver.entity.monster.skeleton

import net.minestom.server.entity.EntityType
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.skeleton.BoggedMeta

class Bogged: AbstractSkeleton<BoggedMeta>(EntityType.SKELETON) {
    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 16.0
    }
}