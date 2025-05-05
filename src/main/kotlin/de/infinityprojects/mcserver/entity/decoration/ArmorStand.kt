package de.infinityprojects.mcserver.entity.decoration

import de.infinityprojects.mcserver.entity.SoundEmitter
import net.minestom.server.coordinate.Vec
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.LivingEntity
import net.minestom.server.entity.MetadataDef
import net.minestom.server.sound.SoundEvent

class ArmorStand : LivingEntity(EntityType.ARMOR_STAND), SoundEmitter {
    var small: Boolean
        get() = metadata.get(MetadataDef.ArmorStand.IS_SMALL) ?: false
        set(value) { metadata.set(MetadataDef.ArmorStand.IS_SMALL, value) }

    var showArms: Boolean
        get() = metadata.get(MetadataDef.ArmorStand.HAS_ARMS) ?: false
        set(value) { metadata.set(MetadataDef.ArmorStand.HAS_ARMS, value) }

    var noBasePlate: Boolean
        get() = metadata.get(MetadataDef.ArmorStand.HAS_NO_BASE_PLATE) ?: false
        set(value) { metadata.set(MetadataDef.ArmorStand.HAS_NO_BASE_PLATE, value) }

    var marker: Boolean
        get() = metadata.get(MetadataDef.ArmorStand.IS_MARKER) ?: false
        set(value) { metadata.set(MetadataDef.ArmorStand.IS_MARKER, value) }

    var disabledSlots: Int = 0

    var headPose: Vec = Vec(0.0, 0.0, 0.0)
    var bodyPose: Vec = Vec(0.0, 0.0, 0.0)
    var leftArmPose: Vec = Vec(-10.0, 0.0, -10.0)
    var rightArmPose: Vec = Vec(-15.0, 0.0, 10.0)
    var leftLegPose: Vec = Vec(-1.0, 0.0, -1.0)
    var rightLegPose: Vec = Vec(1.0, 0.0, 1.0)

    init {
        updateMetadataDefPose()
    }

    fun setHeadPose(vec: Vec) { headPose = vec; updateMetadataDefPose() }
    fun getHeadPose(): Vec = headPose
    fun setBodyPose(vec: Vec) { bodyPose = vec; updateMetadataDefPose() }
    fun getBodyPose(): Vec = bodyPose
    fun setLeftArmPose(vec: Vec) { leftArmPose = vec; updateMetadataDefPose() }
    fun getLeftArmPose(): Vec = leftArmPose
    fun setRightArmPose(vec: Vec) { rightArmPose = vec; updateMetadataDefPose() }
    fun getRightArmPose(): Vec = rightArmPose
    fun setLeftLegPose(vec: Vec) { leftLegPose = vec; updateMetadataDefPose() }
    fun getLeftLegPose(): Vec = leftLegPose
    fun setRightLegPose(vec: Vec) { rightLegPose = vec; updateMetadataDefPose() }
    fun getRightLegPose(): Vec = rightLegPose

    private fun updateMetadataDefPose() {
        metadata.set(MetadataDef.ArmorStand.HEAD_ROTATION, headPose)
        metadata.set(MetadataDef.ArmorStand.BODY_ROTATION, bodyPose)
        metadata.set(MetadataDef.ArmorStand.LEFT_ARM_ROTATION, leftArmPose)
        metadata.set(MetadataDef.ArmorStand.RIGHT_ARM_ROTATION, rightArmPose)
        metadata.set(MetadataDef.ArmorStand.LEFT_LEG_ROTATION, leftLegPose)
        metadata.set(MetadataDef.ArmorStand.RIGHT_LEG_ROTATION, rightLegPose)
    }

    override fun soundVolume(): Float = 1.0f
    override fun soundPitch(): Float = 1.0f
    override fun deathSound(): SoundEvent? = SoundEvent.ENTITY_ARMOR_STAND_BREAK
    override fun hurtSound(): SoundEvent? = SoundEvent.ENTITY_ARMOR_STAND_HIT
}
