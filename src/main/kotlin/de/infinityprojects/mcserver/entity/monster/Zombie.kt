package de.infinityprojects.mcserver.entity.monster

import de.infinityprojects.mcserver.entity.ai.goal.*
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.*
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.MeleeAttackGoal
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import net.minestom.server.utils.time.TimeUnit
import net.minestom.server.world.Difficulty
import java.nio.file.Files.getAttribute
import java.time.LocalDate
import java.time.temporal.ChronoField
import kotlin.random.Random

open class Zombie(type: EntityType = EntityType.ZOMBIE) : Monster<ZombieMeta>(type) {
    companion object {
        const val BABY_SPEED_MODIFIER = 0.5
        const val LEADER_BONUS_FOLLOW_RANGE = 1.0
        const val LEADER_BONUS_REINFORCEMENTS = 0.5
        const val LEADER_BONUS_MAX_HEALTH = 1.0
        const val BABY_CHANCE = 0.05
    }

    private var canBreakDoors: Boolean = false
    private var inWaterTime: Int = 0
    var conversionTime: Int = -1

    var isBaby: Boolean
        get() = typedMeta.isBaby
        set(value) {
            typedMeta.isBaby = value
            if (value) {
                getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.23 + BABY_SPEED_MODIFIER
            } else {
                getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.23
            }
        }

    init {
        setAttributes()
    }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            RemoveBlockGoal(this, Material.TURTLE_EGG, 1.0, 3),
            MeleeAttackGoal(this, 1.0, 20, TimeUnit.SERVER_TICK),
            MoveThroughVillageGoal(this, 1.0, true, 4, this::canBreakDoors),
            RandomStrollGoal(this, 10),
            LookAtPlayerGoal(this, 8.0),
            RandomLookAroundGoal(this, 2)
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            ClosestEntityTarget(this, 32.0) { it is Player },
            ClosestEntityTarget(this, 32.0) { it.entityType == EntityType.VILLAGER },
            ClosestEntityTarget(this, 32.0) { it.entityType == EntityType.IRON_GOLEM },
            ClosestEntityTarget(this, 10.0) { it.entityType == EntityType.TURTLE }
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.FOLLOW_RANGE).baseValue = 35.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.23
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 3.0
        getAttribute(Attribute.ARMOR).baseValue = 2.0
        getAttribute(Attribute.MAX_HEALTH).baseValue = 20.0
        getAttribute(Attribute.KNOCKBACK_RESISTANCE).baseValue = 0.0
        if (isBaby) {
            getAttribute(Attribute.MOVEMENT_SPEED).baseValue += BABY_SPEED_MODIFIER
        }
    }

    fun startUnderWaterConversion(time: Int) {
        conversionTime = time
        typedMeta.isBecomingDrowned = true
    }

    fun setCanBreakDoors(flag: Boolean) {
        canBreakDoors = flag
    }

    fun canBreakDoors(): Boolean = canBreakDoors

    override fun aiTick(time: Long) {
        super.aiTick(time)

        if (isAlive && !noAI) {
            if (conversionTime > 0) {
                --conversionTime
                if (conversionTime == 0) {
                    doUnderWaterConversion()
                }
            } else if (isInWater()) {
                inWaterTime++
                if (inWaterTime >= 600) {
                    startUnderWaterConversion(300)
                }
            } else {
                inWaterTime = 0
                typedMeta.isBecomingDrowned = false
            }
        }
    }

    private fun doUnderWaterConversion() {
        val drowned = Drowned()
        drowned.setInstance(instance, position)
        drowned.health = health
        drowned.bodyEquipment = bodyEquipment
        playSoundEvent(SoundEvent.ENTITY_ZOMBIE_CONVERTED_TO_DROWNED, 1.0f, 1.0f)
        remove()
    }

    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_ZOMBIE_AMBIENT, 1.0f, 1.0f)
    }
    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_ZOMBIE_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_ZOMBIE_DEATH
    override fun ambientSound(): SoundEvent = SoundEvent.ENTITY_ZOMBIE_STEP

    fun populateDefaultEquipment(random: Random) {
        if (random.nextFloat() < if (MinecraftServer.getDifficulty() == Difficulty.HARD) 0.05f else 0.01f) {
            val item = if (random.nextInt(3) == 0) Material.IRON_SWORD else Material.IRON_SHOVEL
            itemInMainHand = ItemStack.of(item)
        }
        // Pumpkin head logic for Halloween
        if (getEquipment(EquipmentSlot.HELMET).isAir) {
            val now = LocalDate.now()
            val day = now[ChronoField.DAY_OF_MONTH]
            val month = now[ChronoField.MONTH_OF_YEAR]
            if (month == 10 && day == 31 && random.nextFloat() < 0.25f) {
                val headItem = if (random.nextFloat() < 0.1f) Material.JACK_O_LANTERN else Material.CARVED_PUMPKIN
                setItemInHand(PlayerHand.MAIN, ItemStack.of(headItem))
            }
        }
    }

    private fun isInWater(): Boolean {
        // Placeholder, ideally checks the block at the entity's eyes.
        return false
    }

    fun handleAttributes(multiplier: Float) {
        getAttribute(Attribute.KNOCKBACK_RESISTANCE).baseValue = Random.nextDouble() * 0.05
        val d0 = Random.nextDouble() * 1.5 * multiplier
        if (d0 > 1.0) {
            getAttribute(Attribute.FOLLOW_RANGE).baseValue = 35.0 * d0
        }
        if (Random.nextFloat() < multiplier * 0.05f) {
            getAttribute(Attribute.MAX_HEALTH).baseValue = 20.0 * (Random.nextDouble() * 3.0 + 1.0)
            // leader zombie bonuses can be added here
            setCanBreakDoors(true)
        }
    }
}