package de.infinityprojects.mcserver.entity.monster

import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.MoveTowardsRestrictionGoal
import de.infinityprojects.mcserver.entity.ai.goal.monster.blaze.BlazeFireballGoal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.ai.target.LastEntityDamagerTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.BlazeMeta
import net.minestom.server.sound.SoundEvent

class Blaze : Monster<BlazeMeta>(EntityType.BLAZE) {
    private var allowedHeightOffset = 0.5f
    private var nextHeightOffsetChangeTick = 0

    private var charged: Boolean
        get() = typedMeta.isOnFire
        set(value) { typedMeta.isOnFire = value }

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            BlazeFireballGoal(this, 4),
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
}

