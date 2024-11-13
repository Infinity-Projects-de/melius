package de.infinityprojects.mcserver.commands.ui

import de.infinityprojects.mcserver.server.MeliusServer
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType.Literal

class TabCommand: Command("tab", "playerlist") {
    init {
        val reload = Literal("reload")

        addSyntax({ executor, context ->
            MeliusServer.playerManager.tablistManager.load()

            MinecraftServer.getConnectionManager().onlinePlayers.forEach {
                MeliusServer.playerManager.tablistManager.sendTabList(it)
            }
        }, reload)
    }
}