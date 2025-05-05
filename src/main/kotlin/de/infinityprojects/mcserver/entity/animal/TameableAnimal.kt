package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.ai.goal.PanicGoal
import de.infinityprojects.mcserver.entity.spawnParticle
import de.infinityprojects.mcserver.server.MeliusServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.entity.metadata.animal.tameable.TameableAnimalMeta
import net.minestom.server.particle.Particle
import java.util.*
import kotlin.math.abs
import kotlin.random.Random

open class TameableAnimal<M: TameableAnimalMeta>(type: EntityType): Animal<M>(type) {
    var sitting: Boolean
        get() = typedMeta.isSitting
        set(value) {
            typedMeta.isSitting = value
        }

    var isTamed: Boolean
        get() = typedMeta.isTamed
        set(value) {
            typedMeta.isTamed = value
        }

    var ownerUUID: UUID?
        get() = typedMeta.owner
        set(value) {
            typedMeta.owner = value
        }

    var owner: Player? = null
        get() {
            if (field == null && ownerUUID != null) {
                field = MeliusServer.playerManager.getPlayerByUUID(ownerUUID!!)
            }
            return field
        }
        set(value) {
            field = value
            ownerUUID = value?.uuid
        }

    fun spawnTamingParticles(fail: Boolean) {
        val particle = if (fail) Particle.SMOKE else Particle.HEART

        repeat(7) {
            val dX = Random.Default.nextDouble() * 0.02;
            val dY = Random.Default.nextDouble() * 0.02;
            val dZ = Random.Default.nextDouble() * 0.02;

            val pX = Random.Default.nextDouble(-0.5, 0.5)
            val pY = Random.Default.nextDouble(Random.Default.nextDouble()) + 0.5
            val pZ = Random.Default.nextDouble(-0.5, 0.5)

            spawnParticle(particle, position.add(pX, pY, pZ), Vec(dX, dY, dZ), 0f, 1)
        }
    }

    fun tame(player: Player) {
        isTamed = true
        owner = player
    }

    override fun canAttack(target: Entity): Boolean {
        if (owner == target.uuid) {
            return false
        }
        return super.canAttack(target)
    }

    fun tryToTeleportToOwner() {
        val owner = owner ?: return
        if (owner is Player && owner.isOnline) {
            teleport(owner.position)
        }
    }

    fun shouldTryTeleportToOwner(): Boolean {
        val owner = this.owner
        return owner != null && getDistanceSquared(owner) >= 144
    }

    private fun teleportToAroundPos(pos: Pos) {
        for (i in 0..10) {
            val j = pos.x + Random.Default.nextDouble(-3.0, 3.0)
            val k = pos.y + Random.Default.nextDouble(-3.0, 2.0)

            if (abs(j) >= 2 || abs(k) >= 2) {
                val l = pos.z + Random.Default.nextDouble(-1.0, 1.0)
                val teleportPos = pos.add(j, k, l)
                if (canTeleportTo(teleportPos)) {
                    teleport(pos.add(j, l, k))
                    return
                }
            }
        }
    }

    fun unableToMoveToOwner(): Boolean {
        return sitting || isPassenger || owner != null && owner!!.gameMode == GameMode.SPECTATOR
    }

    inner class TameableAnimalPanicGoal(speed: Double): PanicGoal(this, speed) {
        override fun tick(time: Long) {
            if (!this@TameableAnimal.unableToMoveToOwner() && this@TameableAnimal.shouldTryTeleportToOwner()) {
                this@TameableAnimal.tryToTeleportToOwner()
            }

            super.tick(time);
        }
    }
}