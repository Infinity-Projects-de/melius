package de.infinityprojects.mcserver.entity.vehicle

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.EntityType
import net.minestom.server.entity.metadata.other.BoatMeta
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.network.packet.client.play.ClientSteerVehiclePacket

class VehicleHandler {
    private val logger = org.slf4j.LoggerFactory.getLogger("VehicleHandler")

    init {
        val vehicleNode = EventNode.type("vehicle_node", EventFilter.PLAYER)
        MinecraftServer.getGlobalEventHandler().addChild(vehicleNode)

        vehicleNode.addListener(PlayerBlockInteractEvent::class.java, ::blockInteractEvent)
        vehicleNode.addListener(PlayerEntityInteractEvent::class.java, ::entityInteractEvent)
        vehicleNode.addListener(PlayerPacketEvent::class.java, ::playerEvent)
    }

    fun playerEvent(event: PlayerPacketEvent) {
        val player = event.player
        val packet = event.packet
        if (packet is ClientSteerVehiclePacket) {
            val vehicle = player.vehicle
            if (vehicle != null) {
                val unmountFlag = packet.flags.toInt() and 0x02 == 0x02
                if (unmountFlag) {
                    if (vehicle != null) {
                        vehicle.removePassenger(player)
                        player.teleport(vehicle.position.add(0.0, 1.0, 0.0))
                    }
                }

                if (packet.forward > 0f && vehicle.entityType == EntityType.MINECART) {
                    val playerDirection = player.position.direction()


                    vehicle.velocity = vehicle.position.direction().mul(playerDirection.mul(packet.forward.toDouble()))
                }
            }


        }

    }

    fun blockInteractEvent(event: PlayerBlockInteractEvent) {
        val player = event.player

        val hand = event.hand
        val item = player.getItemInHand(hand)
        val instance = event.instance

        val materialName = item.material().key().value()
        if (materialName.endsWith("_boat")) {
            val playerPos = player.position
            val spawnPos = event.blockPosition.add(event.cursorPosition)
            val pos = Pos(spawnPos, playerPos.yaw, playerPos.pitch)

            val boat = if (materialName.endsWith("_chest_boat")) Entity(EntityType.CHEST_BOAT) else Entity(EntityType.BOAT)

            val meta = boat.entityMeta as BoatMeta
            val typeName = materialName.split("_")[0].uppercase()
            meta.type = BoatMeta.Type.valueOf(typeName)

            boat.setInstance(instance, pos)
        } else if (materialName.startsWith("minecart")) {
            val block = event.block
            if (block.key().value().endsWith("rail")) {
                val pos = event.blockPosition.add(0.5, 0.5, 0.5)

                val minecart = Entity(EntityType.MINECART)
                minecart.setInstance(instance, pos)
            }
        }
    }

    fun entityInteractEvent(event: PlayerEntityInteractEvent) {
        val player = event.player
        val entity = event.target

        if (entity.entityType == EntityType.BOAT) {
            if (entity.passengers.size < 2) {
                entity.addPassenger(player)
            }
        } else if (entity.entityType == EntityType.CHEST_BOAT) {
            if (entity.passengers.isEmpty()) {
                entity.addPassenger(player)
            }
        } else if (entity.entityType == EntityType.MINECART) {
            if (entity.passengers.isEmpty()) {
                entity.addPassenger(player)
            }
        }
    }

}