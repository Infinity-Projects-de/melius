package de.infinityprojects.mcserver.entity

import de.infinityprojects.mcserver.entity.element.LightningBolt
import de.infinityprojects.mcserver.entity.projectile.SmallFireball
import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.*
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.metadata.PathfinderMobMeta
import net.minestom.server.instance.block.Block
import net.minestom.server.potion.PotionEffect
import kotlin.math.ceil


abstract class PathfinderMob<M : PathfinderMobMeta>(type: EntityType): EntityCreature(type), SoundEmitter {
    val typedMeta: M = entityMeta as? M ?: throw IllegalStateException("Meta is not of type ${type.name()}")

    var lastDamageTimestamp: Long = 0
    val isAlive: Boolean
        get() = !isDead && !isRemoved

    val isInWater: Boolean
        get() {
            return instance.getBlock(position) == Block.WATER ||
                    instance.getBlock(position.add(0.0, eyeHeight, 0.0)) == Block.WATER
        }

    val isInLava: Boolean
        get() {
            return instance.getBlock(position) == Block.LAVA ||
                    instance.getBlock(position.add(0.0, eyeHeight, 0.0)) == Block.LAVA
        }

    val isInLiquid: Boolean
        get() = isInWater || isInLava

    val isPassenger: Boolean
        get() = vehicle != null

    var isPanicking: Boolean = false
    var isAngry: Boolean = false
    fun shootFireball(target: PathfinderMob<out PathfinderMobMeta>, spread: Double) {
        val fireball = SmallFireball(this)
        fireball.shoot(target.position, 1.0, spread)
    }
    val hasCustomName: Boolean
        get() = metadata[MetadataDef.CUSTOM_NAME] != null

    var airSupply: Int
        get() = metadata[MetadataDef.AIR_TICKS] ?: 0
        set(value) {
            metadata[MetadataDef.AIR_TICKS] = value
        }

    var noAI: Boolean
        get() = metadata[MetadataDef.Mob.NO_AI] ?: false
        set(value) {
            metadata[MetadataDef.Mob.NO_AI] = value
        }

    val maxHealth: Double
        get() = getAttributeValue(Attribute.MAX_HEALTH)



    init {
        addAIGroup(
            createGoals(),
            createTargets()
        )
        setSuperclassAttributes()
        setAttributes()
    }

    override fun damage(damage: Damage): Boolean {
        lastDamageTimestamp = instance.time
        return super.damage(damage)
    }


    open fun createGoals(): List<GoalSelector> { return emptyList() }
    open fun createTargets(): List<TargetSelector> { return emptyList() }
    open fun setAttributes() {}

    open fun canBeLeashed() = true

    abstract fun setSuperclassAttributes()

    open fun getSoundSource(): Sound.Source {
        return Sound.Source.NEUTRAL
    }


    fun heal(amount: Float) {
        val maxHealth = getAttributeValue(Attribute.MAX_HEALTH).toFloat()
        val newHealth = health + amount
        if (newHealth > maxHealth) {
            health = maxHealth
        } else {
            health = newHealth
        }
    }

    open fun onInteract(player: Player, hand: PlayerHand): InteractionResult = InteractionResult.PASS
    open fun onLightningStrike(lightning: LightningBolt) {}
    open fun isSensitiveToWater(): Boolean = false
    open fun maxAirSupply(): Int = MAX_AIR_SUPPLY

    companion object {
        const val MAX_AIR_SUPPLY = 300
    }

    open fun canAttack(target: Entity): Boolean = true

    override fun attack(target: Entity, swingHand: Boolean) {
        if (canAttack(target)) {
            playAttackSound()
            super.attack(target, swingHand)
        }
    }

    open fun canBeAffected(potionEffect: PotionEffect): Boolean {
        return true
    }

    fun canTeleportTo(pos: Pos): Boolean {
        val floor = pos.sub(0.0, 1.0, 0.0)
        val floorBlock = instance.getBlock(floor)
        if (!floorBlock.isSolid) return false

        val currBlock = instance.getBlock(pos)
        if (currBlock.isSolid) return false

        if (eyeHeight > 1.0) {
            for (i in 1 until ceil(eyeHeight).toInt()) {
                val eyeBlock = instance.getBlock(pos.add(0.0, i.toDouble(), 0.0))
                if (eyeBlock.isSolid) return false
            }
        }

        return true
    }

}