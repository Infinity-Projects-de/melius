package de.infinityprojects.mcserver.entity.golem

import de.infinityprojects.mcserver.entity.PathfinderMob
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.golem.AbstractGolemMeta

abstract class AbstractGolem<M : AbstractGolemMeta>(type: EntityType): PathfinderMob<M>(type) {
    final override fun setSuperclassAttributes() { }
}