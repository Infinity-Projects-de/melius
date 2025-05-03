package de.infinityprojects.mcserver.entity

import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.particle.Particle
import kotlin.random.Random

abstract class AgeableEntity(type: EntityType): PathfinderMob(type) {
    var isBaby: Boolean
        get() = age < 0
        private set(value) {
            age = if (value) -24000 else 0
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
        ageUp(i, false)
    }

    fun ageUp(i: Int, flag: Boolean) {
        age += i * 20

        if (flag) {
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

    fun ageBoundaryReached() {
        if (!isBaby) {
            // TODO: Boat thing
        }
    }


}