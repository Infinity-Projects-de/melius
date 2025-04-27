package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.PathfinderMob
import de.infinityprojects.mcserver.entity.spawnParticle
import de.infinityprojects.mcserver.world.Explosion
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.particle.Particle
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.world.Difficulty

class WitherSkull(shooter: Entity?, direction: Vec) : Projectile(shooter, EntityType.WITHER_SKULL) {
    var isDangerous: Boolean = false

    init {
        setNoGravity(true)
        velocity = direction
    }

    override fun isOnFire(): Boolean {
        return false
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val target = hitEvent.target
        if (target !is LivingEntity) return
        val damage = if (isDangerous) 8f else 5f
        target.damage(Damage(DamageType.MOB_PROJECTILE, this, shooter, position, damage))

        if (!target.isDead) {
            if (isDangerous) {
                // Apply wither effect based on difficulty
                val effectDuration = when (MinecraftServer.getDifficulty()) {
                    Difficulty.NORMAL -> 10 * 20
                    Difficulty.HARD -> 40 * 20
                    else -> 0
                }
                if (effectDuration > 0) {
                    target.addEffect(Potion(PotionEffect.WITHER, effectDuration, 1))
                }
            }
        } else {
            shooter?.let {
                if (it is PathfinderMob) {
                    it.heal(5.0f)
                }
            }
        }
    }

    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        if (!collideEvent.isCancelled) {
            val explosion = Explosion(this, 1.0f, createFire = false, damageEntities = true)
            explosion.explode(instance, position)
            remove()
        }
    }

    override fun tick(time: Long) {
        super.tick(time)
        if (isDangerous) {
            spawnParticles()
        }
    }

    private fun spawnParticles() {
        instance?.let {
           spawnParticle(Particle.FLAME, position, Vec.ZERO, 0f, 1)
        }
    }

    fun setDangerous(dangerous: Boolean) {
        isDangerous = dangerous
    }
}