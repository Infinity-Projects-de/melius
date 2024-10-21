package de.infinityprojects.mcserver.server

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.event.player.PlayerChatEvent

class ChatManager {
    init {
        val node = EventNode.type("chat_manager", EventFilter.PLAYER)
        MinecraftServer.getGlobalEventHandler().addChild(node)

        node.addListener(PlayerChatEvent::class.java) { event ->
            val player = event.player
            val message = event.message
            event.setChatFormat { e ->
                var comp = Component.text()
                comp = comp.append(Component.text("${player.username}: ").color(NamedTextColor.GRAY))
                val parsed = LegacyComponentSerializer.legacyAmpersand().deserialize(message)
                comp = comp.append(parsed)
                return@setChatFormat comp.build()
            }
        }

    }
}