package de.infinityprojects.mcserver.server

import de.infinityprojects.mcserver.commands.admin.Gamemode
import de.infinityprojects.mcserver.commands.admin.SetSpawn
import de.infinityprojects.mcserver.commands.admin.Teleport
import de.infinityprojects.mcserver.commands.performance.Cpu
import de.infinityprojects.mcserver.commands.performance.Ram
import de.infinityprojects.mcserver.commands.performance.Threads
import de.infinityprojects.mcserver.commands.performance.Tps
import de.infinityprojects.mcserver.utils.CONSOLE_THREAD_NAME
import de.infinityprojects.mcserver.utils.SERVER_BRAND
import net.minecrell.terminalconsole.SimpleTerminalConsole
import net.minestom.server.MinecraftServer
import org.jline.reader.*

class CommandManager {
    init {
        val thread =
            Thread({
                val console = Console()
                console.start()
            }, CONSOLE_THREAD_NAME)

        thread.start()

        val commands =
            listOf(
                Gamemode(),
                Ram(),
                Tps(),
                Cpu(),
                Threads(),
                Teleport(),
                SetSpawn(),
            )

        commands.forEach(MinecraftServer.getCommandManager()::register)
    }

    class Console : SimpleTerminalConsole() {
        override fun isRunning(): Boolean = MinecraftServer.isStarted()

        override fun runCommand(p0: String) {
            MinecraftServer.getCommandManager().execute(MinecraftServer.getCommandManager().consoleSender, p0)
        }

        override fun shutdown() {
            MinecraftServer.stopCleanly()
        }

        override fun buildReader(builder: LineReaderBuilder): LineReader =
            super.buildReader(builder.appName(SERVER_BRAND).completer(CommandCompleter()))
    }

    class CommandCompleter : Completer {
        override fun complete(
            p0: LineReader,
            p1: ParsedLine,
            p2: MutableList<Candidate>,
        ) {
            val buffer = p1.line()
            val split = buffer.split(" ")
            val command = split[0]

            val commands =
                MinecraftServer
                    .getCommandManager()
                    .commands
                    .map { it.names }
                    .toTypedArray()
            val completions = commands.flatten().filter { it.startsWith(command) }

            completions.forEach { p2.add(Candidate(it)) }
        }
    }
}
