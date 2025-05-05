package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.AgeableEntity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.animal.AnimalMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle
import kotlin.random.Random

open class Animal<M : AnimalMeta>(type: EntityType): AgeableEntity<M>(type) {
    var parent: Animal<M>? = null
        private set(animal) {
            if (parent != null) {
                throw IllegalStateException("Animal already has a parent")
            }
            parent = animal
        }

    var inLove = 0

    val isInLove
        get() = inLove > 0

    override fun aiTick(time: Long) {
        super.aiTick(time)

        if (age != 0) {
            inLove = 0
        }

        if (inLove > 0) {
            --inLove
            if (inLove % 10 == 0) {
                val oX = Random.Default.nextFloat() * 0.2f + 0.1f
                val oY = Random.Default.nextFloat() * 0.2f + 0.1f
                val oZ = Random.Default.nextFloat() * 0.2f + 0.1f

                val pos = position
                val pX = pos.x + Random.Default.nextDouble() * 2.0 - 1.0
                val pY = pos.y + Random.Default.nextDouble() * 2.0 - 1.0
                val pZ = pos.z + Random.Default.nextDouble() * 2.0 - 1.0

                val particle = ParticlePacket(Particle.HEART, pX, pY, pZ, oX, oY, oZ, 1.0f, 1)
                instance.sendGroupedPacket(particle)
            }
        }
    }

    final override fun setSuperclassAttributes() {
        getAttribute(Attribute.TEMPT_RANGE).baseValue = 10.0
    }

    open fun isFood(item: ItemStack): Boolean {
        return false
    }

    open fun canMateWith(other: Animal<out AnimalMeta>): Boolean {
        if (this == other) {
            return false
        }

        if (this.entityType != other.entityType) {
            return false
        }

        if (other.isBaby || this.isBaby) {
            return false
        }

        if (this.isInLava || other.isInLava) {
            return false
        }

        return true
    }
}