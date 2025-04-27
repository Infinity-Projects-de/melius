package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.spawnParticle
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect

class SpectralArrow(shooter: Entity?) : Arrow(shooter, EntityType.SPECTRAL_ARROW) {
    var duration = 200

    override fun tick(time: Long) {
        super.tick(time)
        if (!isOnGround) {
            spawnParticle(Particle.EFFECT, position, Vec.ZERO, 0.0f, 1)
        }
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val target = hitEvent.target
        target.addEffect(Potion(PotionEffect.GLOWING, duration, 1))
    }

    fun setDuration(duration: Int) {
        this.duration = duration
    }

    override fun getDefaultPickupItem(): ItemStack {
        return ItemStack.of(Material.SPECTRAL_ARROW)
    }
}