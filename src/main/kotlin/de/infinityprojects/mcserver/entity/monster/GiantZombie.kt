package de.infinityprojects.mcserver.entity.monster

import net.minestom.server.entity.EntityType
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.GiantMeta

class GiantZombie: Monster<GiantMeta>(EntityType.GIANT) {
    override fun setAttributes() {
        super.setAttributes()
        getAttribute(Attribute.MAX_HEALTH).baseValue = 100.0
    }
}