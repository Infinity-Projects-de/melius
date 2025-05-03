package de.infinityprojects.mcserver.entity.animal.axolotl

import de.infinityprojects.mcserver.entity.animal.Animal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.water.AxolotlMeta
import net.minestom.server.instance.Weather
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import kotlin.math.min
import kotlin.random.Random

class Axolotl : Animal(EntityType.AXOLOTL) {
    enum class Variant(val variant: AxolotlMeta.Variant, val common: Boolean) {
        LUCY(AxolotlMeta.Variant.LUCY, true),
        WILD(AxolotlMeta.Variant.WILD, true),
        GOLD(AxolotlMeta.Variant.GOLD, true),
        CYAN(AxolotlMeta.Variant.CYAN, true),
        BLUE(AxolotlMeta.Variant.BLUE, false);

        companion object {
            fun commonVariant(): Variant {
                return Variant.entries
                    .filter { it.common }
                    .random()
            }

            fun rareVariant(): Variant {
                return Variant.entries
                    .filter { !it.common }
                    .random()
            }

            fun fromId(id: Int): Variant {
                return Variant.entries.firstOrNull { it.ordinal == id } ?: LUCY
            }
        }
    }


    companion object {
        const val TOTAL_PLAYDEAD_TIME = 200
        const val AXOLOTL_TOTAL_AIR_SUPPLY = 6000
        const val REHYDRATE_AIR_SUPPLY = 1800

        fun useRareVariant(): Boolean = Random.Default.nextInt(1200) == 0
    }

    var variant: Variant
        get() = Variant.fromId(metadata[MetadataDef.Axolotl.VARIANT])
        set(value) {
            metadata[MetadataDef.Axolotl.VARIANT] = value.ordinal
        }

    var playingDead: Boolean
        get() = metadata[MetadataDef.Axolotl.IS_PLAYING_DEAD] ?: false
        set(value) {
            metadata[MetadataDef.Axolotl.IS_PLAYING_DEAD] = value
        }

    var fromBucket: Boolean
        get() = metadata[MetadataDef.Axolotl.IS_FROM_BUCKET] ?: false
        set(value) {
            metadata[MetadataDef.Axolotl.IS_FROM_BUCKET] = value
        }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 14.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 1.0
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 2.0
        getAttribute(Attribute.STEP_HEIGHT).baseValue = 1.0
        super.setAttributes()
    }

    fun rehydrate() {
        val maxAir = maxAirSupply()
        val new = min(health + REHYDRATE_AIR_SUPPLY, maxAir.toFloat())
        health = new
    }

    override fun maxAirSupply() = AXOLOTL_TOTAL_AIR_SUPPLY

    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_AXOLOTL_HURT
    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_AXOLOTL_DEATH
    override fun ambientSound(): SoundEvent? = if (playingDead) null
    else if (isInWater) SoundEvent.ENTITY_AXOLOTL_IDLE_WATER
    else SoundEvent.ENTITY_AXOLOTL_IDLE_AIR

    override fun isFood(stack: ItemStack): Boolean {
        val food = setOf(
            Material.TROPICAL_FISH, Material.TROPICAL_FISH_BUCKET
        )
        return stack.material() in food
    }

    fun getBucketItemStack(): ItemStack = ItemStack.of(Material.AXOLOTL_BUCKET)

    override fun damage(damage: Damage): Boolean {
        val isRandomChanceTriggered = Random.Default.nextInt(3) == 0
        val isDamageSignificant = Random.Default.nextInt(3) < damage.amount || health / maxHealth < 0.5f
        val canSurviveDamage = damage.amount < health
        val hasDamageSource = damage.source != null
        val isAIEnabled = !noAI
        val isNotAlreadyPlayingDead = !playingDead

        if (isAIEnabled &&
            isRandomChanceTriggered &&
            isDamageSignificant &&
            canSurviveDamage &&
            isInWater &&
            hasDamageSource &&
            isNotAlreadyPlayingDead) {
            // brain -> playDeadTicks = 200
        }

        return super.damage(damage)
    }

    override fun playAttackSound() = playSoundEvent(SoundEvent.ENTITY_AXOLOTL_ATTACK, 1.0f, 1.0f)
    override fun pickupSound(): SoundEvent = SoundEvent.ITEM_BUCKET_FILL_AXOLOTL

    override fun canBeLeashed() = true

    override fun tick(time: Long) {
        super.tick(time)
        if (!noAI) {
            val instance = this.instance
            if (instance != null) {

                if (isAlive && isInWaterOrRain()) {
                    airSupply = airSupply - 1
                    if (airSupply == -20) {
                        airSupply = 0
                        damage(DamageType.DRY_OUT, 2f)
                    }
                } else {
                    airSupply = maxAirSupply()
                }
            }
        }

        if (isInWater) {
            velocity = velocity.mul(0.9)
        }
    }

    fun isInWaterOrRain(): Boolean {
        val instance = this.instance ?: return false
        return isInWater || instance.weather == Weather.RAIN || instance.weather == Weather.THUNDER
    }

    fun removeWhenFarAway(): Boolean =
        !fromBucket && !hasCustomName
}
