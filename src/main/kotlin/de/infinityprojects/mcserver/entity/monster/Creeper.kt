package de.infinityprojects.mcserver.entity.monster

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.PathfinderMob
import de.infinityprojects.mcserver.entity.ai.goal.FloatGoal
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.animal.Cat
import de.infinityprojects.mcserver.entity.animal.Ocelot
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.goal.MeleeAttackGoal
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.PathfinderMobMeta
import net.minestom.server.entity.metadata.monster.CreeperMeta
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import java.time.Duration
import kotlin.random.Random

class Creeper : Monster<CreeperMeta>(EntityType.CREEPER) {
    var swell = 0
    var maxSwell = 30
    var explosionRadius = 3
    var droppedSkulls = 0

    var charged: Boolean
        get() =  typedMeta.isCharged
        set(value) {
            typedMeta.isCharged = value
        }

    var ignited: Boolean
        get() = typedMeta.isIgnited
        set(value) {
            typedMeta.isIgnited = value
        }

    var state: CreeperMeta.State
        get() = typedMeta.state
        set(value) {
            typedMeta.state = value
        }

    override fun createGoals(): List<GoalSelector> {
        // Minestom does not include all MC goals; use your own implementations for missing ones
        return listOf(
            FloatGoal(this),
            SwellGoal(),
            AvoidEntityGoal(this, Ocelot::class, 6.0f, 1.0, 1.2),
            AvoidEntityGoal(this, Cat::class, 6.0f, 1.0, 1.2),
            MeleeAttackGoal(this, 1.0, Duration.ZERO),
            RandomStrollGoal(this, 10),
            LookAtPlayerGoal(this, 8.0),
            RandomLookAroundGoal(this, 2)
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.25
        super.setAttributes()
    }

    override fun tick(time: Long) {
        if (!isDead) {
            if (ignited) state = CreeperMeta.State.FUSE

            val swellDir = state
            if (swellDir == CreeperMeta.State.FUSE && swell == 0) {
                playSoundEvent(SoundEvent.ENTITY_CREEPER_PRIMED, 1.0f, 0.5f)
            }

            val modifier = if (state == CreeperMeta.State.IDLE) -1 else 1
            swell += modifier
            if (swell < 0) swell = 0

            if (swell >= maxSwell) {
                swell = maxSwell
                explodeCreeper()
            }
        }

        super.tick(time)
    }

    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        val item = player.getItemInHand(hand)
        if (item.material() == Material.FLINT_AND_STEEL || item.material() == Material.FIRE_CHARGE) {
            val sound = if (item.material() == Material.FIRE_CHARGE)
                SoundEvent.ITEM_FIRECHARGE_USE else SoundEvent.ITEM_FLINTANDSTEEL_USE
            playSoundEvent(sound, 1.0f, (Random.Default.nextFloat() * 0.4f + 0.8f))

            ignite()
            return InteractionResult.SUCCESS
        }
        return super.onInteract(player, hand)
    }

    private fun explodeCreeper() {
        val f = if (charged) 2.0f else 1.0f
        remove()
        instance.explode(position.x.toFloat(), position.y.toFloat(), position.z.toFloat(), explosionRadius * f, null)
    }

    fun ignite() {
        ignited = true
    }

    fun canDropMobsSkull(): Boolean = charged && droppedSkulls < 1
    fun increaseDroppedSkulls() { droppedSkulls++ }

    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_CREEPER_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_CREEPER_DEATH

    inner class SwellGoal: GoalSelector(this) {
        var target: PathfinderMob<out PathfinderMobMeta>? = null

        override fun shouldStart(): Boolean {
            val target = this@Creeper.target as? PathfinderMob<out PathfinderMobMeta> ?: return false
            return state == CreeperMeta.State.FUSE || target.isAlive && getDistanceSquared(target) < 9.0
        }

        override fun start() {
            this@Creeper.navigator.reset()
            target = this@Creeper.target as? PathfinderMob<out PathfinderMobMeta>
        }

        override fun shouldEnd(): Boolean {
            return true
        }

        override fun end() {
            target = null
        }

        override fun tick(time: Long) {
            val target = this.target
            state = if (target == null) {
                CreeperMeta.State.IDLE
            } else if (getDistanceSquared(target) > 49.0) {
                CreeperMeta.State.IDLE
            } else if (!hasLineOfSight(target)) {
                CreeperMeta.State.IDLE
            } else {
                CreeperMeta.State.FUSE
            }
        }
    }
}
