package de.infinityprojects.mcserver.entity.animal.camel

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.animal.horse.AbstractHorse
import net.minestom.server.entity.*
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.animal.CamelMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.potion.PotionEffect
import net.minestom.server.sound.SoundEvent
import kotlin.math.max
import kotlin.random.Random

class Camel : AbstractHorse<CamelMeta>(EntityType.CAMEL) {
    private var dashCooldown: Int = 0
    private var idleAnimationTimeout: Int = 0

    var poseTime: Long
        get() {
            val time = instance?.time ?: throw IllegalStateException("Instance is null")
            return (time - metadata[MetadataDef.Camel.LAST_POSE_CHANGE_TICK])
        }
        set(value) {
            metadata[MetadataDef.Camel.LAST_POSE_CHANGE_TICK] = value
        }

    init {
        setAttributes()
        setNoGravity(false)
    }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 32.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.09
        getAttribute(Attribute.JUMP_STRENGTH).baseValue = 0.42
        getAttribute(Attribute.STEP_HEIGHT).baseValue = 1.5
    }

    override fun update(time: Long) {
        super.update(time)
        if (isDashing() && dashCooldown < 50 && (isOnGround || isInLiquid || isPassenger)) {
            setDashing(false)
        }

        if (dashCooldown > 0) {
            dashCooldown--
            if (dashCooldown == 0) {
                playSoundEvent(SoundEvent.ENTITY_CAMEL_DASH_READY, 1.0f, 1.0f)
            }
        }

        setupAnimationStates()
    }

    private fun setupAnimationStates() {
        if (idleAnimationTimeout <= 0) {
            idleAnimationTimeout = Random.Default.nextInt(40) + 80
        } else {
            idleAnimationTimeout--
        }
    }

    fun isDashing(): Boolean {
        return typedMeta.isDashing
    }

    fun setDashing(flag: Boolean) {
        typedMeta.isDashing = flag
    }

    fun sitDown() {
        if (!isSitting()) {
            playSoundEvent(SoundEvent.ENTITY_CAMEL_SIT, 1.0f, 1.0f)
            pose = EntityPose.SITTING
            poseTime = -(instance?.time ?: throw IllegalStateException("Instance is null"))
        }
    }

    fun standUp() {
        if (isSitting()) {
            playSoundEvent(SoundEvent.ENTITY_CAMEL_STAND, 1.0f, 1.0f)
            pose = EntityPose.STANDING
            poseTime = instance?.time ?: throw IllegalStateException("Instance is null")
        }
    }

    fun standUpInstantly() {
        pose = EntityPose.STANDING
        resetLastPoseChangeTickToFullStand(instance?.time ?: 0)
    }

    private fun resetLastPoseChangeTickToFullStand(time: Long) {
        poseTime = max(0, time - 52 - 1)
    }

    fun isSitting(): Boolean {
        return typedMeta.lastPoseChangeTick < 0
    }

    override fun canBeAffected(potionEffect: PotionEffect): Boolean {
        return potionEffect != PotionEffect.POISON && super.canBeAffected(potionEffect)
    }

    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        val itemStack = player.getItemInHand(hand)

        if (player.isSneaking && !isBaby) {
            openInventory(player)
            return InteractionResult.SUCCESS
        } else {
           // val result = itemStack.interactWithEntity(player, this, hand)

            if (isFood(itemStack)) {
                return feed(player, itemStack, hand)
            } else {
                if (passengers.size < 2 && !isBaby) {
                    this.addPassenger(player)
                }
                return InteractionResult.SUCCESS
            }
        }
    }

    override fun isTamed(): Boolean {
        return true
    }

    override fun handleEating(
        player: Player,
        item: ItemStack
    ): Boolean {
        if (!isFood(item)) {
            return false
        }

        val healed = health < maxHealth
        if (healed) {
            heal(2f)
        }

        val canFallInLove = isTamed() && !isBaby && canFallInLove()
        if (canFallInLove) {
            setInLove(player)
        }


        if (isBaby) {
            ageUp(10)
        }

        if (healed || canFallInLove || isBaby) {
            if (!isSilent) {
                playEatSound()
            }
            return true
        }

        return false
    }

    override fun isFood(item: ItemStack): Boolean {
        return item.material() == Material.HAY_BLOCK
    }

    override fun ambientSound(): SoundEvent = SoundEvent.ENTITY_CAMEL_AMBIENT
    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_CAMEL_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_CAMEL_DEATH

    override fun playEatSound() {
        playSoundEvent(SoundEvent.ENTITY_CAMEL_EAT, 1.0f, 1.0f + Random.Default.nextFloat() - Random.Default.nextFloat() * 0.2f)
    }

    private fun openInventory(player: Player) {
        // Open inventory logic here
        player.openInventory(this.inventory)
    }



}