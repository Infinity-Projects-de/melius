package de.infinityprojects.mcserver.entity.vehicle

import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.BoatMeta
import net.minestom.server.item.Material

class Boat(val chest: Boolean, type: BoatMeta.Type): Vehicle(EntityType.fromNamespaceId("minecraft:${if (chest) "chest_" else ""}${type.name.lowercase()}_boat")) {
    constructor(item: Material) : this(item.key().value().endsWith("_chest_boat"), BoatMeta.Type.valueOf(item.key().value().split("_")[0].uppercase()))
}