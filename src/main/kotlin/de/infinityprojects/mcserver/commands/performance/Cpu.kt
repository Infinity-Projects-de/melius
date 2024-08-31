package de.infinityprojects.mcserver.commands.performance

import de.infinityprojects.mcserver.commands.performance.Ram.Companion.toggleBar
import de.infinityprojects.mcserver.utils.CPU_BENCHMARK_THREAD_NAME
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.MinecraftServer
import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.arguments.ArgumentType.Literal
import net.minestom.server.entity.Player
import net.minestom.server.timer.TaskSchedule
import java.text.DecimalFormat

class Cpu : Command("cpu") {
    private val bar = BossBar.bossBar(Component.empty(), 0.0f, BossBar.Color.BLUE, BossBar.Overlay.PROGRESS)
    private val f = DecimalFormat("0.00")

    init {
        val current = mutableListOf<ThreadData>()

        val thread =
            Thread({
                while (true) {
                    val threadInfos = ThreadData.getThreads()
                    for (info in threadInfos) {
                        val threadData = current.find { it.name == info.threadName }
                        if (threadData != null) {
                            threadData.update(info)
                        } else {
                            current.add(ThreadData(info))
                        }
                    }
                    Thread.sleep(1000)
                }
            }, CPU_BENCHMARK_THREAD_NAME)

        thread.isDaemon = true
        thread.start()

        MinecraftServer.getSchedulerManager().submitTask {
            val totals = getTotalPercentage(current)

            val progress = (totals.totalCpu / 100).coerceAtMost(1f)

            val text =
                Component
                    .empty()
                    .append(Component.text("CPU: ").color(NamedTextColor.GRAY))
                    .append(Component.text(f.format(totals.totalCpu)).color(Ram.getColor(progress, false)))
                    .append(Component.text("% User: ").color(NamedTextColor.GRAY))
                    .append(Component.text(f.format(totals.totalUser)).color(Ram.getColor(totals.totalUser / 100, false)))
                    .append(Component.text("% Blocked: ").color(NamedTextColor.GRAY))
                    .append(Component.text(f.format(totals.totalBlocked)).color(Ram.getColor(totals.totalBlocked / 100, false)))
                    .append(Component.text("% Waited: ").color(NamedTextColor.GRAY))
                    .append(Component.text(f.format(totals.totalWaited)).color(Ram.getColor(totals.totalWaited / 100, false)))
                    .append(Component.text("%").color(NamedTextColor.GRAY))

            bar.name(text)
            bar.progress(progress)
            bar.color(Ram.getBarColor(progress, false))

            return@submitTask TaskSchedule.tick(20)
        }

        setDefaultExecutor { sender, _ ->
            show(sender, false, current)
        }

        val arg = Literal("bar")
        addSyntax(
            { sender, _ ->
                if (sender is Player) {
                    toggleBar(sender, bar)
                }
            },
            arg,
        )

        val showAll = Literal("all")

        addSyntax(
            { sender, _ ->
                show(sender, true, current)
            },
            showAll,
        )
    }

    fun show(
        sender: CommandSender,
        showAll: Boolean,
        current: List<ThreadData>,
    ) {
        val totals = getTotalPercentage(current)
        sender.sendMessage(
            Component
                .empty()
                .append(Component.text("Total CPU: ${f.format(totals.totalCpu)}%").color(NamedTextColor.GREEN))
                .append(Component.text(" User: ${f.format(totals.totalUser)}%").color(NamedTextColor.AQUA))
                .append(Component.text(" Blocked: ${f.format(totals.totalBlocked)}%").color(NamedTextColor.RED))
                .append(Component.text(" Waited: ${f.format(totals.totalWaited)}%").color(NamedTextColor.YELLOW)),
        )

        var notShown = 0
        for (data in current) {
            if (data.cpuPercentage == 0f && !showAll) {
                notShown++
                continue
            }
            sender.sendMessage(
                Component
                    .empty()
                    .append(Component.text(data.name).color(NamedTextColor.GRAY))
                    .append(Component.text(" CPU: ${f.format(data.cpuPercentage)}%").color(NamedTextColor.GREEN))
                    .append(Component.text(" User: ${f.format(data.userPercentage)}%").color(NamedTextColor.AQUA))
                    .append(Component.text(" Blocked: ${f.format(data.blockedPercentage)}%").color(NamedTextColor.RED))
                    .append(Component.text(" Waited: ${f.format(data.waitedPercentage)}%").color(NamedTextColor.YELLOW)),
            )
        }

        if (notShown > 0) {
            sender.sendMessage(Component.text("Not shown (0% CPU): $notShown").color(NamedTextColor.GRAY))
        }
    }

    private data class Totals(
        val totalCpu: Float,
        val totalUser: Float,
        val totalBlocked: Float,
        val totalWaited: Float,
    )

    companion object {
        private fun getTotalPercentage(threads: List<ThreadData>): Totals {
            var totalCpu = 0f
            var totalUser = 0f
            var totalBlocked = 0f
            var totalWaited = 0f
            for (data in threads) {
                totalCpu += data.cpuPercentage
                totalUser += data.userPercentage
                totalBlocked += data.blockedPercentage
                totalWaited += data.waitedPercentage
            }
            return Totals(totalCpu, totalUser, totalBlocked, totalWaited)
        }
    }
}
