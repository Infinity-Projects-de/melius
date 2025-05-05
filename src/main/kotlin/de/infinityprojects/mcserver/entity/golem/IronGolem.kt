package de.infinityprojects.mcserver.entity.golem

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.village.GolemRandomStrollInVillageGoal
import de.infinityprojects.mcserver.entity.ai.goal.village.OfferFlowerGoal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.MeleeAttackGoal
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.golem.IronGolemMeta
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import net.minestom.server.utils.time.TimeUnit
import kotlin.random.Random

class IronGolem: AbstractGolem<IronGolemMeta>(EntityType.IRON_GOLEM) {
    var attackTick = 0
    var offerFlowerCounter = 0
    var remainingAngerTime = 0

    var isPlayerCreated: Boolean
        get() = typedMeta.isPlayerCreated
        set(value) {
            typedMeta.isPlayerCreated = value
        }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            MeleeAttackGoal(this, 2.0, 5, TimeUnit.SERVER_TICK),
            RandomStrollGoal(this, 5),
            GolemRandomStrollInVillageGoal(this, 0.6),
            OfferFlowerGoal(this),
            LookAtPlayerGoal(this, 6.0),
            RandomLookAroundGoal(this, 2),
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            ClosestEntityTarget(this, 10.0) { entity ->
                entity.entityType == EntityType.PLAYER
            },
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 100.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.25
        getAttribute(Attribute.KNOCKBACK_RESISTANCE).baseValue = 1.0
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 15.0
        super.setAttributes()
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        if (attackTick > 0) attackTick--
        if (offerFlowerCounter > 0) offerFlowerCounter--
        if (remainingAngerTime > 0) remainingAngerTime--
    }

    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        val item = player.getItemInHand(hand)
        if (item.material() == Material.IRON_INGOT) {
            val prevHealth = health
            heal(25f)
            if (health > prevHealth) {
                player.setItemInHand(hand, item.withAmount { a -> a - 1 })
                val f1 = 1.0f + (Random.Default.nextFloat() - Random.Default.nextFloat()) * 0.2f
                playSoundEvent(SoundEvent.ENTITY_IRON_GOLEM_REPAIR, 1.0f, f1)

                return InteractionResult.SUCCESS
            } else {
                return InteractionResult.PASS
            }
        } else {
            return InteractionResult.PASS
        }
    }
}