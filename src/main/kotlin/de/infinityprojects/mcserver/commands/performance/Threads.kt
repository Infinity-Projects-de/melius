package de.infinityprojects.mcserver.commands.performance

import de.infinityprojects.mcserver.commands.performance.Ram.Companion.toggleBar
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.timer.TaskSchedule

class Threads : Command("threads") {
    private val bar = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)

    init {
        MinecraftServer.getSchedulerManager().submitTask {
            val threads = Thread.getAllStackTraces()
            val threadCount = threads.size
            val alive = threads.count { it.key.isAlive }
            val progress = alive.toFloat() / threadCount.toFloat()

            val color = Ram.getColor(progress, true)
            val threadText =
                Component
                    .empty()
                    .append(Component.text(threads.count { it.key.isAlive }).color(color))
                    .append(Component.text("/").color(NamedTextColor.GRAY))
                    .append(Component.text(threadCount).color(NamedTextColor.GRAY))
                    .append(Component.text(" threads alive").color(NamedTextColor.GRAY))

            bar.name(threadText)
            bar.progress(progress)
            bar.color(Ram.getBarColor(progress, true))
            return@submitTask TaskSchedule.tick(5)
        }

        setDefaultExecutor { sender, _ ->
            val threads = Thread.getAllStackTraces()
            sender.sendMessage(
                Component
                    .empty()
                    .append(Component.text("Thread count: ${threads.size}"))
                    .color(NamedTextColor.GRAY),
            )

            for ((thread, _) in threads) {
                sender.sendMessage(
                    Component
                        .empty()
                        .append(Component.text(thread.name))
                        .color(NamedTextColor.GRAY),
                )
            }
        }

        addSyntax({ sender, _ ->
            if (sender is Player) {
                toggleBar(sender, bar)
            }
        }, ArgumentType.Literal("bar"))
    }
}
