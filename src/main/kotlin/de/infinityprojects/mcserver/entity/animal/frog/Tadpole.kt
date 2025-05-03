package de.infinityprojects.mcserver.entity.animal.frog

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.animal.water.EntityFish
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.water.fish.TadpoleMeta
import net.minestom.server.entity.pathfinding.followers.WaterNodeFollower
import net.minestom.server.entity.pathfinding.generators.WaterNodeGenerator
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

class Tadpole() : EntityFish<TadpoleMeta>(EntityType.TADPOLE) {
    init {
        navigator.setNodeGenerator { WaterNodeGenerator() }
        navigator.setNodeFollower { WaterNodeFollower(this) }
    }

    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 1.0
        getAttribute(Attribute.MAX_HEALTH).baseValue = 6.0
        super.setAttributes()
    }

    override fun onInteract(
        player: Player,
        hand: PlayerHand
    ): InteractionResult {
        val item = player.getItemInHand(hand)
        if (isFood(item)) {
            feed(player, item)
            player.setItemInHand(hand, item.withAmount { i -> i - 1 })
            return InteractionResult.SUCCESS
        }
        return super.onInteract(player, hand)
    }

    fun isFood(itemStack: ItemStack): Boolean {
        return itemStack.material() == Material.SLIME_BALL
    }

    private fun feed(player: Player, itemStack: ItemStack) {
        val left = BABY_INITIAL_AGE - age
        ageUp(speedUpSecondsWhenFeeding(left))
    }

    override fun ambientSound(): SoundEvent? = null
    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_TADPOLE_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_TADPOLE_DEATH

    override fun ageBoundaryReached() {
        super.ageBoundaryReached()

        val frog = Frog()
        frog.setInstance(instance, position)
        playSoundEvent(SoundEvent.ENTITY_TADPOLE_GROW_UP, 0.15f, 1.0f)
        remove()
    }

    override fun getFlopSound(): SoundEvent {
        return SoundEvent.ENTITY_TADPOLE_FLOP
    }
}