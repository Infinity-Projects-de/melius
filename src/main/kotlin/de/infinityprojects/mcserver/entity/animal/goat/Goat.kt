package de.infinityprojects.mcserver.entity.animal.goat

import de.infinityprojects.mcserver.entity.InteractionResult
import de.infinityprojects.mcserver.entity.animal.Animal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.Player
import net.minestom.server.entity.PlayerHand
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.animal.GoatMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent
import kotlin.random.Random

class Goat: Animal<GoatMeta>(EntityType.GOAT) {
    private var isScreamingGoat: Boolean
        get() = typedMeta.isScreaming
        set(value) {
            typedMeta.isScreaming = value
        }

    private var hasLeftHorn: Boolean
        get() = typedMeta.hasLeftHorn()
        set(value) {
            typedMeta.setLeftHorn(value)
        }

    private var hasRightHorn: Boolean
        get() = typedMeta.hasRightHorn()
        set(value) {
            typedMeta.setRightHorn(value)
        }

    init {
        // canfloat
    }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 10.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.2
        setAgeDependantValues()
    }

    override fun ageBoundaryReached() {
        super.ageBoundaryReached()
        setAgeDependantValues()
    }

    fun setAgeDependantValues() {
        if (isBaby) {
            getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 1.0
            removeHorns()
        } else {
            getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 2.0
            addHorns()
        }
    }

    fun createHorn(): ItemStack {
        val isScreaming = isScreamingGoat
        val hornMaterial = if (isScreaming) Material.GOAT_HORN else Material.GOAT_HORN
        return ItemStack.of(hornMaterial) // TODO: should set random instrument
    }

    override fun ambientSound(): SoundEvent = if (isScreamingGoat) SoundEvent.ENTITY_GOAT_SCREAMING_AMBIENT else SoundEvent.ENTITY_GOAT_AMBIENT

    override fun hurtSound(): SoundEvent = if (isScreamingGoat) SoundEvent.ENTITY_GOAT_SCREAMING_HURT else SoundEvent.ENTITY_GOAT_HURT

    override fun deathSound(): SoundEvent = if (isScreamingGoat) SoundEvent.ENTITY_GOAT_SCREAMING_DEATH else SoundEvent.ENTITY_GOAT_DEATH

    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_GOAT_STEP, soundVolume(), soundPitch())
    }

    override fun playEatSound() {
        val sound = if (isScreamingGoat) SoundEvent.ENTITY_GOAT_SCREAMING_EAT else SoundEvent.ENTITY_GOAT_EAT
        playSoundEvent(sound, soundVolume(), Random.Default.nextFloat() * (1.2f - 0.8f) + 0.8f)
    }

    fun getMilkingSound(): SoundEvent = if (isScreamingGoat) SoundEvent.ENTITY_GOAT_SCREAMING_MILK else SoundEvent.ENTITY_GOAT_MILK

    override fun aiTick(time: Long) {
        super.aiTick(time)
    }

    fun dropHorn(): Boolean {
        if (!hasLeftHorn && !hasRightHorn) return false

        val right = (!hasLeftHorn || Random.nextBoolean())
        if (right) {
            hasRightHorn = false
        } else {
            hasLeftHorn = false
        }

        val pos = position
        val item = createHorn()
        val itemEntity = ItemEntity(item)
        itemEntity.setInstance(instance, pos)
        return true
    }



    fun addHorns() {
        hasLeftHorn = true
        hasRightHorn = true
    }

    fun removeHorns() {
        hasLeftHorn = false
        hasRightHorn = false
    }

    override fun onInteract(player: Player, hand: PlayerHand): InteractionResult {
        val itemStack = player.getItemInHand(hand)
        return if (itemStack.material() == Material.BUCKET && !isBaby) {
            playSoundEvent(getMilkingSound(), 1.0f, 1.0f)
            player.setItemInHand(hand, ItemStack.of(Material.MILK_BUCKET))
            InteractionResult.SUCCESS
        } else {
            super.onInteract(player, hand)
        }
    }
}