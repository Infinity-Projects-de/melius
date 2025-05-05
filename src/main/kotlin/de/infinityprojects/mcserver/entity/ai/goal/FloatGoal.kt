package de.infinityprojects.mcserver.entity.ai.goal

import net.minestom.server.entity.EntityCreature
import net.minestom.server.entity.ai.GoalSelector
import kotlin.random.Random

class FloatGoal(entity: EntityCreature): GoalSelector(entity) {
    val rand = Random(System.currentTimeMillis())

    override fun shouldStart(): Boolean {
        val e = this.entityCreature
        val pos = e.position
        val world = e.instance
        if (world == null) return false

        val block = world.getBlock(pos)
        return block.isLiquid
    }

    override fun start() {
    }

    override fun tick(time: Long) {
        val e = this.entityCreature

        if (rand.nextFloat() < 0.8F) {
            e.velocity = e.velocity.add(0.0, 0.01, 0.0);
        }
    }

    override fun shouldEnd(): Boolean {
        return false
    }

    override fun end() {
    }
}