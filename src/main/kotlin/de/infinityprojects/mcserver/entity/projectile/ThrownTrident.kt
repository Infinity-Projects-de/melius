package de.infinityprojects.mcserver.entity.projectile

import net.minestom.server.component.DataComponents
import net.minestom.server.entity.*
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.item.ItemStack
import net.minestom.server.item.enchant.Enchantment
import net.minestom.server.sound.SoundEvent

class ThrownTrident(shooter: Entity?, itemStack: ItemStack) : Arrow(shooter, EntityType.TRIDENT) {
    private var dealtDamage = false
    private var loyaltyLevel = getLoyaltyLevel(itemStack)
    private var isReturning = false

    init {
        metadata[MetadataDef.ThrownItemProjectile.ITEM] = itemStack
    }

    var noClip: Boolean
        get() = metadata[MetadataDef.ThrownTrident.IS_NO_CLIP]
        set(value) {
            metadata[MetadataDef.ThrownTrident.IS_NO_CLIP] = value
        }

    override fun tick(time: Long) {
        super.tick(time)
        if (inGround) {
            dealtDamage = true
        }

        val owner = shooter
        if (loyaltyLevel > 0 && (dealtDamage) && owner != null) {
            if (!isAcceptableReturnOwner(owner)) {
                remove()
            } else {
                if (position.distance(owner.position.add(0.0, owner.eyeHeight, 0.0)) < owner.boundingBox.width() + 1.0) {
                    remove()
                    return
                }

                noClip = true

                val direction = owner.position.add(0.0, owner.eyeHeight, 0.0)
                position = position.add(direction.asVec().normalize().mul(0.05 * loyaltyLevel))
                if (!isReturning) {
                    playSoundEvent(SoundEvent.ITEM_TRIDENT_RETURN, 10f, 1f)
                    isReturning = true
                }
            }
        }
    }

    private fun isAcceptableReturnOwner(owner: Entity): Boolean {
        return !(owner is Player && owner.gameMode == GameMode.SPECTATOR) && (owner !is LivingEntity || !owner.isDead)
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        super.onProjectileHit(hitEvent)
        val target = hitEvent.target
        if (target is LivingEntity) {
            val damage = 8f
            target.damage(Damage(DamageType.TRIDENT, this, shooter, position,damage))
            dealtDamage = true
        }
        playSoundEvent(SoundEvent.ITEM_TRIDENT_HIT, 1f, 1f)
    }

    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        if (!collideEvent.isCancelled) {
            playSoundEvent(SoundEvent.ITEM_TRIDENT_HIT_GROUND, 1f, 1f)
            remove()
        }
    }

    private fun getLoyaltyLevel(itemStack: ItemStack): Int {
        return itemStack.get(DataComponents.ENCHANTMENTS)?.level(Enchantment.LOYALTY) ?: 0
    }
}