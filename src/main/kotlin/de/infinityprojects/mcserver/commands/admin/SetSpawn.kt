package de.infinityprojects.mcserver.commands.admin

import de.infinityprojects.mcserver.server.MeliusServer
import de.infinityprojects.mcserver.utils.TWO_DECIMAL_FORMAT
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import java.text.DecimalFormat

class SetSpawn : Command("setspawn") {
    init {
        setDefaultExecutor { sender, _ ->
            sender.sendMessage("§cUsage: /setspawn <coords> [world] [player(s)]")
        }

        val coords = ArgumentType.RelativeBlockPosition("coords")
        val player = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(false)
        val world =
            ArgumentType.String("world").setDefaultValue { sender ->
                if (sender is Player) {
                    MeliusServer.worldManager.getWorldName(sender.instance)
                } else {
                    null
                }
            }

        addSyntax({ sender, ctx ->
            val coord = ctx.get(coords).fromSender(sender).asPosition()
            val worldName = ctx.get(world)
            val instance = MeliusServer.worldManager.getWorld(worldName) ?: return@addSyntax

            MeliusServer.worldManager.setSpawn(instance, coord)

            val dF = DecimalFormat(TWO_DECIMAL_FORMAT)
            sender.sendMessage(
                Component
                    .empty()
                    .append(Component.text("Set spawn for world ").color(NamedTextColor.YELLOW))
                    .append(Component.text(worldName).color(NamedTextColor.GOLD))
                    .append(Component.text(" to ").color(NamedTextColor.YELLOW))
                    .append(Component.text(dF.format(coord.x)).color(NamedTextColor.GOLD))
                    .append(Component.text(", ").color(NamedTextColor.YELLOW))
                    .append(Component.text(dF.format(coord.y)).color(NamedTextColor.GOLD))
                    .append(Component.text(", ").color(NamedTextColor.YELLOW))
                    .append(Component.text(dF.format(coord.z)).color(NamedTextColor.GOLD)),
            )
        }, coords, world)

        addSyntax({ sender, ctx ->
            val players = ctx.get(player).find(sender)
            val coord = ctx.get(coords).fromSender(sender).asPosition()
            val worldName = ctx.get(world)

            players.forEach {
                if (it !is Player) {
                    return@forEach
                }
                MeliusServer.playerManager.setSpawn(it, worldName, coord)
            }

            val name = if (players.size > 1) "${players.size} players" else (players.first() as Player).username
            val dF = DecimalFormat(TWO_DECIMAL_FORMAT)
            sender.sendMessage(
                Component
                    .empty()
                    .append(Component.text("Set spawn for ").color(NamedTextColor.YELLOW))
                    .append(Component.text(name).color(NamedTextColor.GOLD))
                    .append(Component.text(" in world ").color(NamedTextColor.YELLOW))
                    .append(Component.text(worldName).color(NamedTextColor.GOLD))
                    .append(Component.text(" to ").color(NamedTextColor.YELLOW))
                    .append(Component.text(dF.format(coord.x)).color(NamedTextColor.GOLD))
                    .append(Component.text(", ").color(NamedTextColor.YELLOW))
                    .append(Component.text(dF.format(coord.y)).color(NamedTextColor.GOLD))
                    .append(Component.text(", ").color(NamedTextColor.YELLOW))
                    .append(Component.text(dF.format(coord.z)).color(NamedTextColor.GOLD)),
            )
        }, coords, world, player)
    }
}
