package de.infinityprojects.mcserver.entity.element

import de.infinityprojects.mcserver.entity.PathfinderMob
import de.infinityprojects.mcserver.entity.SoundEmitter
import net.kyori.adventure.sound.Sound
import net.minestom.server.collision.BoundingBox
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.PathfinderMobMeta
import net.minestom.server.instance.Instance
import net.minestom.server.instance.block.Block
import net.minestom.server.sound.SoundEvent
import kotlin.random.Random

class LightningBolt(
    val visualOnly: Boolean = false
) : Entity(EntityType.LIGHTNING_BOLT), SoundEmitter {

    var life: Int = 2
    var flashes: Int = Random.nextInt(3) + 1
    var seed: Long = Random.nextLong()
    var cause: Player? = null
    private val hitEntities: MutableSet<PathfinderMob<out PathfinderMobMeta>> = mutableSetOf()
    var blocksSetOnFire: Int = 0

    override fun soundSource(): Sound.Source = Sound.Source.WEATHER

    fun setVisualOnly(flag: Boolean) {

    }

    fun setCause(player: Player?) {
        cause = player
    }

    override fun tick(time: Long) {
        super.tick(time)
        if (life == 2) {
            playThunderSound()
            if (!visualOnly) {
                spawnFire(4)
                powerLightningRod()
                clearCopperOnLightningStrike()
            }
        }

        --life
        if (life < 0) {
            if (flashes == 0) {
                remove()
            } else if (life < -Random.nextInt(10)) {
                --flashes
                life = 1
                seed = Random.nextLong()
                spawnFire(0)
            }
        }

        if (life >= 0) {
            if (!visualOnly) {
                val bb = BoundingBox.fromPoints(
                    position.sub(3.0, 3.0, 3.0),
                    position.add(3.0, 6.0, 3.0)
                )
                val entities = instance.getNearbyEntities(position, 3.0)
                    .filterIsInstance<PathfinderMob<out PathfinderMobMeta>>()
                    .filter { it.isAlive && it != this }

                for (entity in entities) {
                    if (entity !in hitEntities) {
                        hitEntities.add(entity)
                        entity.damage(Damage(DamageType.LIGHTNING_BOLT, this, this, position, 5f))
                    }
                }
            }
        }
    }

    private fun playThunderSound() {
        playSoundEvent(SoundEvent.ENTITY_LIGHTNING_BOLT_THUNDER, 10000.0f, 0.8f + Random.nextFloat() * 0.2f)
        playSoundEvent(SoundEvent.ENTITY_LIGHTNING_BOLT_IMPACT, 2.0f, 0.5f + Random.nextFloat() * 0.2f)
    }

    private fun powerLightningRod() {
        // Lightning rod block logic (stubbed, as Minestom doesn't implement vanilla blocks)
    }

    private fun getStrikePosition(): Pos {
        return Pos(position.x, position.y - 1.0E-6, position.z)
    }

    private fun spawnFire(count: Int) {
        if (visualOnly) return
        val inst: Instance = instance
        val basePos = getStrikePosition()
        fun setFire(pos: Pos) {
            val block = inst.getBlock(pos)
            if (block == Block.AIR) {
                // Instantiating fire logic (stub, as block implementation is minimal)
                inst.setBlock(pos, Block.FIRE)
                blocksSetOnFire++
            }
        }
        setFire(basePos)
        repeat(count) {
            val randPos = basePos.add(
                (Random.nextInt(3) - 1).toDouble(),
                (Random.nextInt(3) - 1).toDouble(),
                (Random.nextInt(3) - 1).toDouble()
            )
            setFire(randPos)
        }
    }

    private fun clearCopperOnLightningStrike() {
        // Stub: No copper/weathering implementation in Minestom by default.
    }

    fun getBlocksSetOnFire(): Int = blocksSetOnFire

    fun getHitEntities(): Sequence<Entity> = hitEntities.asSequence().filter { it.isAlive }
}
