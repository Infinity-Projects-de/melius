package de.infinityprojects.mcserver.entity.vehicle

import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.EntityType
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.entity.EntityAttackEvent
import net.minestom.server.event.player.PlayerBlockInteractEvent
import net.minestom.server.event.player.PlayerEntityInteractEvent
import net.minestom.server.event.player.PlayerPacketEvent
import net.minestom.server.network.packet.client.play.ClientInputPacket

class VehicleHandler {
    private val logger = org.slf4j.LoggerFactory.getLogger("VehicleHandler")

    init {
        val vehicleHandler = EventNode.all("vehicle_handler")
        val playerActions = EventNode.type("player_actions", EventFilter.PLAYER)
        val entityListener = EventNode.type("entity_listener", EventFilter.ENTITY)

        MinecraftServer.getGlobalEventHandler().addChild(vehicleHandler)
        vehicleHandler.addChild(playerActions)
        vehicleHandler.addChild(entityListener)

        playerActions.addListener(PlayerBlockInteractEvent::class.java, ::blockInteractEvent)
        playerActions.addListener(PlayerEntityInteractEvent::class.java, ::entityInteractEvent)

        playerActions.addListener(PlayerPacketEvent::class.java, ::playerEvent)

        entityListener.addListener(EntityAttackEvent::class.java) { event ->
            val entity = event.target
            if (entity is Vehicle) {
               entity.damage(0.25f)
            }
        }
    }

    fun playerEvent(event: PlayerPacketEvent) {
        val player = event.player
        val packet = event.packet
        if (packet is ClientInputPacket) {
            val vehicle = player.vehicle
            if (vehicle != null) {
                if (packet.shift()) {
                    vehicle.removePassenger(player)
                    player.teleport(vehicle.position.add(0.0, 1.0, 0.0))
                }

                if (packet.forward() || packet.backward()) {
                    if (vehicle.entityType == EntityType.MINECART) {
                        val playerDirection = player.position.direction()
                        if (packet.backward()) {
                            playerDirection.mul(-1.0)
                        }
                        vehicle.velocity = vehicle.position.direction().mul(playerDirection)
                    }
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
            val boat = Boat(item.material())
            boat.setInstance(instance, pos)
        } else if (materialName.endsWith("minecart")) {
            val block = event.block
            if (block.key().value().endsWith("rail")) {
                val pos = event.blockPosition.add(0.5, 0.5, 0.5)

                val minecart = if (materialName.startsWith("tnt")) {
                    Vehicle(EntityType.TNT_MINECART)
                } else if (materialName.startsWith("hopper")) {
                    Vehicle(EntityType.HOPPER_MINECART)
                } else if (materialName.startsWith("command")) {
                    Vehicle(EntityType.COMMAND_BLOCK_MINECART)
                } else if (materialName.startsWith("furnace")) {
                    Vehicle(EntityType.FURNACE_MINECART)
                } else if (materialName.startsWith("chest")) {
                    Vehicle(EntityType.CHEST_MINECART)
                } else if (materialName.startsWith("tnt")) {
                    Vehicle(EntityType.TNT_MINECART)
                } else {
                    Vehicle(EntityType.MINECART)
                }

                minecart.setInstance(instance, pos)
            }
        }
    }

    fun entityInteractEvent(event: PlayerEntityInteractEvent) {
        val player = event.player
        val entity = event.target

        if (entity is Vehicle) {
            entity.addPassenger(player)
        }
    }

}