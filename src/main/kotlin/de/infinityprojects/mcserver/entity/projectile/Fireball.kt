package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.spawnParticle
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.particle.Particle

abstract class Fireball(owner: Entity? = null, type: EntityType) : Projectile(owner, type) {
    var accelerationPower: Double = 0.1

    override fun tick(time: Long) {
        applyInertia()

        if (!isRemoved && instance != null) {
            val colResult = moveUntilCollision { entity ->
                canHitEntity(entity)
            }
            val nextPos = colResult?.position ?: position.add(velocity)

            teleport(nextPos)
            super.tick(time)

            if (shouldBurn()) {
                setOnFireDuration(20L)
            }

            if (colResult != null && isAlive()) {
                hitTargetOrDeflectSelf(colResult.entity)
            }

            createParticleTrail()
        } else {
            remove()
        }
    }

    private fun applyInertia() {
        val currentVel = velocity
        val norm = currentVel.normalize()
        val extraAccel = norm.mul(accelerationPower)
        val inLiquid = isInLiquid()
        val inertia = if (inLiquid) getLiquidInertia() else getInertia()

        if (inLiquid) {
            repeat(4) {
                val particlePos = position.sub(currentVel.mul(0.25))
                spawnParticle(Particle.BUBBLE, particlePos, Vec.ZERO, 1.0f, 1)
            }
        }

        velocity = currentVel.add(extraAccel).mul(inertia)
    }

    private fun createParticleTrail() {
        val particle = getTrailParticle()
        if (particle != null) {
            val pos = position.add(0.0, 0.5, 0.0)
            spawnParticle(particle, pos, Vec.ZERO, 0f, 1)
        }
    }

    protected open fun shouldBurn(): Boolean = true

    protected open fun getTrailParticle(): Particle? = Particle.SMOKE

    protected open fun getInertia(): Double = 0.95

    protected open fun getLiquidInertia(): Double = 0.8

    fun assignDirectionalMovement(direction: Vec, power: Double) {
        velocity = direction.normalize().mul(power)
    }

    open fun hitTargetOrDeflectSelf(entity: Entity?) {
        if (entity != null && entity != shooter && entity is LivingEntity) {
            val damage = Damage(DamageType.FIREBALL, this, shooter, position, 0f)
            entity.damage(damage)
        }
        val sound = hitSound()
        if (sound != null) {
            playSoundEvent(sound, 1f, 1f)
        }
    }

    open fun setOnFireDuration(ticks: Long) {
        fireFor = ticks
    }

    open fun isAlive(): Boolean = !isRemoved

    open fun moveUntilCollision(canHit: (Entity) -> Boolean): CollisionResult? {

        val blockCollision = instance?.getNearbyEntities(position, 1.0)
        if (blockCollision != null) {
            for (entity in blockCollision) {
                if (canHit(entity)) {
                    val collisionResult = CollisionResult(position, entity)
                    return collisionResult
                }
            }
        }

        return null
    }

    open var fireFor: Long = 0L

    open fun isInLiquid(): Boolean {
        val block = instance?.getBlock(position)
        return block?.isLiquid == true
    }

    open fun onDeflection(entity: Entity?, deflected: Boolean) {
        if (deflected) {
            accelerationPower = 0.1
        } else {
            accelerationPower *= 0.5
        }
    }

    data class CollisionResult(val position: Pos, val entity: Entity?)
}

