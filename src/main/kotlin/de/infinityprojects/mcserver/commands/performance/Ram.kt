package de.infinityprojects.mcserver.commands.performance

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.timer.TaskSchedule
import kotlin.math.min

class Ram : Command("ram") {
    private val bar = BossBar.bossBar(Component.empty(), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)

    init {
        MinecraftServer.getSchedulerManager().submitTask {
            val usedMemory = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) shr 20
            val maxMemory = Runtime.getRuntime().maxMemory() shr 20
            val progress = min(usedMemory.toFloat() / maxMemory.toFloat(), 1f)
            bar.progress(progress)
            bar.color(getBarColor(progress, false))

            val ramText =
                Component
                    .text("RAM: ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(usedMemory).color(getColor(progress, false)))
                    .append(Component.text("/").color(NamedTextColor.GRAY))
                    .append(Component.text(maxMemory).color(NamedTextColor.GRAY))
                    .append(Component.text("MB").color(NamedTextColor.GRAY))

            bar.name(ramText)

            return@submitTask TaskSchedule.tick(5)
        }

        setDefaultExecutor { sender, _ ->
            sender.sendMessage("§7Total memory: ${Runtime.getRuntime().totalMemory() / 1024 / 1024} MB")
            sender.sendMessage("§7Free memory: ${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB")
            sender.sendMessage(
                "§7Used memory: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024} MB",
            )
            sender.sendMessage("§7Max memory: ${Runtime.getRuntime().maxMemory() / 1024 / 1024} MB")
        }

        val arg = ArgumentType.Literal("bar")

        addSyntax({ sender, _ ->
            if (sender is Player) {
                toggleBar(sender, bar)
            }
        }, arg)
    }

    companion object {
        fun toggleBar(
            player: Player,
            bar: BossBar,
        ) {
            if (MinecraftServer.getBossBarManager().getBossBarViewers(bar).contains(player)) {
                bar.removeViewer(player)
            } else {
                bar.addViewer(player)
            }
        }

        fun interpolateColor(
            start: Int,
            end: Int,
            progress: Float,
        ): Int {
            val startR = start shr 16 and 0xff
            val startG = start shr 8 and 0xff
            val startB = start and 0xff

            val endR = end shr 16 and 0xff
            val endG = end shr 8 and 0xff
            val endB = end and 0xff

            val r = ((startR + (endR - startR) * progress).toInt() shl 16 and 0xff0000)
            val g = ((startG + (endG - startG) * progress).toInt() shl 8 and 0xff00)
            val b = ((startB + (endB - startB) * progress).toInt() and 0xff)

            return r or g or b
        }

        fun getColor(
            progress: Float,
            reverse: Boolean,
        ): TextColor {
            val turnedProgress =
                if (reverse) {
                    1f - progress
                } else {
                    progress
                }

            val color =
                if (turnedProgress < 0.5f) {
                    interpolateColor(0x00FF00, 0xFFFF00, turnedProgress * 2)
                } else {
                    interpolateColor(0xFFFF00, 0xFF0000, (turnedProgress - 0.5f) * 2)
                }
            return TextColor.color(color)
        }

        fun getBarColor(
            progress: Float,
            reverse: Boolean,
        ): BossBar.Color {
            val turnedProgress =
                if (reverse) {
                    1f - progress
                } else {
                    progress
                }

            return when {
                turnedProgress < 0.45f -> BossBar.Color.GREEN
                turnedProgress < 0.65f -> BossBar.Color.YELLOW
                else -> BossBar.Color.RED
            }
        }
    }
}
