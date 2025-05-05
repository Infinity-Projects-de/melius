package de.infinityprojects.mcserver.entity.animal.horse

import de.infinityprojects.mcserver.entity.animal.Animal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.animal.AnimalMeta
import net.minestom.server.entity.metadata.animal.DonkeyMeta
import net.minestom.server.sound.SoundEvent

class Donkey(type: EntityType = EntityType.DONKEY) : AbstractChestedHorse<DonkeyMeta>(type) {
    override fun ambientSound(): SoundEvent = SoundEvent.ENTITY_DONKEY_AMBIENT

    override fun angrySound(): SoundEvent = SoundEvent.ENTITY_DONKEY_ANGRY

    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_DONKEY_DEATH

    override fun playEatSound() {
        playSoundEvent(SoundEvent.ENTITY_DONKEY_EAT, 0.4f, 1.0f)
    }

    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_DONKEY_HURT

    override fun canMateWith(other: Animal<out AnimalMeta>): Boolean {
        return when (other) {
            is Donkey, is Horse -> true
            else -> false
        }
    }

    override fun playJumpSound() {
        playSoundEvent(SoundEvent.ENTITY_DONKEY_JUMP, 0.4f, 1.0f)
    }
}