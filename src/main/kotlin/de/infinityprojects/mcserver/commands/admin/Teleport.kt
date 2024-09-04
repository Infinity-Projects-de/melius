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

        val targets = ArgumentType.Entity("player").onlyPlayers(false).singleEntity(false)
        val destination = ArgumentType.Entity("target").onlyPlayers(false).singleEntity(true)

        val coordsArg = ArgumentType.RelativeBlockPosition("coords")

        val worldArg =
            ArgumentType
                .String("world")
                .setSuggestionCallback { sender, context, suggestion ->
                    MeliusServer.worldManager.worlds.keys
                        .forEach { suggestion.addEntry(SuggestionEntry(it)) }
                }.setDefaultValue { sender -> if (sender is Player) sender.instance.dimensionName else null }

        addSyntax({ sender, context ->
            val dest = getDestination(sender, context, destination) ?: return@addSyntax
            if (sender is Player) {
                teleport(sender, listOf(sender), dest)
            } else {
                sender.sendMessage(Component.text("Only players can teleport to a destination").color(NamedTextColor.RED))
            }
        }, destination)

        addSyntax({ sender, context ->
            val targetEntities = getTargets(sender, context, targets)
            val dest = getDestination(sender, context, destination) ?: return@addSyntax
            teleport(sender, targetEntities, dest)
        }, targets, destination)

        addSyntax({ sender, context ->
            val coords = context.get(coordsArg).fromSender(sender).asPosition()
            val world = getWorld(sender, context, worldArg) ?: return@addSyntax
            if (sender is Player) {
                teleport(sender, listOf(sender), coords, world)
            } else {
                sender.sendMessage(Component.text("Only players can teleport to a location").color(NamedTextColor.RED))
            }
        }, coordsArg, worldArg)

        addSyntax({ sender, context ->
            val targetEntities = getTargets(sender, context, targets)
            val coords = context.get(coordsArg).fromSender(sender).asPosition()
            val world = getWorld(sender, context, worldArg) ?: return@addSyntax
            teleport(sender, targetEntities, coords, world)
        }, targets, coordsArg, worldArg)
    }

    private fun teleport(
        sender: CommandSender,
        targets: List<Entity>,
        destination: Entity,
    ) {
        val name = if (destination is Player) destination.username else destination.entityType.name()
        targets.forEach {
            it.teleport(destination.position)
            if (it is Player) {
                it.sendMessage(
                    Component
                        .empty()
                        .append(Component.text("Teleported to ", NamedTextColor.YELLOW))
                        .append(Component.text(name, NamedTextColor.GOLD)),
                )
            }
        }

        val targetName =
            if (targets.size == 1) {
                val target = targets.first()
                if (target is Player) {
                    target.username
                } else {
                    target.entityType.name()
                }
            } else {
                "${targets.size} entities"
            }

        val message =
            Component
                .empty()
                .append(Component.text("Teleported ", NamedTextColor.YELLOW))
                .append(Component.text(targetName, NamedTextColor.GOLD))
                .append(Component.text(" to ", NamedTextColor.YELLOW))
                .append(Component.text(name, NamedTextColor.GOLD))

        MinecraftServer.getCommandManager().consoleSender.sendMessage(message)
        if ((targets.size != 1 || targets.first() != sender)) {
            if (sender != MinecraftServer.getCommandManager().consoleSender) {
                sender.sendMessage(message)
            }
        }
    }

    private fun teleport(
        sender: CommandSender,
        targets: List<Entity>,
        position: Pos,
        world: Instance?,
    ) {
        targets.forEach {
            if (world != null && it.instance != world) {
                it.setInstance(world)
            }
            it.teleport(position)
            if (it is Player) {
                it.sendMessage(
                    Component
                        .empty()
                        .append(Component.text("Teleported to ", NamedTextColor.YELLOW))
                        .append(Component.text("${position.blockX()} ${position.blockY()} ${position.blockZ()}", NamedTextColor.GOLD)),
                )
            }
        }

        val targetName =
            if (targets.size == 1) {
                val target = targets.first()
                if (target is Player) {
                    target.username
                } else {
                    target.entityType.name()
                }
            } else {
                "${targets.size} entities"
            }

        val message =
            Component
                .empty()
                .append(Component.text("Teleported ", NamedTextColor.YELLOW))
                .append(Component.text(targetName, NamedTextColor.GOLD))
                .append(Component.text(" to ", NamedTextColor.YELLOW))
                .append(Component.text("${position.blockX()} ${position.blockY()} ${position.blockZ()}", NamedTextColor.GOLD))

        MinecraftServer.getCommandManager().consoleSender.sendMessage(message)
        if ((targets.size != 1 || targets.first() != sender)) {
            if (sender != MinecraftServer.getCommandManager().consoleSender) {
                sender.sendMessage(message)
            }
        }
    }

    private fun getDestination(
        sender: CommandSender,
        context: CommandContext,
        argument: ArgumentEntity,
    ): Entity? {
        val destination = context.get(argument).find(sender).firstOrNull()
        if (destination == null) {
            sender.sendMessage(Component.text("Destination not found").color(NamedTextColor.RED))
            return null
        }
        return destination
    }

    private fun getTargets(
        sender: CommandSender,
        context: CommandContext,
        argument: ArgumentEntity,
    ): List<Entity> {
        val targets = context.get(argument).find(sender)
        if (targets.isEmpty()) {
            sender.sendMessage(Component.text("No targets found").color(NamedTextColor.RED))
            return emptyList()
        }
        return targets
    }

    private fun getWorld(
        sender: CommandSender,
        context: CommandContext,
        argument: Argument<String>,
    ): Instance? {
        val worldName = context.get(argument)
        val world = MeliusServer.worldManager.getWorld(worldName)
        if (world == null) {
            sender.sendMessage(Component.text("World $worldName not found").color(NamedTextColor.RED))
            return null
        }
        return world
    }
}
