package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.ai.goal.FloatGoal
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.TemptGoal
import de.infinityprojects.mcserver.entity.ai.goal.animal.BreedGoal
import de.infinityprojects.mcserver.entity.animal.water.Turtle
import net.minestom.server.color.DyeColor
import net.minestom.server.component.DataComponents
import net.minestom.server.entity.EntityPose
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.animal.tameable.CatMeta
import net.minestom.server.entity.metadata.animal.tameable.CatVariant
import net.minestom.server.entity.pathfinding.PPath
import net.minestom.server.item.Material
import net.minestom.server.registry.DynamicRegistry
import net.minestom.server.sound.SoundEvent

class Cat : TameableAnimal<CatMeta>(EntityType.CAT) {
    var variant: DynamicRegistry.Key<CatVariant>
        set(value) {
            set(DataComponents.CAT_VARIANT, value)
        }
        get() = get(DataComponents.CAT_VARIANT) ?: CatVariant.TABBY

    var isLying: Boolean
        get() = typedMeta.isLying
        set(value) {
            typedMeta.isLying = value
        }

    var isRelaxed: Boolean
        get() = typedMeta.isRelaxed
        set(value) {
            typedMeta.isRelaxed = value
        }

    var collarColor: DyeColor
        get() = get(DataComponents.CAT_COLLAR) ?: DyeColor.RED
        set(value) {
            set(DataComponents.CAT_COLLAR, value)
        }

    init {
        setAttributes()
    }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            FloatGoal(this),
            TameableAnimalPanicGoal(1.5),
            SitGoal(this),
            CatSleepWithPlayerGoal(this),
            TemptGoal(this, 0.6) { itemStack ->
                itemStack.material() == Material.COD || itemStack.material() == Material.SALMON
            }, // W/ chance
            CatSitOnBedGoal(1.1, 8),
            FollowOwnerGoal(this, 1.0, 10.0f, 5.0f),
            JumpOnBlockGoal(this, 0.8),
            LeapAtTargetGoal(this, 0.3f),
            OcelotAttackGoal(this),
            BreedGoal(this, 0.8),
            RandomStrollGoal(this, 8),
            LookAtPlayerGoal(this, 10.0),
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            RandomTargetNonTamedGoal(this, Rabbit::class, false),
            RandomTargetNonTamedGoal(this, Turtle::class, false) { entity ->
                entity.isBaby
            }
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 10.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.3
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 3.0
    }

    override fun ambientSound(): SoundEvent? {
        return if (isTamed) {
            when {
                inLove() -> SoundEvent.ENTITY_CAT_PURR
                random.nextInt(4) == 0 -> SoundEvent.ENTITY_CAT_PURREOW
                else -> SoundEvent.ENTITY_CAT_AMBIENT
            }
        } else {
            SoundEvent.ENTITY_CAT_STRAY_AMBIENT
        }
    }

    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_CAT_HURT
    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_CAT_DEATH

    fun playEatingSound() {
        playSoundEvent(SoundEvent.ENTITY_CAT_EAT, 1.0f, 1.0f)
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        val speed = getAttribute(Attribute.MOVEMENT_SPEED).value
        if (navigator.state == PPath.State.FOLLOWING) {
            when (speed) {
                0.6 -> {
                    pose = EntityPose.CROAKING
                    isSprinting = false
                }
                1.3 -> {
                    pose = EntityPose.STANDING
                    isSprinting = true
                }
                else -> {
                    pose = EntityPose.STANDING
                    isSprinting = false
                }
            }
        } else {
            pose = EntityPose.STANDING
            isSprinting = false
        }
    }



    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        val held = player.getItemInHand(hand)
        if (isTamed) {
            if (isOwner(player)) {
                // Dye the collar
                val dyeColor = held.material().toDyeColor()
                if (dyeColor != null && dyeColor.id != collarColor) {
                    collarColor = dyeColor.id
                    // Assume consumption method is handled elsewhere, or mutate inventory here.
                    return InteractionResult.SUCCESS
                }
                // Healing
                if (isFood(held) && health < getAttributeValue(Attribute.MAX_HEALTH)) {
                    heal(held.foodRestorationValue())
                    playEatingSound()
                    return InteractionResult.SUCCESS
                }
            }
        } else if (isFood(held)) {
            tryToTame(player)
            playEatingSound()
            return InteractionResult.SUCCESS
        }

        return super.onInteract(player, hand)
    }

    fun isFood(item: net.minestom.server.item.ItemStack): Boolean {
        return item.material() == Material.COD || item.material() == Material.SALMON
    }

    fun tryToTame(player: Player) {
        if (random.nextInt(3) == 0) {
            tame(player)
            setSitting(true)
        }
    }

    inner class CatSitOnBedGoal: GoalSelector(this) {
        override fun shouldStart(): Boolean {
            TODO("Not yet implemented")
        }

        override fun start() {
            TODO("Not yet implemented")
        }

        override fun tick(time: Long) {
            TODO("Not yet implemented")
        }

        override fun shouldEnd(): Boolean {
            TODO("Not yet implemented")
        }

        override fun end() {
            TODO("Not yet implemented")
        }
    }

    inner class CatSleepWithPlayerGoal: GoalSelector(this) {
        override fun shouldStart(): Boolean {
            TODO("Not yet implemented")
        }

        override fun start() {
            TODO("Not yet implemented")
        }

        override fun tick(time: Long) {
            TODO("Not yet implemented")
        }

        override fun shouldEnd(): Boolean {
            TODO("Not yet implemented")
        }

        override fun end() {
            TODO("Not yet implemented")
        }
    }



    companion object {
        const val DEFAULT_VARIANT = 0
        const val DEFAULT_COLLAR_COLOR = 14 // RED
    }
}
