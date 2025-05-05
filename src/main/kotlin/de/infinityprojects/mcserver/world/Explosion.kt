package de.infinityprojects.mcserver.world

import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.instance.Instance

class Explosion(
    var entity: Entity?,
    val power: Float,
    val createFire: Boolean,
    val damageEntities: Boolean
) {
    constructor(power: Float = 0f, createFire: Boolean = false, damageEntities: Boolean = false) : this(
        entity = null,
        power = power,
        createFire = createFire,
        damageEntities = damageEntities
    )

    constructor(entity: Entity?, power: Float = 0f) : this(
        entity = entity,
        power = power,
        createFire = false,
        damageEntities = true
    )

    constructor(power: Float) : this(
        entity = null,
        power = power,
        createFire = false,
        damageEntities = true
    )

    init {
        require(power >= 0) { "Power must be greater than or equal to 0" }
    }

    fun explode(instance: Instance, position: Pos) {
        instance.explode(position.x.toFloat(), position.y.toFloat(), position.z.toFloat(), power)
    }
}
