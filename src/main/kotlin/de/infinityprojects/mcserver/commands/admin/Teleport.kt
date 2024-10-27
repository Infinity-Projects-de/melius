package de.infinityprojects.mcserver.commands.admin

import de.infinityprojects.mcserver.server.MeliusServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import net.minestom.server.command.builder.suggestion.SuggestionEntry
import net.minestom.server.coordinate.Pos
import net.minestom.server.entity.Entity
import net.minestom.server.entity.Player
import net.minestom.server.instance.Instance

class Teleport : Command("teleport", "tp") {
    init {
        setDefaultExecutor { sender, context ->
            sender.sendMessage(Component.text("Correct usage: /teleport <player> <player/coords/world>..."))
        }

        val destinationEntity = ArgumentType.Entity("destination").onlyPlayers(false).singleEntity(true)

        addSyntax({ sender, context ->
            val dest = context.get(destinationEntity).find(sender).firstOrNull()
            if (dest == null) {
                sender.sendMessage(Component.text("Destination entity not found").color(NamedTextColor.RED))
                return@addSyntax
            }
            if (sender is Player) {
                if (sender.instance != dest.instance) sender.setInstance(dest.instance)
                sender.teleport(dest.position)
            } else {
                sender.sendMessage(Component.text("Only players can teleport to a destination").color(NamedTextColor.RED))
            }
        }, destinationEntity)

        val destinationCoords = ArgumentType.RelativeBlockPosition("coords")
        val worldArgument = ArgumentType
            .String("world")
            .setSuggestionCallback { sender, context, suggestion ->
                MeliusServer.worldManager.worlds.keys
                    .forEach { suggestion.addEntry(SuggestionEntry(it)) }
            }.setDefaultValue { sender -> if (sender is Player) sender.instance.dimensionName else null }

        addSyntax({ sender, context ->
            val coords = context.get(destinationCoords).fromSender(sender).asPosition()
            val world = MeliusServer.worldManager.getWorld(context.get(worldArgument))
            if (world == null) {
                sender.sendMessage(Component.text("World not found").color(NamedTextColor.RED))
                return@addSyntax
            }
            if (sender is Player) {
                if (sender.instance != world) sender.setInstance(world)
                sender.teleport(coords)
            } else {
                sender.sendMessage(Component.text("Only players can teleport to a location").color(NamedTextColor.RED))
            }
        }, destinationCoords, worldArgument)

        val targets = ArgumentType.Entity("target").onlyPlayers(false).singleEntity(false)

        addSyntax({ sender, context ->
            val targetEntities = context.get(targets).find(sender)
            val dest = context.get(destinationEntity).find(sender).firstOrNull()

            if (dest == null) {
                sender.sendMessage(Component.text("Destination entity not found").color(NamedTextColor.RED))
                return@addSyntax
            }

            targetEntities.forEach {
                if (it.instance != dest.instance) it.setInstance(dest.instance)
                it.teleport(dest.position)
                if (it is Player) {
                    val name = if (dest is Player) dest.username else dest.entityType.name()
                    it.sendMessage(
                        Component
                            .empty()
                            .append(Component.text("Teleported to ", NamedTextColor.YELLOW))
                            .append(Component.text(name, NamedTextColor.GOLD)),
                    )
                }
            }
        })

        addSyntax({ sender, context ->
            val targetEntities = context.get(targets).find(sender)
            val coords = context.get(destinationCoords).fromSender(sender).asPosition()
            val world = MeliusServer.worldManager.getWorld(context.get(worldArgument))
            if (world == null) {
                sender.sendMessage(Component.text("World not found").color(NamedTextColor.RED))
                return@addSyntax
            }

            targetEntities.forEach {
                if (it.instance != world) it.setInstance(world)
                it.teleport(coords)
                if (it is Player) {
                    it.sendMessage(
                        Component
                            .empty()
                            .append(Component.text("Teleported to ", NamedTextColor.YELLOW))
                            .append(Component.text("${coords.blockX()} ${coords.blockY()} ${coords.blockZ()}", NamedTextColor.GOLD)),
                    )
                }
            }
        }, targets, destinationCoords, worldArgument)
    }
}
