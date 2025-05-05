package de.infinityprojects.mcserver.entity.animal

import net.minestom.server.component.DataComponents
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.animal.CowMeta
import net.minestom.server.entity.metadata.animal.CowVariant
import net.minestom.server.registry.DynamicRegistry

class Cow: AbstractCow<CowMeta>(EntityType.COW){
    var variant: DynamicRegistry.Key<CowVariant>
        get() = get(DataComponents.COW_VARIANT) ?: CowVariant.TEMPERATE
        set(value) {
            set(DataComponents.COW_VARIANT, value)
        }
}