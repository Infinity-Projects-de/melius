package de.infinityprojects.mcserver.entity.monster

import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.ElderGuardianMeta
import net.minestom.server.network.packet.server.play.ChangeGameStatePacket
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.sound.SoundEvent

class ElderGuardian : Guardian<ElderGuardianMeta>(EntityType.ELDER_GUARDIAN) {
    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.3
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 8.0
        getAttribute(Attribute.MAX_HEALTH).baseValue = 80.0
    }

    override val attackDuration: Int = 60

    override fun ambientSound(): SoundEvent = if (isInWater) SoundEvent.ENTITY_ELDER_GUARDIAN_AMBIENT else SoundEvent.ENTITY_ELDER_GUARDIAN_AMBIENT_LAND
    override fun hurtSound(): SoundEvent = if (isInWater) SoundEvent.ENTITY_ELDER_GUARDIAN_HURT else SoundEvent.ENTITY_ELDER_GUARDIAN_HURT_LAND
    override fun deathSound(): SoundEvent = if (isInWater) SoundEvent.ENTITY_ELDER_GUARDIAN_DEATH else SoundEvent.ENTITY_ELDER_GUARDIAN_DEATH_LAND
    override fun floppingSound(): SoundEvent = SoundEvent.ENTITY_ELDER_GUARDIAN_FLOP

    override fun aiTick(time: Long) {
        super.aiTick(time)
        if ((aliveTicks + entityId) % 1200 == 0L) {
            val players = instance.getNearbyEntities(position, 50.0)
                .filterIsInstance<Player>()
                .map { player ->
                    player
                }

            val miningFatigue = Potion(PotionEffect.MINING_FATIGUE, 6000, 2)

            players.forEach { player ->
                player.addEffect(miningFatigue)
                player.sendPacket(ChangeGameStatePacket(
                    ChangeGameStatePacket.Reason.PLAYER_ELDER_GUARDIAN_MOB_APPEARANCE,
                    1.0f
                ))
            }
        }
    }
}