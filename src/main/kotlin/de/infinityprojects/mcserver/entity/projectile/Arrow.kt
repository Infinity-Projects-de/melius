package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.SoundEmitter
import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Point
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.*
import net.minestom.server.instance.block.Block
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

abstract class Arrow(
    shooter: Entity?,
    type: EntityType = EntityType.ARROW,
    velocity: Vec = Vec.ZERO,
    itemStack: ItemStack? = null,
    firedWeapon: ItemStack? = null
) : Projectile(shooter, type), SoundEmitter {
    enum class PickupStatus { DISALLOWED, ALLOWED, CREATIVE_ONLY }

    var inGround: Boolean
        get() = metadata[MetadataDef.Arrow.IN_GROUND] ?: false
        set(value) {
            metadata[MetadataDef.Arrow.IN_GROUND] = value
            inGroundTime = 0
        }

    var inGroundTime = 0

    var shakeTime: Int = 0
    var life: Int = 0
    var baseDamage: Double = 2.0
    var pickupStatus: PickupStatus = PickupStatus.DISALLOWED
    var crit: Boolean
        get() = metadata[MetadataDef.Arrow.IS_CRITICAL] ?: false
        set(value) {
            metadata[MetadataDef.Arrow.IS_CRITICAL] = value
        }
    var pierceLevel: Byte
        get() = metadata[MetadataDef.Arrow.PIERCING_LEVEL] ?: 0
        set(value) {
            metadata[MetadataDef.Arrow.PIERCING_LEVEL] = value
        }
    var pickupItemStack: ItemStack = itemStack ?: getDefaultPickupItem()
    var firedFromWeapon: ItemStack? = firedWeapon

    open fun getDefaultPickupItem(): ItemStack = ItemStack.of(Material.ARROW)
    open fun getHitGroundSound(): SoundEvent = SoundEvent.ENTITY_ARROW_HIT

    init {
        this.velocity = velocity
    }

    override fun tick(time: Long) {
        val atRest = inGround
        if (atRest) {
            shakeTime = (shakeTime - 1).coerceAtLeast(0)
            life++
            if (life >= 1200) remove()
            return
        }

        if (shakeTime > 0) {
            shakeTime--
        }

        val nextPosition = position.add(velocity)
        if (!instance.getBlock(nextPosition).isAir) {
            inGround = true
            shakeTime = 7
            crit = false
            pierceLevel = 0
            playSoundEvent(getHitGroundSound(), 1f, 1f)
            velocity = Vec.ZERO
            life++
            return
        }

        position = nextPosition
        velocity = velocity.mul(if (isInWater()) 0.6 else 0.99)
        if (!inGround) {
            applyGravity()
        } else {
            inGroundTime++
            if (inGroundTime > 1200) {
                remove()
            }
        }
        super.tick(time)
    }

    fun isInWater(): Boolean {
        // Simple version; customize as needed for your system.
        return instance.getBlock(position).compare(Block.WATER)
    }

    override fun shoot(to: Point?, power: Double, spread: Double) {
        super.shoot(to, power, spread)
        life = 0
    }

    fun setPickupStatus(status: PickupStatus) {
        pickupStatus = status
    }

    fun tryPickup(player: Player): Boolean {
        val pickup = when (pickupStatus) {
            PickupStatus.ALLOWED -> true
            PickupStatus.CREATIVE_ONLY -> player.gameMode == GameMode.CREATIVE
            else -> false
        }

        if (pickup) {
            playSoundEvent(SoundEvent.ENTITY_ITEM_PICKUP, 1f, 1f)
            if (player.inventory.addItemStack(pickupItemStack)) {
                remove()
            }
        }

        return pickup
    }

    override fun soundSource(): Sound.Source = Sound.Source.PLAYER

    override fun onTouch(player: Player) {
        if ((inGround || hasNoGravity()) && shakeTime <= 0) {
            tryPickup(player)
        }
    }
}
