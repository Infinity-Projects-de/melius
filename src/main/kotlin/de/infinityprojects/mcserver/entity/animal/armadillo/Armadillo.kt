package de.infinityprojects.mcserver.entity.animal.armadillo

import de.infinityprojects.mcserver.entity.animal.Animal
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.ItemEntity
import net.minestom.server.entity.MetadataDef
import net.minestom.server.entity.ai.GoalSelector
import net.minestom.server.entity.ai.TargetSelector
import net.minestom.server.entity.attribute.Attribute
import net.minestom.server.entity.damage.Damage
import net.minestom.server.entity.metadata.animal.ArmadilloMeta
import net.minestom.server.item.ItemStack
import net.minestom.server.item.Material
import net.minestom.server.sound.SoundEvent

class Armadillo : Animal(EntityType.ARMADILLO) {
    private var inStateTicks: Long = 0
    private var scuteTime: Int = pickNextScuteDropTime()
    private var peekReceivedClient: Boolean = false

    init {
        getAttribute(Attribute.MAX_HEALTH).baseValue = 12.0
        getAttribute(Attribute.MOVEMENT_SPEED).baseValue = 0.14
    }

    override fun createGoals(): List<GoalSelector> {
        return emptyList()
    }

    override fun createTargets(): List<TargetSelector> {
        return emptyList()
    }

    override fun aiTick(time: Long) {
        super.aiTick(time)
        if (isAlive && !isBaby && --scuteTime <= 0) {
           /* if (dropFromGiftLootTable(LootTables.ARMADILLO_SHED, this::spawnAtLocation)) {
                playSoundEvent(SoundEvent.ENTITY_ARMADILLO_SCUTE_DROP, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f)
            }*/
            scuteTime = pickNextScuteDropTime()
        }
    }

    override fun tick(time: Long) {
        super.tick(time)
       // setupAnimationStates()

        if (isScared()) {
          //  clampHeadRotationToBody()
        }
        ++inStateTicks
    }

    private fun pickNextScuteDropTime(): Int {
        return random.nextInt(20 * 60 * 5) + 20 * 60 * 5
    }

    override fun damage(damage: Damage): Boolean {
        if (isScared()) {
            damage.amount = (damage.amount - 1.0f) / 2.0f
        }

        return super.damage(damage)
    }

    fun rollUp() {
        if (!isScared()) {
            this.navigator.reset()
            inLove = 0
            playSoundEvent(SoundEvent.ENTITY_ARMADILLO_ROLL, soundVolume(), soundPitch())
            switchToState(ArmadilloMeta.State.ROLLING)
        }
    }

    fun rollOut() {
        if (isScared()) {
            playSoundEvent(SoundEvent.ENTITY_ARMADILLO_UNROLL_FINISH, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f)
            switchToState( ArmadilloMeta.State.IDLE)
        }
    }

    fun isScared(): Boolean {
        return getState() == ArmadilloMeta.State.SCARED
    }

    fun getState():  ArmadilloMeta.State {
        return metadata[MetadataDef.Armadillo.STATE]
    }

    fun switchToState(state: ArmadilloMeta.State) {
        metadata[MetadataDef.Armadillo.STATE] = state
    }

    override fun playStepSound() {
        playSoundEvent(SoundEvent.ENTITY_ARMADILLO_STEP,.15f, 1.0f)
    }

    override fun hurtSound(): SoundEvent? = if (isScared()) SoundEvent.ENTITY_ARMADILLO_HURT_REDUCED else SoundEvent.ENTITY_ARMADILLO_HURT

    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_ARMADILLO_DEATH

    override fun ambientSound(): SoundEvent? = if (isScared()) null else SoundEvent.ENTITY_ARMADILLO_AMBIENT

    fun brushOffScute(): Boolean {
        if (isBaby) {
            return false
        }
        val itemStack = ItemStack.of(Material.ARMADILLO_SCUTE, 1)
        val item = ItemEntity(itemStack)
        item.setInstance(instance, position.add(Vec(0.0, 0.5, 0.0)))
        playSoundEvent(SoundEvent.ENTITY_ARMADILLO_BRUSH, 1.0f, (random.nextFloat() - random.nextFloat()) * 0.2f + 1.0f)
        return true
    }
}