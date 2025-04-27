package de.infinityprojects.mcserver.entity.projectile

import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ExperienceOrb
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import kotlin.random.Random

class ThrownExpBottle(shooter: Entity?) : Projectile(shooter, EntityType.EXPERIENCE_BOTTLE) {
    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        if (!collideEvent.isCancelled) {
            val pos = collideEvent.collisionPosition
            val count = 3 + Random.Default.nextInt(5) + Random.Default.nextInt(5)
            val expOrb = ExperienceOrb(count.toShort())
            expOrb.setInstance(instance, pos)
            remove()
        }
    }
}