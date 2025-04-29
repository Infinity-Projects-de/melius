package de.infinityprojects.mcserver.entity.animal.allay

import de.infinityprojects.mcserver.entity.PathfinderMob
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.pathfinding.followers.FlyingNodeFollower
import net.minestom.server.entity.pathfinding.generators.FlyingNodeGenerator
import net.minestom.server.instance.block.Block
import net.minestom.server.sound.SoundEvent
import kotlin.math.min

class Allay : PathfinderMob(EntityType.ALLAY) {
    var duplicationCooldown: Long = 0
    private var holdingItemAnimationTicks = 0f
    private var holdingItemAnimationTicks0 = 0f
    private var dancingAnimationTicks = 0f
    private var spinningAnimationTicks = 0f
    private var spinningAnimationTicks0 = 0f
    private var jukeboxPos: Pos? = null
    private var likedPlayer: Player? = null

    init {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 20.0
        getAttribute(Attribute.FLYING_SPEED).baseValue = 0.1
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.1
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 2.0

        this.navigator.setNodeGenerator { return@setNodeGenerator FlyingNodeGenerator() }
        this.navigator.setNodeFollower { return@setNodeFollower FlyingNodeFollower(this) }
    }

    override fun createGoals(): List<GoalSelector> {
        return emptyList()
    }

    override fun createTargets(): List<TargetSelector> {
        return emptyList()
    }

    override fun setSuperclassAttributes() {}

    override fun aiTick(time: Long) {
        super.aiTick(time)
        if (isAlive && time % 10 == 0L) {
            heal(1.0f)
        }
        if (isDancing() && shouldStopDancing() && time % 20 == 0L) {
            setDancing(false)
            jukeboxPos = null
        }
        updateDuplicationCooldown()
    }

    override fun tick(time: Long) {
        super.tick(time)
        holdingItemAnimationTicks0 = holdingItemAnimationTicks
        holdingItemAnimationTicks = if (hasItemInHand()) {
            min(holdingItemAnimationTicks + 1.0f, 5.0f)
        } else {
            min(holdingItemAnimationTicks - 1.0f, 5.0f)
        }

        if (isDancing()) {
            dancingAnimationTicks++
            spinningAnimationTicks0 = spinningAnimationTicks
            if (isSpinning()) {
                spinningAnimationTicks++
            } else {
                spinningAnimationTicks--
            }
            spinningAnimationTicks = min(spinningAnimationTicks, 15.0f)
        } else {
            dancingAnimationTicks = 0.0f
            spinningAnimationTicks = 0.0f
            spinningAnimationTicks0 = 0.0f
        }


        velocity = if (isInWater) {
            velocity.mul(0.8)
        } else if (isInLava) {
            velocity.mul(0.5)
        } else {
            velocity.mul(0.91)
        }
    }

    override fun damage(damage: Damage): Boolean {
        if (!isLikedPlayer(target)) {
            return false
        }
        return super.damage(damage)
    }

    private fun isLikedPlayer(entity: Entity?): Boolean {
        return entity is Player && entity.uuid == likedPlayer
    }

    fun setJukeboxPlaying(pos: Pos, playing: Boolean) {
        if (playing) {
            if (!isDancing()) {
                jukeboxPos = pos
                setDancing(true)
            }
        } else if (pos == jukeboxPos || jukeboxPos == null) {
            jukeboxPos = null
            setDancing(false)
        }
    }

    fun isDancing(): Boolean {
        return metadata[MetadataDef.Allay.IS_DANCING]
    }

    fun setDancing(flag: Boolean) {
        if (!flag || !isPanicking) {
            metadata[MetadataDef.Allay.IS_DANCING] = flag
        }
    }

    private fun shouldStopDancing(): Boolean {
        return jukeboxPos == null || !jukeboxPos!!.sameBlock(position) || !instance.getBlock(jukeboxPos!!)
            .compare(Block.JUKEBOX)
    }

    fun isSpinning(): Boolean {
        val f = dancingAnimationTicks % 55.0f
        return f < 15.0f
    }

    private fun updateDuplicationCooldown() {
        if (duplicationCooldown > 0) {
            duplicationCooldown--
            metadata[MetadataDef.Allay.CAN_DUPLICATE] = duplicationCooldown == 0L
        }
    }

    fun canDuplicate(): Boolean {
        return metadata[MetadataDef.Allay.CAN_DUPLICATE] ?: true
    }

    fun duplicateAllay() {
        val allay = Allay()
        allay.setInstance(instance, position)
        allay.resetDuplicationCooldown()
        resetDuplicationCooldown()
    }

    fun resetDuplicationCooldown() {
        duplicationCooldown = 6000
    }

    fun hasItemInHand(): Boolean {
        return !itemInMainHand.isAir
    }

    override fun ambientSound(): SoundEvent? = if (hasItemInHand()) {
        SoundEvent.ENTITY_ALLAY_AMBIENT_WITH_ITEM
    } else {
        SoundEvent.ENTITY_ALLAY_AMBIENT_WITHOUT_ITEM
    }

    override fun hurtSound(): SoundEvent? {
        return SoundEvent.ENTITY_ALLAY_HURT
    }

    override fun deathSound(): SoundEvent? {
        return SoundEvent.ENTITY_ALLAY_DEATH
    }

    override fun soundVolume(): Float {
        return 0.4f
    }
}