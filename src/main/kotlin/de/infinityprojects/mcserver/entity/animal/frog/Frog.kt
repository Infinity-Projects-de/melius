package de.infinityprojects.mcserver.entity.animal.frog

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.animal.Animal
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.animal.FrogMeta
import net.minestom.server.entity.metadata.animal.FrogVariant
import net.minestom.server.entity.pathfinding.followers.WaterNodeFollower
import net.minestom.server.entity.pathfinding.generators.WaterNodeGenerator
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.registry.DynamicRegistry
import net.minestom.server.sound.SoundEvent

class Frog : Animal<FrogMeta>(EntityType.FROG) {
    var tongueTarget: Entity?
        get() {
            val target = typedMeta.tongueTarget
            if (target != null) {
                return instance.getEntityById(target)
            }
            return null
        }
        set(value) {
            if (value != null) {
                typedMeta.tongueTarget = value.entityId
            }
            typedMeta.tongueTarget = null
        }

    var variant: DynamicRegistry.Key<FrogVariant>
        get() {
            return get(DataComponents.FROG_VARIANT, FrogVariant.TEMPERATE)
        }
        set(value) {
            set(DataComponents.FROG_VARIANT, value)
        }


    init {
        this.navigator.setNodeFollower { WaterNodeFollower(this) }
        this.navigator.setNodeGenerator { WaterNodeGenerator() } // WaterNodeGenerator for swimming?
    }

    fun eraseTongueTarget() {
        tongueTarget = null
    }

    override fun tick(time: Long) {
        super.tick(time)

        if (isInWater) {
            velocity = velocity.mul(0.9)
        }
    }

    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 1.0
        getAttribute(Attribute.MAX_HEALTH).baseValue = 10.0
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 10.0
        super.setAttributes()
    }

    override fun ambientSound(): SoundEvent = SoundEvent.ENTITY_FROG_AMBIENT
    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_FROG_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_FROG_DEATH
    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_FROG_STEP, 1.0f, 1.0f)
    }

    override fun isFood(itemStack: ItemStack): Boolean {
        return itemStack.material() == Material.SLIME_BALL
    }

    override fun canBeLeashed(): Boolean {
        return true
    }

    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        return InteractionResult.PASS
    }

    override fun playEatSound() {
        playSoundEvent(SoundEvent.ENTITY_FROG_EAT, 2.0f, 1.0f)
    }
}