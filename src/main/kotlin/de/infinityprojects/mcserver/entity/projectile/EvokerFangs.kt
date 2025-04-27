package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.SoundEmitter
import de.infinityprojects.mcserver.entity.isAlliedTo
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.sound.SoundEvent
import java.util.*
import kotlin.random.Random

class EvokerFangs(
    var warmupDelayTicks: Int = 0,
    var owner: LivingEntity? = null
) : Projectile(owner,EntityType.EVOKER_FANGS), SoundEmitter {
    companion object {
        const val ATTACK_DURATION = 20
        const val LIFE_OFFSET = 2
        const val ATTACK_TRIGGER_TICKS = 14
        private const val DEFAULT_WARMUP_DELAY = 0
    }

    private var sentSpikeEvent = false
    private var lifeTicks = 22
    private var attackStarted = false
    private var ownerUUID: UUID? = null

    constructor(x: Double, y: Double, z: Double, yawDegrees: Float, delay: Int, owner: LivingEntity?) : this(delay, owner) {
        position = Pos(x, y, z, yawDegrees, 0f)
        this.owner = owner
        this.ownerUUID = owner?.uuid
    }

    fun setOwner(owner: LivingEntity?) {
        this.owner = owner
        this.ownerUUID = owner?.uuid
    }

    fun getOwner(): LivingEntity? = owner

    override fun tick(time: Long) {
        super.tick(time)
        if (--warmupDelayTicks < 0) {
            if (warmupDelayTicks == -8) {
                instance.entities
                    .filterIsInstance<LivingEntity>()
                    .filterNot { it == this || it == owner }
                    .filter { boundingBox.intersectEntity(position, it) }
                    .forEach { dealDamageTo(it) }
            }

            if (!sentSpikeEvent) {
                //sendEntityEvent(4)
                playSoundEvent(SoundEvent.ENTITY_EVOKER_FANGS_ATTACK, 1.0f, Random.nextFloat() * 0.2f + 0.85f)

                sentSpikeEvent = true
            }

            if (--lifeTicks < 0) {
                remove()
            }
        }
    }

    private fun dealDamageTo(entity: LivingEntity) {
        val owner = this.owner
        if (!entity.isDead && entity != owner) {
            val damage = if (owner == null) {
                Damage(DamageType.MAGIC, this, owner, position, 6f)
            } else {
                if (owner.isAlliedTo(entity)) return
                Damage(DamageType.INDIRECT_MAGIC, this, owner, position, 6f)
            }
            entity.damage(damage)
        }
    }

    fun getAnimationProgress(delta: Float): Float {
        if (!attackStarted) return 0.0f
        val ticks = lifeTicks - 2
        return if (ticks <= 0) 1.0f else 1.0f - ((ticks - delta) / 20.0f)
    }

}
