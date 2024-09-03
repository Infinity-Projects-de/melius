package de.infinityprojects.mcserver.server

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.adventure.audience.Audiences
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import org.slf4j.LoggerFactory

class PlayerManager {
    private val logger = LoggerFactory.getLogger("PlayerManager")
    private val players = hashMapOf<String, Player>() // replaceable with Audiences

    init {
        val node = EventNode.type("player_manager", EventFilter.PLAYER)
        MinecraftServer.getGlobalEventHandler().addChild(node)

        node.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
            val player = event.player
            event.spawningInstance = MeliusServer.worldManager.getDefaultWorld()
            player.respawnPoint = Pos(0.0, 41.0, 0.0)

            players[player.username] = player
            broadcast(
                Component
                    .empty()
                    .append(Component.text("[+] ").color(NamedTextColor.GREEN))
                    .append(Component.text(player.username).color(NamedTextColor.GOLD))
                    .append(Component.text(" joined the server").color(NamedTextColor.YELLOW)),
            )
        }

        node.addListener(PlayerDisconnectEvent::class.java) { event ->
            players.remove(event.player.username)

            broadcast(
                Component
                    .empty()
                    .append(Component.text("[-] ").color(NamedTextColor.RED))
                    .append(Component.text(event.player.username).color(NamedTextColor.GOLD))
                    .append(Component.text(" left the server").color(NamedTextColor.YELLOW)),
            )
        }
    }

    fun broadcast(message: Component) {
        Audiences.all().sendMessage(message)
    }
}
