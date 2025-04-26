package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.ai.goal.FloatGoal
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.PanicGoal
import de.infinityprojects.mcserver.entity.ai.goal.TemptGoal
import de.infinityprojects.mcserver.entity.ai.goal.animal.BreedGoal
import de.infinityprojects.mcserver.entity.ai.goal.animal.FollowParentGoal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

abstract class AbstractCow(type: EntityType): Animal(type) {
    init {
        require(type == EntityType.COW || type == EntityType.MOOSHROOM) {
            "Invalid entity type for Cow: $type"
        }
    }

    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.2
        getAttribute(Attribute.MAX_HEALTH).baseValue = 10.0
    }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            FloatGoal(this),
            PanicGoal(this, 2.0),
            BreedGoal(this, 1.0),
            TemptGoal(this, 1.2) { item -> item.material() == Material.WHEAT },
            FollowParentGoal(this, 1.25),
            RandomStrollGoal(this, 5),
            LookAtPlayerGoal(this, 6.0),
            RandomLookAroundGoal(this, 2),
        )
    }

    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_COW_DEATH
    override fun stepSound(): SoundEvent? = SoundEvent.ENTITY_COW_STEP
    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_COW_HURT
    override fun playAmbientSound() {
        playSoundEvent(SoundEvent.ENTITY_COW_AMBIENT, soundVolume(), soundPitch())
    }

    override fun soundVolume(): Float = 0.4f

    override fun onInteract(
        player: Player,
        hand: PlayerHand
    ): InteractionResult {
        val item = player.getItemInHand(hand)
        if (item.material() == Material.BUCKET) {
            if (!isBaby) {
                val milkBucket = ItemStack.of(Material.MILK_BUCKET, 1)
                player.setItemInHand(hand, milkBucket)
                playSoundEvent(SoundEvent.ENTITY_COW_MILK, 1.0f, 1.0f)
                return InteractionResult.SUCCESS
            }
        }
        return super.onInteract(player, hand)
    }
}