package de.infinityprojects.mcserver.entity.animal.fish

import net.minestom.server.entity.EntityType
import net.minestom.server.sound.SoundEvent

class Cod: EntityFishSchool(EntityType.COD) {
    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_COD_DEATH

    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_COD_HURT

    override fun ambientSound(): SoundEvent? = SoundEvent.ENTITY_COD_AMBIENT

    override fun getFlopSound(): SoundEvent = SoundEvent.ENTITY_COD_FLOP
}