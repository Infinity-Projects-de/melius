package de.infinityprojects.mcserver.entity.vehicle

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.AbstractVehicleMeta

open class Vehicle(entity: EntityType): Entity(entity) {
    var health = 1f

    val maxPassengers = if (entity.key().value().endsWith("_boat") && !entity.key().value().startsWith("chest_")) 2 else 1

    init {
        val meta = this.entityMeta
        if (meta !is AbstractVehicleMeta) {
            throw IllegalArgumentException("EntityMeta is not an AbstractVehicleMeta")
        }
    }

    override fun addPassenger(entity: Entity) {
        if (entity.passengers.size < maxPassengers) {
            super.addPassenger(entity)
        }
    }

    fun damage(amount: Float) {
        if (health - amount <= 0) {
            MinecraftServer.getSchedulerManager().scheduleNextTick {
                remove()
            }
        } else {
            health -= amount
            val meta = this.entityMeta as AbstractVehicleMeta
            meta.setNotifyAboutChanges(false)
            meta.shakingTicks = 20
            meta.shakingDirection = 1
            meta.shakingMultiplier = 10f
            meta.setNotifyAboutChanges(true)
        }
    }
}