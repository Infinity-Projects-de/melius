package de.infinityprojects.mcserver.entity.animal.water

import de.infinityprojects.mcserver.entity.AgeableEntity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.water.AgeableWaterAnimalMeta

abstract class AgeableWaterAnimal<M : AgeableWaterAnimalMeta>(type: EntityType): AgeableEntity<M>(type) {
    override fun setSuperclassAttributes() {}
}