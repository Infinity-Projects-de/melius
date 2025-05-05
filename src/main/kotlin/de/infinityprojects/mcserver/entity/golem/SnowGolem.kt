package de.infinityprojects.mcserver.entity.golem

import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.projectile.Snowball
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.goal.RangedAttackGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.metadata.golem.SnowGolemMeta
import net.minestom.server.utils.time.TimeUnit

class SnowGolem: AbstractGolem<SnowGolemMeta>(EntityType.SNOW_GOLEM) {

    override fun createGoals(): List<GoalSelector> {
        val rangedAttackGoal = RangedAttackGoal(this, 20, 20, 20, true, 1.0, 0.0, TimeUnit.SERVER_TICK)
        rangedAttackGoal.setProjectileGenerator { shooter -> Snowball(shooter) }

        return listOf(
            rangedAttackGoal,
            RandomStrollGoal(this, 5),
            LookAtPlayerGoal(this, 6.0),
            RandomLookAroundGoal(this, 2),
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            ClosestEntityTarget(this, 10.0) { entity ->
                entity.entityType == EntityType.PLAYER
            }
        )
    }
}