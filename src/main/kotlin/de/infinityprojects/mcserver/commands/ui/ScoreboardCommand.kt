package de.infinityprojects.mcserver.commands.ui

import de.infinityprojects.mcserver.server.MeliusServer
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType.Literal

class ScoreboardCommand: Command("sb", "scoreboard") {
    init {
        val reload = Literal("reload")

        addSyntax({ executor, context ->
            MeliusServer.playerManager.scoreboardManager.load()

            MinecraftServer.getConnectionManager().onlinePlayers.forEach {
                MeliusServer.playerManager.scoreboardManager.sendScoreboard(it)
            }
        }, reload)
    }
}