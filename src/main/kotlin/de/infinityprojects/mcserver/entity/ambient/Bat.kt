package de.infinityprojects.mcserver.entity.ambient

import net.minestom.server.coordinate.Pos
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.instance.block.BlockFace
import net.minestom.server.sound.SoundEvent
import kotlin.math.floor
import kotlin.math.sign
import kotlin.random.Random

class Bat : EntityAmbient(EntityType.BAT) {
    var targetPosition: Pos? = null

    init {
        setResting(true)
    }

    override fun tick(time: Long) {
        super.tick(time)

        if (isResting()) {
            velocity = Vec.ZERO
            position = Pos(
                position.x,
                floor(position.y + 1.0 - boundingBox.maxY()),
                position.z
            )
        } else {
            velocity = velocity.mul(1.0, 0.6, 1.0)
        }
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        val pos = position;
        val posAbove = pos.relative(BlockFace.TOP)

        if (isResting()) {
            if (instance.getBlock(posAbove).isSolid) {
                if (Random.Default.nextInt(200) == 0) {
                    val yRot = Random.Default.nextInt(360)
                    position = pos.withYaw(yRot.toFloat())
                }

                if (instance.getNearbyEntities(pos, 4.0).any { e -> e is Player }) {
                    setResting(false)
                }
            } else {
                setResting(false)
            }
        } else {
            var targetPos = targetPosition
            if (targetPos != null && (!instance.getBlock(targetPos).isAir || targetPos.y <= instance.cachedDimensionType.minY())) {
                targetPos = null
            }
            if (targetPos == null || Random.Default.nextInt(30) == 0 || targetPos.distanceSquared(pos) < 4.0) {
                val offsetX = Random.Default.nextInt(-7, 8).toDouble()
                val offsetY = Random.Default.nextInt(6).toDouble() - 2.0
                val offsetZ = Random.Default.nextInt(-7, 8).toDouble()
                targetPos = pos.add(offsetX, offsetY, offsetZ)
            }

            val dX = targetPos.x + 0.5 - pos.x
            val dY = targetPos.y + 0.1 - pos.y
            val dZ = targetPos.z + 0.5 - pos.z

            val vel = velocity
            val newVel = vel.add(
                (sign(dX) * 0.5 - vel.x),
                (sign(dY) * 0.7 - vel.y),
                (sign(dZ) * 0.5 - vel.z),
            ).mul(10.0)

            velocity = newVel
            if (Random.Default.nextInt(100) == 0 && instance.getBlock(posAbove).isSolid) {
                setResting(true)
            }

            val norm = vel.normalize()
            val headDir = pos.add(norm)

            lookAt(headDir)

            targetPosition = targetPos
        }
    }

    fun isResting(): Boolean {
        return metadata[MetadataDef.Bat.IS_HANGING]
    }

    fun setResting(flag: Boolean) {
        metadata[MetadataDef.Bat.IS_HANGING] = flag
    }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 6.0
    }

    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_BAT_DEATH

    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_BAT_HURT

    override fun ambientSound(): SoundEvent? =
        if (isResting() && Random.Default.nextInt(4) != 0) null else SoundEvent.ENTITY_BAT_AMBIENT

    override fun soundPitch(): Float = super.soundPitch() * 0.95f

    override fun soundVolume(): Float = 0.1f
}