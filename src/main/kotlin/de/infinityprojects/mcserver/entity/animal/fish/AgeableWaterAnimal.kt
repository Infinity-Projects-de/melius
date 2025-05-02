package de.infinityprojects.mcserver.entity.animal.fish

import de.infinityprojects.mcserver.entity.PathfinderMob
import net.minestom.server.entity.EntityType

abstract class AgeableWaterAnimal(type: EntityType): PathfinderMob(type) {
    override fun setSuperclassAttributes() {}
}