package de.infinityprojects.mcserver.entity.monster

import de.infinityprojects.mcserver.entity.PathfinderMob
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.MoveTowardsRestrictionGoal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.PathfinderMobMeta
import net.minestom.server.entity.metadata.monster.BlazeMeta
import net.minestom.server.sound.SoundEvent
import kotlin.math.sqrt

class Blaze : Monster<BlazeMeta>(EntityType.BLAZE) {
    private var allowedHeightOffset = 0.5f
    private var nextHeightOffsetChangeTick = 0

    private var charged: Boolean
        get() = typedMeta.isOnFire
        set(value) { typedMeta.isOnFire = value }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            BlazeFireballGoal(),
            MoveTowardsRestrictionGoal(this, 1.0, 5),
            RandomStrollGoal(this, 7),
            LookAtPlayerGoal(this, 8.0),
            RandomLookAroundGoal(this, 8)
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            LastEntityDamagerTarget(this, 8f),
            ClosestEntityTarget(this, 8.0) { entity ->
                entity is Player && hasLineOfSight(entity)
            }
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 6.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.23
        getAttribute(Attribute.FOLLOW_RANGE).baseValue = 48.0
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)
        if (!isOnGround && velocity.y < 0.0) {
            velocity = velocity.mul(1.0, 0.6, 1.0)
        }
    }

    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_BLAZE_HURT
    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_BLAZE_AMBIENT, 1.0f, 1.0f)
    }
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_BLAZE_DEATH
    override fun soundVolume(): Float = 1.0f
    override fun soundPitch(): Float = 1.0f

    override fun isSensitiveToWater(): Boolean = true

    override fun isOnFire(): Boolean = charged

    inner class BlazeFireballGoal : GoalSelector(this) {
        private var attackStep = 0
        private var attackTime = 0
        private var lastSeen = 0

        override fun shouldStart(): Boolean {
            val target = target as? PathfinderMob<out PathfinderMobMeta> ?: return false
            return target.isAlive && canAttack(target)
        }

        override fun start() {

        }

        override fun shouldEnd(): Boolean {
            val isDead = if (target != null && target is LivingEntity) {
                (target as LivingEntity).isDead
            } else {
                true
            }
            return attackTime <= 0 || target == null || isDead
        }

        override fun end() {
            charged = false
            lastSeen = 0
            attackStep = 0
        }

        override fun tick(time: Long) {
            attackTime--
            val target = target as? PathfinderMob<out PathfinderMobMeta> ?: return

            val hasLineOfSight = hasLineOfSight(target)
            if (hasLineOfSight) {
                lastSeen = 0
            } else {
                lastSeen++
            }

            val dX = target.position.x - position.x
            val dY = (target.position.y + 0.5) - (position.y + 0.5)
            val dZ = target.position.z - position.z
            val sqDist = dX * dX + dY * dY + dZ * dZ

            if (sqDist < 4.0) {
                if (!hasLineOfSight) return

                if (attackTime <= 0) {
                    attackTime = 20
                    attack(target)
                }

                navigator.setPathTo(target.position)
            } else if (sqDist < getAttributeValue(Attribute.FOLLOW_RANGE) * getAttributeValue(Attribute.FOLLOW_RANGE) && hasLineOfSight) {
                if (attackTime <= 0) {
                    attackStep++
                    if (attackStep == 1) {
                        attackTime = 60
                        charged = true
                    } else if (attackStep <= 4) {
                        attackTime = 6
                    } else {
                        attackTime = 100
                        attackStep = 0
                        charged = true
                    }

                    if (attackStep > 1) {
                        val d = sqrt(sqrt(sqDist)) * 0.5
                        shootFireball(target, spread = d)
                    }
                }
                lookAt(target)
            } else if (lastSeen < 5) {
                navigator.setPathTo(target.position)
            }
        }
    }
}

