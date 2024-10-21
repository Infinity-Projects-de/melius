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
            var totals = 0f
            for (data in current) {
                totals += data.cpuPercentage
            }

            val progress = (totals / 100).coerceAtMost(1f)

            val text =
                Component
                    .empty()
                    .append(Component.text("CPU: ").color(NamedTextColor.GRAY))
                    .append(Component.text("${f.format(totals)}%").color(Ram.getColor(progress, false)))
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
        var totalCpu = 0f
        var totalUser = 0f
        var totalBlocked = 0f
        var totalWaited = 0f

        var notShown = 0
        for (data in current.sortedBy { it.cpuPercentage }) {
            totalCpu += data.cpuPercentage
            totalUser += data.userPercentage
            totalBlocked += data.blockedPercentage
            totalWaited += data.waitedPercentage

            if (data.cpuPercentage == 0f && !showAll) {
                notShown++
                continue
            }
            sender.sendMessage(
                Component
                    .empty()
                    .append(Component.text("${f.format(data.cpuPercentage)}% ").color(NamedTextColor.GREEN))
                    .append(Component.text(data.name).color(NamedTextColor.GRAY)),
            )
        }

        if (notShown > 0) {
            sender.sendMessage(Component.text("Not shown (0% CPU): $notShown").color(NamedTextColor.GRAY))
        }

        sender.sendMessage(
            Component
                .empty()
                .append(Component.text("Total CPU: ${f.format(totalCpu)}%").color(NamedTextColor.GREEN))
                .append(Component.text(" User: ${f.format(totalUser)}%").color(NamedTextColor.GRAY))
                .append(Component.text(" Blocked: ${f.format(totalBlocked)}%").color(NamedTextColor.GRAY))
                .append(Component.text(" Waited: ${f.format(totalWaited)}%").color(NamedTextColor.GRAY)),
        )
    }
}
