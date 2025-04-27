package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.monster.Blaze
import de.infinityprojects.mcserver.entity.spawnParticle
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.item.ItemEntityMeta
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle

class Snowball(shooter: Entity?) : Projectile(shooter, EntityType.SNOWBALL) {
     private fun getParticle(): Particle {
        val itemMeta = entityMeta as ItemEntityMeta
        val itemStack = itemMeta.item
        return if (itemStack.material() == Material.SNOWBALL) {
            Particle.ITEM_SNOWBALL
        } else {
            Particle.ITEM
        }
    }

    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        if (!collideEvent.isCancelled) {
            repeat(8) {
                val particle = getParticle()
                val position = position
                spawnParticle(particle, position, Vec.ZERO, 0.0f, 1)
            }
            remove()
        }
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val target = hitEvent.target
        if (target is LivingEntity) {
            val damage = if (target is Blaze) 3f else 0f
            target.damage(Damage(DamageType.MOB_PROJECTILE, this, shooter, position, damage))
        }
        remove()
    }
}