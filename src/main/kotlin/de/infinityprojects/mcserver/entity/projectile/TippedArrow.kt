package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.spawnParticle
import net.kyori.adventure.util.RGBLike
import net.minestom.server.color.Color
import net.minestom.server.component.DataComponents
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.particle.Particle
import net.minestom.server.potion.CustomPotionEffect
import net.minestom.server.potion.Potion
import kotlin.experimental.or

class TippedArrow(shooter: Entity?, val itemStack: ItemStack) : Arrow(shooter, EntityType.ARROW) {
    private val potionEffect: MutableList<CustomPotionEffect> = mutableListOf()
    private var effectColor: RGBLike = Color.WHITE

    init {
        updateEffects()
    }

    init {
        metadata[MetadataDef.ThrownItemProjectile.ITEM] = itemStack
    }

    private fun updateEffects() {
        // Assuming the ItemStack has Potion data
        val contents = itemStack.get(DataComponents.POTION_CONTENTS) ?: return
        potionEffect.addAll(contents.customEffects)
        contents.customColor?.let {
            effectColor = it
        }
    }

    override fun tick(time: Long) {
        super.tick(time)
        if (inGround) {
            if (inGroundTime % 5 == 0) {
                spawnParticles(1)
            }
        } else {
            spawnParticles(2)
        }
    }

    private fun spawnParticles(amount: Int) {
        if (amount > 0) {
            val color = effectColor
            repeat(amount) {
                spawnParticle(
                    Particle.DUST.withColor(color), position, Vec.ZERO, 1f, 1)
            }
        }
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val target = hitEvent.target
        potionEffect.forEach { effect ->
            val flags =
                (if (effect.isAmbient) Potion.AMBIENT_FLAG else 0) or
                (if (effect.showParticles()) Potion.PARTICLES_FLAG else 0) or
                (if (effect.showIcon()) Potion.ICON_FLAG else 0)

            target.addEffect(Potion(effect.id, effect.amplifier(), effect.duration(), flags))
        }
    }

    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        if (!collideEvent.isCancelled && inGroundTime >= 600) {
            metadata[MetadataDef.ThrownItemProjectile.ITEM] = ItemStack.of(Material.ARROW, 1)
        }
    }
}