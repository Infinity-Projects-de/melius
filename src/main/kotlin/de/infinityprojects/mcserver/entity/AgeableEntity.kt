package de.infinityprojects.mcserver.entity

import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.metadata.AgeableMobMeta
import net.minestom.server.particle.Particle
import kotlin.random.Random

abstract class AgeableEntity<M : AgeableMobMeta>(type: EntityType): PathfinderMob<M>(type) {
    var isBaby: Boolean
        get() = age < 0
        private set(value) {
            age = if (value) BABY_INITIAL_AGE else 0
            metadata[MetadataDef.AgeableMob.IS_BABY] = value
        }

    var age = 0
        private set(value) {
            if (field < 0 && value >= 0) {
                isBaby = false
                ageBoundaryReached()
            }
            field = value
        }

    private var forcedAgeTimer = 0
        set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Forced age timer cannot be negative")
            }
            field = value
        }

    fun ageUp(i: Int) {
        ageUp(i, true)
    }

    fun ageUp(i: Int, showParticles: Boolean) {
        age += i * 20

        if (showParticles) {
            forcedAgeTimer = 40
        }
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        if (forcedAgeTimer > 0) {
            if (forcedAgeTimer % 4 == 0) {
                val rX = Random.Default.nextInt(2) - 1
                val rZ = Random.Default.nextInt(2) - 1
                val rY = Random.Default.nextInt(1) - 0.5
                val pos = position.add(rX.toDouble(), rY, rZ.toDouble())
                spawnParticle(Particle.HAPPY_VILLAGER, pos, Vec.ZERO, 0f, 1)
            }

            --forcedAgeTimer
        }

        if (isAlive) {
            if (age < 0) {
                age++
            }
        }
    }

    open fun ageBoundaryReached() {
        if (!isBaby) {
            // TODO: Boat thing
        }
    }

    companion object {
        const val BABY_INITIAL_AGE = -24000

        fun speedUpSecondsWhenFeeding(seconds: Int): Int {
            return ((seconds / 20).toFloat() * 0.1f).toInt()
        }
    }


}