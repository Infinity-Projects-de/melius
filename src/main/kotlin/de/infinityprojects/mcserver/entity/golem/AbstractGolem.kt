package de.infinityprojects.mcserver.entity.golem

import de.infinityprojects.mcserver.entity.PathfinderMob
import net.minestom.server.entity.EntityType

abstract class AbstractGolem(type: EntityType): PathfinderMob(type) {
    final override fun setSuperclassAttributes() { }
}