package de.infinityprojects.mcserver.entity.monster

import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.metadata.monster.CaveSpiderMeta
import net.minestom.server.potion.Potion
import net.minestom.server.potion.PotionEffect
import net.minestom.server.world.Difficulty

class CaveSpider : Spider<CaveSpiderMeta>(EntityType.CAVE_SPIDER) {
    init {
        setAttributes()
    }

    override fun setAttributes() {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 12.0
    }

    override fun attack(target: Entity) {
        super.attack(target)

        if (target is LivingEntity) {
            val difficulty = MinecraftServer.getDifficulty()
            val duration = when (difficulty) {
                Difficulty.NORMAL -> 7 * 20
                Difficulty.HARD -> 15 * 20
                else -> 0
            }
            if (duration > 0) {
                target.addEffect(Potion(PotionEffect.POISON, duration, 0))
            }
        }
    }
}