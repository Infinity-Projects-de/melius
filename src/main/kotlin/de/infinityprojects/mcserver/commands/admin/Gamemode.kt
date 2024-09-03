package de.infinityprojects.mcserver.commands.admin

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.ArgumentType.Literal
import net.minestom.server.entity.GameMode
import net.minestom.server.entity.Player
import net.minestom.server.utils.entity.EntityFinder

class Gamemode : Command("gamemode", "gm") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage("Usage: /gamemode <survival|creative|adventure|spectator> [players]")
        }

        val playerArgument =
            ArgumentType.Entity("player").onlyPlayers(true).singleEntity(false).setDefaultValue { sender ->
                if (sender is Player) {
                    EntityFinder().setTargetSelector(EntityFinder.TargetSelector.SELF)
                } else {
                    null
                }
            }

        addSyntax({ sender, context ->
            changeGamemode(sender, context.get(playerArgument), GameMode.SURVIVAL)
        }, Literal("survival"), playerArgument)

        addSyntax({ sender, context ->
            changeGamemode(sender, context.get(playerArgument), GameMode.CREATIVE)
        }, Literal("creative"), playerArgument)

        addSyntax({ sender, context ->
            changeGamemode(sender, context.get(playerArgument), GameMode.ADVENTURE)
        }, Literal("adventure"), playerArgument)

        addSyntax({ sender, context ->
            changeGamemode(sender, context.get(playerArgument), GameMode.SPECTATOR)
        }, Literal("spectator"), playerArgument)
    }

    fun changeGamemode(
        sender: CommandSender,
        finder: EntityFinder,
        mode: GameMode,
    ) {
        val players = finder.find(sender)
        val notPlayers = players.count { it !is Player }
        if (notPlayers > 0) {
            sender.sendMessage(
                Component.empty().append(
                    Component.text("$notPlayers selected entities are not players.").color(NamedTextColor.RED),
                ),
            )
        }
        if (players.isEmpty()) {
            sender.sendMessage(
                Component.empty().append(
                    Component.text("No players found").color(NamedTextColor.RED),
                ),
            )
        }
        players.forEach { player ->
            if (player is Player) {
                player.gameMode = mode
                player.sendMessage(
                    Component
                        .empty()
                        .append(Component.text("Gamemode changed to ").color(NamedTextColor.YELLOW))
                        .append(Component.text(mode.name.lowercase()).color(NamedTextColor.GOLD)),
                )
            }
        }
        if (players.size != 1 || players.first() != sender) {
            sender.sendMessage(
                Component.empty().append(
                    Component
                        .text("Gamemode changed to ")
                        .color(NamedTextColor.YELLOW)
                        .append(Component.text(mode.name.lowercase()).color(NamedTextColor.GOLD))
                        .append(Component.text(" for ${players.size} players").color(NamedTextColor.YELLOW)),
                ),
            )
        }
    }
}
