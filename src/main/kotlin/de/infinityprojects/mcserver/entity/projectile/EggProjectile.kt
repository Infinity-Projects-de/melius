package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.animal.Chicken
import de.infinityprojects.mcserver.entity.spawnParticle
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle
import kotlin.random.Random

class EggProjectile(
    shooter: LivingEntity?,
    val itemStack: ItemStack = ItemStack.of(Material.EGG)
) : Projectile(shooter, EntityType.EGG) {
    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        val e = collideEvent.entity
        if (e is LivingEntity) {
            e.damage(Damage(DamageType.THROWN, this, shooter, position, 0.0f))
        }
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val instance = instance ?: return

        if (Random.nextInt(8) == 0) {
            val spawnCount = if (Random.nextInt(32) == 0) 4 else 1
            repeat(spawnCount) {
                val chicken = Chicken()
                chicken.setInstance(instance, position)
            }
        }

        for (i in 0 until 8) {
            val offsetX = (Random.nextFloat() - 0.5) * 0.08
            val offsetY = (Random.nextFloat() - 0.5) * 0.08
            val offsetZ = (Random.nextFloat() - 0.5) * 0.08
            spawnParticle(
                Particle.ITEM.withItem(itemStack), position, Vec(
                    offsetX, offsetY, offsetZ
                ), 0.0f, 10
            )
        }

        remove()
    }

    fun getDefaultItem(): ItemStack = ItemStack.of(Material.EGG)
}
