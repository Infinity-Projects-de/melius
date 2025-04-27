package de.infinityprojects.mcserver.entity.projectile

import de.infinityprojects.mcserver.entity.spawnParticle
import net.kyori.adventure.sound.Sound
import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.*
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.event.entity.projectile.ProjectileCollideWithBlockEvent
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.particle.Particle
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.sound.SoundEvent
import net.minestom.server.utils.Direction
import java.util.*
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

class ShulkerBullet(
    val owner: LivingEntity?,
    var finalTarget: Entity?,
    val axis: Axis? = null
) : Projectile(owner, EntityType.SHULKER_BULLET) {

    companion object {
        private const val SPEED = 0.15
    }

    private var currentMoveDirection: BlockFace? = BlockFace.TOP
    private var flightSteps: Int = 0
    private var targetDelta = Vec.ZERO
    private var targetId: UUID? = null

    constructor() : this(null, null, null)

    init {
        this.setNoGravity(true)
        this.velocity = Vec.ZERO
    }

    override fun tick(time: Long) {
        super.tick(time)
        // Handle target re-acquire if needed
        if (finalTarget == null && targetId != null) {
            finalTarget = instance.getEntityByUuid(targetId!!)
            if (finalTarget == null) targetId = null
        }

        // Retargeting or dead target logic
        if (finalTarget == null || !finalTarget!!.isRemoved || (finalTarget is Player && (finalTarget as Player).gameMode == GameMode.SPECTATOR)) {
            // Apply gravity since no target
            applyGravity()
        } else {
            // Accelerate deltas
            targetDelta = Vec(
                clamp(targetDelta.x * 1.025, -1.0, 1.0),
                clamp(targetDelta.y * 1.025, -1.0, 1.0),
                clamp(targetDelta.z * 1.025, -1.0, 1.0)
            )
            // Pursue target
            velocity = velocity.add(
                (targetDelta.x - velocity.x) * 0.2,
                (targetDelta.y - velocity.y) * 0.2,
                (targetDelta.z - velocity.z) * 0.2,
            )
        }

        // Movement hit detection

       /* val hit = ProjectileHelper.getHitResultOnMoveVector(this) { canHitEntity(it) }
        // Update position
        position = position.add(velocity)
        // Effects
        if (hit != null && !isRemoved && hit.type != ProjectileHelper.HitType.MISS) {
            hitTargetOrDeflectSelf(hit)
        }*/


        if (finalTarget != null && !finalTarget!!.isRemoved) {
            if (flightSteps > 0) {
                flightSteps--
                if (flightSteps == 0) {
                    selectNextMoveDirection(currentMoveDirection?.toDirection()?.axis())
                }
            }
            if (currentMoveDirection != null) {
                val blockpos = position
                val axis = currentMoveDirection!!.toDirection().axis()
                if (instance.getBlock(blockpos.relative(currentMoveDirection!!)).isSolid) {
                    selectNextMoveDirection(axis)
                } else {
                    val targetBlock = finalTarget!!.position
                    if ((axis == Axis.X && blockpos.x == targetBlock.x) ||
                        (axis == Axis.Y && blockpos.y == targetBlock.y) ||
                        (axis == Axis.Z && blockpos.z == targetBlock.z)
                    ) {
                        selectNextMoveDirection(axis)
                    }
                }
            }
        }
        // Particle emission for client handled elsewhere (Minestom particle update)
    }

    private fun selectNextMoveDirection(axis: Axis?) {
        val d0 = if (finalTarget == null) 0.5 else (finalTarget!!.boundingBox.height() * 0.5)
        val blockposition = finalTarget?.let {
            Pos(it.position.x, it.position.y + d0, it.position.z)
        } ?: position.sub(0.0, 1.0, 0.0)

        var d1 = blockposition.x + 0.5
        var d2 = blockposition.y + d0
        var d3 = blockposition.z + 0.5
        var dir: BlockFace? = null
        if (blockposition.distanceSquared(position) > 4.0) {
            val start = position
            val list = mutableListOf<BlockFace>()
            if (axis != Axis.X) {
                if (start.x < blockposition.x && instance.getBlock(start.relative(BlockFace.EAST)).isAir)
                    list.add(BlockFace.EAST)
                else if (start.x > blockposition.x && instance.getBlock(start.relative(BlockFace.WEST)).isAir)
                    list.add(BlockFace.WEST)
            }
            if (axis != Axis.Y) {
                if (start.y < blockposition.y && instance.getBlock(start.relative(BlockFace.TOP)).isAir)
                    list.add(BlockFace.TOP)
                else if (start.y > blockposition.y && instance.getBlock(start.relative(BlockFace.BOTTOM)).isAir)
                    list.add(BlockFace.BOTTOM)
            }
            if (axis != Axis.Z) {
                if (start.z < blockposition.z && instance.getBlock(start.relative(BlockFace.SOUTH)).isAir)
                    list.add(BlockFace.SOUTH)
                else if (start.z > blockposition.z && instance.getBlock(start.relative(BlockFace.NORTH)).isAir)
                    list.add(BlockFace.NORTH)
            }
            dir = if (list.isEmpty()) {
                var fallback = BlockFace.entries.random()
                repeat(5) {
                    if (instance.getBlock(start.relative(fallback)).isAir) return@repeat
                    fallback = BlockFace.entries.random()
                }
                fallback
            } else {
                list.random()
            }
            val d = dir.toDirection()
            d1 = position.x + d.normalX()
            d2 = position.y + d.normalY()
            d3 = position.z + d.normalZ()
        }
        setMoveDirection(dir)
        val dx = d1 - position.x
        val dy = d2 - position.y
        val dz = d3 - position.z
        val d7 = sqrt(dx * dx + dy * dy + dz * dz)
        if (d7 == 0.0) {
            targetDelta = Vec.ZERO
        } else {
            targetDelta = Vec(dx / d7 * SPEED, dy / d7 * SPEED, dz / d7 * SPEED)
        }
        flightSteps = 10 + Random.Default.nextInt(5) * 10
    }

    private fun setMoveDirection(direction: BlockFace?) {
        this.currentMoveDirection = direction
    }

    override fun onProjectileHit(hitEvent: ProjectileCollideWithEntityEvent) {
        val entity = hitEvent.entity
        if (entity is LivingEntity) {
            entity.damage(DamageType.MOB_PROJECTILE, 4f)
            entity.addEffect(
                Potion(
                    PotionEffect.LEVITATION,
                    200,
                    1,
                )
            )
        }

        playSoundEvent(SoundEvent.ENTITY_SHULKER_BULLET_HIT, soundVolume(), soundPitch())
        remove()
    }

    override fun onProjectileCollide(collideEvent: ProjectileCollideWithBlockEvent) {
        super.onProjectileCollide(collideEvent)
        spawnParticle(Particle.EXPLOSION, position, Vec(0.2, 0.2, 0.2), 0f, 2)
        playSoundEvent(SoundEvent.ENTITY_SHULKER_BULLET_HIT, soundVolume(), soundPitch())
        remove()
    }

    override fun soundSource(): Sound.Source = Sound.Source.HOSTILE

    private fun clamp(value: Double, minV: Double, maxV: Double): Double =
        max(minV, min(value, maxV))

    enum class Axis { X, Y, Z }

    fun Direction.axis(): Axis {
        return when (this) {
            Direction.EAST, Direction.WEST -> Axis.X
            Direction.UP, Direction.DOWN -> Axis.Y
            Direction.SOUTH, Direction.NORTH -> Axis.Z
        }
    }

}
