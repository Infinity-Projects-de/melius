package de.infinityprojects.mcserver.entity.animal

import de.infinityprojects.mcserver.entity.ai.goal.FloatGoal
import de.infinityprojects.mcserver.entity.ai.goal.LookAtPlayerGoal
import de.infinityprojects.mcserver.entity.ai.goal.PanicGoal
import de.infinityprojects.mcserver.entity.ai.goal.TemptGoal
import de.infinityprojects.mcserver.entity.ai.goal.animal.BreedGoal
import de.infinityprojects.mcserver.entity.ai.goal.animal.FollowParentGoal
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.goal.RandomLookAroundGoal
import net.minestom.server.entity.ai.goal.RandomStrollGoal
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

class Chicken: Animal(EntityType.CHICKEN) {
    var eggTime = calculateEggTime()

    override fun createGoals(): List<GoalSelector> {
        return listOf(
            FloatGoal(this),
            PanicGoal(this, 1.4),
            BreedGoal(this, 1.0),
            TemptGoal(this, 1.2) { item -> item.material() == Material.WHEAT_SEEDS },
            FollowParentGoal(this, 1.1),
            RandomStrollGoal(this, 5),
            LookAtPlayerGoal(this, 6.0),
            RandomLookAroundGoal(this, 2),
        )
    }

    override fun setAttributes() {
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.1
        getAttribute(Attribute.MAX_HEALTH).baseValue = 4.0
        super.setAttributes()
    }

    fun calculateEggTime(): Int {
        return random.nextInt(6000) + 6000
    }

    fun tryToLayEgg() {
        --eggTime
        if (eggTime < 0) {
            val pitch = (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f
            playSoundEvent(SoundEvent.ENTITY_CHICKEN_EGG, 1.0f, pitch)
            val egg = ItemStack.of(Material.EGG, 1)
            val item = ItemEntity(egg)
            item.setInstance(instance, position)

            eggTime = calculateEggTime()
        }
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)

        if (!isOnGround && velocity.y < 0.0) {
            velocity = velocity.mul(1.0, 0.6, 1.0)
        }

        tryToLayEgg()
    }

    override fun playAmbientSound() {
        playSoundEvent(SoundEvent.ENTITY_CHICKEN_AMBIENT, soundVolume(), soundPitch())
    }

    override fun hurtSound(): SoundEvent = SoundEvent.ENTITY_CHICKEN_HURT
    override fun deathSound(): SoundEvent = SoundEvent.ENTITY_CHICKEN_DEATH
    override fun stepSound(): SoundEvent? = SoundEvent.ENTITY_CHICKEN_STEP
}