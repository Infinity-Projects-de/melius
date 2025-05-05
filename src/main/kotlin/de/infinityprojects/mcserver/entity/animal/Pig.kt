package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.ai.goal.*
import de.infinityprojects.mcserver.entity.ai.goal.animal.BreedGoal
import de.infinityprojects.mcserver.entity.ai.goal.animal.FollowParentGoal
import de.infinityprojects.mcserver.entity.element.LightningBolt
import net.minestom.server.MinecraftServer
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.animal.PigMeta
import net.minestom.server.entity.metadata.animal.PigVariant
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.registry.DynamicRegistry
import net.minestom.server.sound.SoundEvent
import net.minestom.server.world.Difficulty

class Pig : Animal<PigMeta>(EntityType.PIG) {
    var isSaddled
        get() = typedMeta.isHasSaddle
        set(value) {
            typedMeta.isHasSaddle = value
        }

    var variant: DynamicRegistry.Key<PigVariant>
        get() = get(DataComponents.PIG_VARIANT) ?: PigVariant.TEMPERATE
        set(value) {
            set(DataComponents.PIG_VARIANT, value)
        }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            FloatGoal(this),
            PanicGoal(this, 1.25),
            BreedGoal(this, 1.0),
            TemptGoal(this, 1.2) { item -> item.material() == Material.CARROT_ON_A_STICK },
            TemptGoal(this, 1.2) { item -> item.material() == Material.CARROT },
            FollowParentGoal(this, 1.1),
            WaterAvoidingRandomStrollGoal(this, 1.0),
            LookAtPlayerGoal(this, 6.0),
            RandomLookAroundGoal(this, 2),
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.25
        getAttribute(Attribute.MAX_HEALTH).baseValue = 10.0
    }

    override fun ambientSound(): SoundEvent? = SoundEvent.ENTITY_PIG_AMBIENT
    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_PIG_HURT
    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_PIG_STEP, 0.15f, 1.0f)
    }
    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_PIG_DEATH

    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        val item = player.getItemInHand(hand)

        // Saddle mounting flow
        if (isSaddled && !player.isSneaking) {
            player.addPassenger(this)
            return InteractionResult.SUCCESS
        }
        // Attempt to saddle
        if (item.material() == Material.SADDLE && !isSaddled && !isBaby) {
            isSaddled = true
            player.setItemInHand(hand, item.withAmount { a -> a - 1 })
            playSoundEvent(SoundEvent.ENTITY_PIG_SADDLE, 1.0f, 1.0f)

            return InteractionResult.SUCCESS
        }
        return super.onInteract(player, hand)
    }

    override fun onLightningStrike(lightning: LightningBolt) {
        if (MinecraftServer.getDifficulty() != Difficulty.PEACEFUL) {
            val piglin = ZombifiedPiglin()
            piglin.setInstance(instance, position)
            if (!this.itemInMainHand.isSimilar(ItemStack.AIR)) {
                piglin.setItemInHand(PlayerHand.MAIN, ItemStack.of(Material.GOLDEN_SWORD))
            }
            piglin.isPersistent = true
            remove()
        } else {
            super.onLightningStrike(lightning)
        }
    }

    /*

    override fun getControllingPassenger(): Player? {
        val passenger = passengers.firstOrNull() as? Player
        if (isSaddled && passenger != null) {
            // Carrot-on-a-stick check
            if (passenger.itemInHand(Hand.MAIN).material() == Material.CARROT_ON_A_STICK) return passenger
        }
        return null
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        // Handle boost decaying
        if (boostActive) {
            boostTicks--
            if (boostTicks <= 0) boostActive = false
        }
    }

    override fun boost(): Boolean {
        if (!boostActive && isSaddled) {
            boostTicks = 140
            boostActive = true
            return true
        }
        return false
    }

     */
}