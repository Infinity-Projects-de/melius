package de.infinityprojects.mcserver.entity.monster

import de.infinityprojects.mcserver.entity.ai.goal.*
import de.infinityprojects.mcserver.entity.ai.goal.monster.LeapAtTargetGoal
import de.infinityprojects.mcserver.entity.animal.armadillo.Armadillo
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.MeleeAttackGoal
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.SpiderMeta
import net.minestom.server.potion.PotionEffect
import net.minestom.server.sound.SoundEvent
import kotlin.random.Random

open class Spider<M : SpiderMeta>(type: EntityType) : Monster<M>(EntityType.SPIDER) {

    companion object {
        operator fun invoke() = Spider<SpiderMeta>(EntityType.SPIDER)
    }

    init {
        setAttributes()
        addAIGroup(createGoals(), createTargets())
    }

    var climbing
        get() = typedMeta.isClimbing
        set(value) {
            typedMeta.isClimbing = value
        }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 16.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.3
    }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            FloatGoal(this),
            AvoidEntityGoal(this, Armadillo::class.java, 6.0f, 1.0, 1.2) { entity -> !(entity as Armadillo).isScared },
            LeapAtTargetGoal(this, 0.4f),
            SpiderMeleeAttackGoal(this),
            RandomStrollGoal(this, 0.8),
            LookAtPlayerGoal(this, Player::class.java, 8.0f),
            RandomLookAroundGoal(this)
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            LastEntityDamagerTarget(this),
            SpiderNearestAttackableTargetGoal(this, Player::class.java),
            SpiderNearestAttackableTargetGoal(this, EntityType.IRON_GOLEM.entityClass)
        )
    }

    override fun tick(time: Long) {
        super.tick(time)
        setClimbing(isColliding)
    }

    override fun canBeAffected(potionEffect: PotionEffect): Boolean {
        return potionEffect != PotionEffect.POISON && super.canBeAffected(potionEffect)
    }

    override fun ambientSound(): SoundEvent = SoundEvent.ENTITY_SPIDER_AMBIENT
    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_SPIDER_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_SPIDER_DEATH
    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_SPIDER_STEP, 1.0, 1.0)
    }

    inner class SpiderMeleeAttackGoal() : MeleeAttackGoal(this, 1.0, true) {
        override fun shouldStart(): Boolean {
            return super.shouldStart() && !this@Spider.isPassenger
        }

        override fun shouldEnd(): Boolean {
            val lightLevel = this@Spider.instance.getBlockLight(position.blockX(), position.blockY(), position.blockZ())
            if (lightLevel >= 0.5f && Random.Default.nextInt(100) == 0) {
                this@Spider.target = null
                return true
            }
            return super.shouldEnd()
        }
    }

    inner class SpiderNearestAttackableTargetGoal<T : LivingEntity>(klass: Class<T>) : ClosestEntityTarget(this, 12.0, { e -> klass.isInstance(e) }) {
        override fun findTarget(): Entity? {
            val lightLevel = this@Spider.instance.getBlockLight(position.blockX(), position.blockY(), position.blockZ())
            if (lightLevel < 0.5f) {
                return super.findTarget()
            }
            return null
        }
    }
}