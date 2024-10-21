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
                    .append(Component.text("Hardware threads: ${Runtime.getRuntime().availableProcessors()}"))
                    .color(NamedTextColor.GRAY),
            )
            sender.sendMessage(
                Component
                    .empty()
                    .append(Component.text("Thread count: ${threads.size}"))
                    .color(NamedTextColor.GRAY),
            )

            val grouped = groupByCommonParts(threads.keys.toList())

            for ((name, threads) in grouped) {
                val responsible = threads[0]
                sender.sendMessage(
                    Component
                        .empty()
                        .append(Component.text(name).color(NamedTextColor.GRAY))
                        .let {
                            if (threads.size > 1) {
                                it.append(Component.text(" (${threads.size} threads)").color(NamedTextColor.GREEN))
                            } else {
                                it
                            }
                        }.let {
                            if (responsible.isDaemon) {
                                it.append(Component.text(" (daemon)").color(NamedTextColor.BLUE))
                            } else {
                                it
                            }
                        }.let {
                            if (!responsible.isAlive) {
                                it.append(Component.text(" (dead)").color(NamedTextColor.RED))
                            } else {
                                it
                            }
                        }.let {
                            if (responsible.isVirtual) {
                                it.append(Component.text(" (virtual)").color(NamedTextColor.AQUA))
                            } else {
                                it
                            }
                        },
                )
            }
        }

        addSyntax({ sender, _ ->
            if (sender is Player) {
                toggleBar(sender, bar)
            }
        }, ArgumentType.Literal("bar"))
    }

    fun groupByCommonParts(threads: List<Thread>): Map<String, MutableList<Thread>> {
        val grouped = mutableMapOf<String, MutableList<Thread>>()
        for (thread in threads) {
            val name = thread.name
            val withoutLastChar = name.substring(0, name.length - 2)

            val list = grouped.computeIfAbsent(withoutLastChar) { mutableListOf() }
            list.add(thread)
        }
        for ((name, list) in grouped.filter { it.value.size == 1 }) {
            val thread = list[0]
            grouped.remove(name)
            grouped.computeIfAbsent(thread.name) { mutableListOf() }.add(thread)
        }

        return grouped
    }
}
