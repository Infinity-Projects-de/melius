package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.PathfinderMob
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.item.ItemStack
import net.minestom.server.network.packet.server.play.ParticlePacket
import net.minestom.server.particle.Particle

open class Animal(type: EntityType): PathfinderMob(type) {
    var parent: Animal? = null
        private set(animal) {
            if (parent != null) {
                throw IllegalStateException("Animal already has a parent")
            }
            parent = animal
        }

    var inLove = 0
    var age = 0
        private set(value) {
            if (value < 0) {
                throw IllegalArgumentException("Age cannot be negative")
            }
            if (value < age) {
                throw IllegalArgumentException("Age cannot be decreased")
            }
            age = value
        }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        if (this.age != 0) {
            this.inLove = 0
        }

        if (this.inLove > 0) {
            --this.inLove
            if (this.inLove % 10 == 0) {
                val oX = random.nextFloat() * 0.2f + 0.1f
                val oY = random.nextFloat() * 0.2f + 0.1f
                val oZ = random.nextFloat() * 0.2f + 0.1f

                val pos = position
                val pX = pos.x + random.nextDouble() * 2.0 - 1.0
                val pY = pos.y + random.nextDouble() * 2.0 - 1.0
                val pZ = pos.z + random.nextDouble() * 2.0 - 1.0

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

    val isBaby: Boolean
        get() = metadata[MetadataDef.AgeableMob.IS_BABY] ?: false
}