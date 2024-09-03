package de.infinityprojects.mcserver.commands.admin

import de.infinityprojects.mcserver.server.MeliusServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player

class Teleport : Command("teleport", "tp") {
    init {
        setDefaultExecutor { sender, context ->
            sender.sendMessage(Component.text("Correct usage: /teleport <player> <player/coords/world>..."))
        }

        val playerArg = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(false)

        val targetArg = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true)
        val coordsArg = ArgumentType.RelativeBlockPosition("coords")
        val worldArg = ArgumentType.String("world")

        addSyntax({ sender, context ->
            val player = context.get(playerArg).find(sender)
            val target = context.get(targetArg).find(sender).firstOrNull() ?: return@addSyntax

            player.forEach { p ->
                p.teleport(target.position)
            }
        }, playerArg, targetArg)

        addSyntax({ sender, context ->
            val player = context.get(playerArg).find(sender)
            val coords = context.get(coordsArg).fromSender(sender).asPosition()

            player.forEach { p ->
                p.teleport(coords)
            }
        }, playerArg, coordsArg)

        addSyntax({ sender, context ->
            val player = context.get(playerArg).find(sender)
            val worldName = context.get(worldArg)
            val world = MeliusServer.worldManager.getWorld(worldName)
            if (world == null) {
                sender.sendMessage(Component.text("World $worldName not found").color(NamedTextColor.RED))
                return@addSyntax
            }
            val coords = context.get(coordsArg).fromSender(sender).asPosition()

            player.forEach { p ->
                if (p.instance != world) {
                    p.setInstance(world)
                }
                p.teleport(coords)
            }
        }, playerArg, coordsArg, worldArg)

        addSyntax({ sender, context ->
            val player = context.get(targetArg).find(sender).firstOrNull() ?: return@addSyntax
            if (sender is Player) {
                sender.teleport(player.position)
            }
        }, targetArg)
    }
}
