package de.infinityprojects.mcserver.entity.element

import de.infinityprojects.mcserver.entity.PathfinderMob
import de.infinityprojects.mcserver.entity.spawnParticle
import net.minestom.server.color.Color
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.metadata.PathfinderMobMeta
import net.minestom.server.entity.metadata.other.AreaEffectCloudMeta
import net.minestom.server.item.component.PotionContents
import net.minestom.server.particle.Particle
import net.minestom.server.potion.CustomPotionEffect
import net.minestom.server.potion.Potion
import java.util.*
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class AreaEffectCloud(
    x: Double = 0.0,
    y: Double = 0.0,
    z: Double = 0.0
) : Entity(EntityType.AREA_EFFECT_CLOUD) {
    var potionContents: PotionContents = PotionContents.EMPTY
    var potionDurationScale: Float = 1.0f
    val victims: MutableMap<LivingEntity, Long> = mutableMapOf()
    var duration: Int = -1
    var waitTime: Int = 20
    var reapplicationDelay: Int = 20
    var durationOnUse: Int = 0
    var radiusOnUse: Float = 0.0f
    var radiusPerTick: Float = 0.0f

    var owner: LivingEntity? = null
        get() {
            if (field != null && !field!!.isRemoved) {
                return field
            }
            if (ownerUUID != null) {
                val e = instance?.getEntityByUuid(ownerUUID)
                if (e is LivingEntity) {
                    field = e
                    return field
                }
            }
            return field
        }
        set(value) {
            field = value
            ownerUUID = value?.uuid
        }

    var ownerUUID: UUID? = null

    val typedMeta: AreaEffectCloudMeta
        get() = entityMeta as? AreaEffectCloudMeta ?: throw IllegalStateException("Meta is not of type AreaEffectCloudMeta")

    init {
        setNoGravity(true)
        teleport(Pos(x, y, z))
        setBoundingBox(radius * 2.0, 0.5, radius * 2.0)
    }

    var radius: Float
        get() = typedMeta.radius
        set(value) {
            typedMeta.radius = value
        }

    var color: Int
        get() = typedMeta.color
        set(value) {
            typedMeta.color = value
        }

    var isSinglePoint: Boolean
        get() = typedMeta.isSinglePoint
        set(value) {
            typedMeta.isSinglePoint = value
        }

    var particle: Particle
        get() = typedMeta.particle
        set(value) {
            typedMeta.particle = value
        }


    fun setPotionContents(value: PotionContents) {
        potionContents = value
        updateColor()
    }

    fun setPotionDurationScale(value: Float) {
        potionDurationScale = value
    }

    fun updateColor() {
        // Typical vanilla logic below: color gets set based on potion contents
        if (potionContents == PotionContents.EMPTY) {
            particle = Particle.ENTITY_EFFECT

            color = 0
        } else {
            // In real implementation, ARGB conversion and matching Minestom ParticleData needed
            val rgb = potionContents.customColor ?: Color.WHITE
            particle = Particle.ENTITY_EFFECT.withColor(rgb)

            val colorInt = Color.fromRGBLike(rgb).asRGB()
            color = colorInt
        }

    }

    fun addEffect(effect: CustomPotionEffect) {
        potionContents.customEffects.add(effect)
    }

    override fun tick(time: Long) {
        super.tick(time)
        if (instance == null) return

        if (duration != -1 && aliveTicks >= waitTime + duration) {
            remove()
            return
        }

        if (aliveTicks < waitTime) {
            var r = radius
            if (radiusPerTick != 0.0f) {
                r += radiusPerTick
                if (r < 0.5f) {
                    remove()
                    return
                }
                radius = r
            }

            if (aliveTicks % 5 == 0L) {
                victims.entries.removeIf { aliveTicks >= it.value }
                if (!potionContents.customEffects.isNotEmpty()) {
                    victims.clear()
                } else {
                    val effects = potionContents.customEffects.map { Potion(it.id, it.amplifier(), duration) }
                    val entities = instance!!.getNearbyEntities(position, r.toDouble())
                        .filterIsInstance<PathfinderMob<out PathfinderMobMeta>>()
                        .filter { it.getDistanceSquared(this) <= r * r }
                    for (entity in entities) {
                        if (!victims.containsKey(entity)) {
                            val match = effects.any { entity.canBeAffected(it.effect) }
                            if (match) {
                                victims[entity] = aliveTicks + reapplicationDelay
                                for (effect in effects) {
                                    if (entity.canBeAffected(effect.effect))
                                    entity.addEffect(effect)
                                }
                                if (radiusOnUse != 0.0f) {
                                    r += radiusOnUse
                                    if (r < 0.5f) {
                                        remove()
                                        return
                                    }
                                    radius = r
                                }
                                if (durationOnUse != 0 && duration != -1) {
                                    duration += durationOnUse
                                    if (duration <= 0) {
                                        remove()
                                        return
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        particles()
    }

    fun particles() {
        val r = radius
        if (Random.Default.nextBoolean()) {
            val p = particle
            val count = ceil(Math.PI * r * r).toInt()
            val spread = r
            repeat(count) {
                val angle = Random.Default.nextFloat() * (2f * Math.PI)
                val mag = sqrt(Random.Default.nextFloat()) * spread
                val px = position.x + cos(angle) * mag
                val py = position.y
                val pz = position.z + sin(angle) * mag
                spawnParticle(particle, position, position.add(px, py, pz), 0f, count,)
            }
        }
    }

    fun setRadiusOnUse(v: Float) { radiusOnUse = v }
    fun setRadiusPerTick(v: Float) { radiusPerTick = v }
    fun setDurationOnUse(v: Int) { durationOnUse = v }
    fun setWaitTime(v: Int) { waitTime = v }
}
