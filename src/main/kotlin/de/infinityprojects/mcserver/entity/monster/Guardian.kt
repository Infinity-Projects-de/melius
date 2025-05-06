package de.infinityprojects.mcserver.entity.monster

import de.infinityprojects.mcserver.entity.PathfinderMob
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.MoveTowardsRestrictionGoal
import de.infinityprojects.mcserver.entity.animal.axolotl.Axolotl
import de.infinityprojects.mcserver.entity.animal.water.Squid
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.Player
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.ai.goal.RangedAttackGoal
import net.minestom.server.entity.ai.target.ClosestEntityTarget
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.damage.DamageType
import net.minestom.server.entity.metadata.monster.GuardianMeta
import net.minestom.server.entity.pathfinding.followers.WaterNodeFollower
import net.minestom.server.entity.pathfinding.generators.WaterNodeGenerator
import net.minestom.server.sound.SoundEvent
import net.minestom.server.utils.time.TimeUnit
import net.minestom.server.world.Difficulty
import kotlin.random.Random

open class Guardian<M : GuardianMeta> protected constructor(type: EntityType) : Monster<M>(type) {
    companion object {
        operator fun invoke() = Guardian<GuardianMeta>(EntityType.GUARDIAN)
    }

    open val attackDuration: Int = 40

    var activeTarget: PathfinderMob<*>?
        get() = typedMeta.target as? PathfinderMob<*>
        set(value) {
            typedMeta.target = value
        }

    var randomStrollGoal: RandomStrollGoal? = null

    init {
        navigator.setNodeFollower { WaterNodeFollower(this) }
        navigator.setNodeGenerator { WaterNodeGenerator() }
    }

    override fun createGoals(): List<GoalSelector> {
        val moveTowardsRestriction = MoveTowardsRestrictionGoal(this, 1.0, 10)
        randomStrollGoal = RandomStrollGoal(this, 80)
        return listOf(
            GuardianAttackGoal(),
            moveTowardsRestriction,
            randomStrollGoal!!,
            LookAtPlayerGoal(this, 8.0),
            RandomLookAroundGoal(this, 2)
        )
    }

    override fun createTargets(): List<TargetSelector> {
        return listOf(
            ClosestEntityTarget(this, 10.0) { entityLiving ->
                (entityLiving is Player || entityLiving is Squid || entityLiving is Axolotl) && entityLiving.getDistanceSquared(this) > 9.0
            }
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.ATTACK_DAMAGE).baseValue = 6.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.5
        getAttribute(Attribute.MAX_HEALTH).baseValue = 30.0
    }

    override fun ambientSound(): SoundEvent = if (isInWater) SoundEvent.ENTITY_GUARDIAN_AMBIENT else SoundEvent.ENTITY_GUARDIAN_AMBIENT_LAND
    override fun hurtSound(): SoundEvent = if (isInWater) SoundEvent.ENTITY_GUARDIAN_HURT else SoundEvent.ENTITY_GUARDIAN_HURT_LAND
    override fun deathSound(): SoundEvent = if (isInWater) SoundEvent.ENTITY_GUARDIAN_DEATH else SoundEvent.ENTITY_GUARDIAN_DEATH_LAND

    override fun aiTick(time: Long) {
        if (isAlive) {
            if (isInWater) {
                airSupply = 300
            } else if (onGround) {
                velocity = velocity.add(
                    (Random.Default.nextFloat() * 2.0f - 1.0f) * 0.4,
                    0.5,
                    (Random.Default.nextFloat() * 2.0f - 1.0f) * 0.4
                )
                position = position.withYaw { Random.Default.nextDouble() * 360.0 }
                onGround = false
            }

            val target = target
            if (target != null) {
                lookAt(target)
            }
        }

        super.aiTick(time)
    }

    /*inner class GuardianMoveController(private val guardian: Guardian<M>) : WaterNodeFollower(guardian) {
        override fun tick() {
            if (operation == Operation.MOVE_TO && !guardian.navigation.isDone) {
                val vec3d = Pos(wantedX - guardian.position.x, wantedY - guardian.position.y, wantedZ - guardian.position.z)
                val d0 = vec3d.length()
                val d1 = vec3d.x / d0
                val d2 = vec3d.y / d0
                val d3 = vec3d.z / d0
                val f = atan2(vec3d.z, vec3d.x).toFloat() * (180F / Math.PI.toFloat()) - 90.0f

                guardian.yaw = rotlerp(guardian.yaw, f, 90.0f)
                guardian.yBodyRot = guardian.yaw
                val f1 = (speedModifier * guardian.getAttributeValue(Attribute.MOVEMENT_SPEED)).toFloat()
                val f2 = MathHelper.lerp(0.125f, guardian.speed, f1)

                guardian.speed = f2
                val d4 = sin((guardian.tickCount + guardian.id) * 0.5) * 0.05
                val d5 = cos(guardian.yaw * (Math.PI / 180F))
                val d6 = sin(guardian.yaw * (Math.PI / 180F))
                val d7 = sin((guardian.tickCount + guardian.id) * 0.75) * 0.05

                guardian.deltaMovement = guardian.deltaMovement.add(
                    d4 * d5,
                    d7 * (d6 + d5) * 0.25 + f2 * d2 * 0.1,
                    d4 * d6
                )
                val lookControl = guardian.lookControl
                val d8 = guardian.position.x + d1 * 2.0
                val d9 = guardian.eyeY + d2 / d0
                val d10 = guardian.position.z + d3 * 2.0
                val d11 = lookControl.wantedX
                val d12 = lookControl.wantedY
                val d13 = lookControl.wantedZ

                if (!lookControl.isLookingAtTarget) {
                    lookControl.setLookAt(
                        MathHelper.lerp(0.125, d11, d8),
                        MathHelper.lerp(0.125, d12, d9),
                        MathHelper.lerp(0.125, d13, d10),
                        10.0f, 40.0f
                    )
                }

                guardian.setMoving(true)
            } else {
                guardian.speed = 0.0f
                guardian.setMoving(false)
            }
        }
    }*/

    inner class GuardianAttackGoal : RangedAttackGoal(this, 20, 15, 15, true, 0.0, 0.0, TimeUnit.SERVER_TICK) {
        private var attackTime = 0
        private val elder = this@Guardian is ElderGuardian

        init {
            this@GuardianAttackGoal.setProjectileGenerator { shooter -> null }
        }

        override fun shouldStart(): Boolean {
            val target = this@Guardian.target as? PathfinderMob<*> ?: return false
            return target.isAlive
        }

        override fun start() {
            attackTime = -10
            navigator.reset()
            activeTarget?.let {
                lookAt(it)
            }
        }

        override fun shouldEnd(): Boolean {
            return elder || activeTarget?.let { this@Guardian.getDistanceSquared(it) > 9.0 } ?: false
        }

        override fun end() {
            target = null
            randomStrollGoal?.start()
        }

        override fun tick(time: Long) {
            val target = this@Guardian.target as? PathfinderMob<*> ?: return

            navigator.reset()
            lookAt(target)
            if (!hasLineOfSight(target)) {
                this@Guardian.target = null
            } else {
                attackTime++
                if (attackTime == 0) {
                    activeTarget = target
                    if (!isSilent) {
                        playSoundEvent(SoundEvent.ENTITY_GUARDIAN_ATTACK, 1.0f, 1.0f)
                    }
                } else if (attackTime >= attackDuration) {
                    var damage = 1.0f

                    if (MinecraftServer.getDifficulty() == Difficulty.HARD) {
                        damage += 2.0f
                    }

                    if (elder) {
                        damage += 2.0f
                    }

                    target.damage(Damage(DamageType.MOB_ATTACK, this@Guardian, this@Guardian, position, damage))
                }
            }

            super.tick(time)
        }
    }
}