package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.SoundEmitter
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityProjectile
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.sound.SoundEvent

open class Projectile(shooter: Entity?, type: EntityType): EntityProjectile(shooter, type), SoundEmitter {
    open fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {}
    open fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {}
    open fun onTouch(player: Player) {}
    open fun canHitEntity(entity: Entity): Boolean = true
    open fun hitSound(): SoundEvent? = null

    open fun applyGravity() {
        if (hasNoGravity()) return
        val gravity = 0.05
        velocity = velocity.add(0.0, -gravity, 0.0)
    }
}