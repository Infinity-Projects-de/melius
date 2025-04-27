package de.infinityprojects.mcserver.entity.projectile

import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.instance.block.Block

class SmallFireball(shooter: LivingEntity) : Fireball(shooter, EntityType.SMALL_FIREBALL) {
    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        if (true /* Gamerule Griefing */) {
            val blockPos = collideEvent.collisionPosition
            if (instance.getBlock(blockPos).isAir) {
                instance.setBlock(blockPos, Block.FIRE)
            }
        }
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val target = hitEvent.entity
        val fireDuration = 100 // (5.0F in seconds * 20 ticks)
        if (target !is LivingEntity) return
        target.fireTicks = fireDuration

        // Do damage
        if(shooter != null) {
            val damage = 5.0f
            target.damage(Damage(DamageType.ON_FIRE, this, shooter, position, damage))
        }
    }
}