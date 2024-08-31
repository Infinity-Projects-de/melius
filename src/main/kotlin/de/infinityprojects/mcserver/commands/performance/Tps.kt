package de.infinityprojects.mcserver.commands.performance

import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType
import net.minestom.server.entity.Player
import net.minestom.server.timer.ExecutionType
import net.minestom.server.timer.TaskSchedule
import java.text.DecimalFormat
import kotlin.math.min

class Tps : Command("tps") {
    private val msptBuffer = CircularBuffer(6000) // 20 ticks * 60 seconds * 5 miutes
    private val bar = BossBar.bossBar(Component.empty(), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)

    init {
        var startTick = System.nanoTime()

        MinecraftServer.getSchedulerManager().scheduleTask({
            startTick = System.nanoTime()
        }, TaskSchedule.immediate(), TaskSchedule.tick(1), ExecutionType.TICK_START)

        MinecraftServer.getSchedulerManager().scheduleTask({
            val endTick = System.nanoTime()
            msptBuffer.add((endTick - startTick) / 1_000_000f)
        }, TaskSchedule.immediate(), TaskSchedule.tick(1), ExecutionType.TICK_END)

        val f = DecimalFormat("0.0000")

        MinecraftServer.getSchedulerManager().submitTask {
            if (msptBuffer.last() == 0f) {
                return@submitTask TaskSchedule.tick(5)
            }
            if (MinecraftServer.getBossBarManager().getBossBarViewers(bar).isEmpty()) {
                return@submitTask TaskSchedule.tick(5)
            }
            val lastMS = msptBuffer.last()
            val tps = minTPS(1000 / lastMS)
            val progress = min(tps / 20f, 1f)
            bar.progress(progress)
            bar.color(Ram.getBarColor(progress, true))

            val color = Ram.getColor(progress, true)

            val tpsText =
                Component
                    .text("TPS: ")
                    .color(NamedTextColor.GRAY)
                    .append(Component.text(tps).color(color))
                    .append(Component.text(" | MSPT: ").color(NamedTextColor.GRAY))
                    .append(Component.text(f.format(lastMS)).color(color))
                    .append(Component.text("ms").color(color))

            bar.name(tpsText)

            return@submitTask TaskSchedule.tick(5)
        }
        setDefaultExecutor { sender, _ ->
            sender.sendMessage("§7Last MSPT: ${f.format(msptBuffer.last())}ms")
            sender.sendMessage("§7Average MSPT (1min): ${f.format(msptBuffer.averageOfLast(1200))}ms")
            sender.sendMessage("§7Average MSPT (5min): ${f.format(msptBuffer.average())}ms")
            sender.sendMessage("§7Max MSPT (5min): ${f.format(msptBuffer.max())}ms")
            sender.sendMessage("§7")
            sender.sendMessage("§7Last TPS: ${minTPS(1000 / msptBuffer.average())}") // 1000ms / 1s
            sender.sendMessage("§7Average TPS (1min): ${minTPS(1000 / msptBuffer.averageOfLast(1200))}")
            sender.sendMessage("§7Average TPS (5min): ${minTPS(1000 / msptBuffer.average())}")
            sender.sendMessage("§7Min TPS (5min): ${minTPS(1000 / msptBuffer.max())}")
        }

        val arg = ArgumentType.Literal("bar")

        addSyntax({ sender, _ ->
            if (sender is Player) {
                Ram.toggleBar(sender, bar)
            }
        }, arg)
    }

    fun minTPS(tps: Float): Int = min(20, tps.toInt())
}
