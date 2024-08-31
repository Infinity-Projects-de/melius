package de.infinityprojects.mcserver

import de.infinityprojects.mcserver.commands.admin.Gamemode
import de.infinityprojects.mcserver.commands.performance.Cpu
import de.infinityprojects.mcserver.commands.performance.Ram
import de.infinityprojects.mcserver.commands.performance.Threads
import de.infinityprojects.mcserver.commands.performance.Tps
import de.infinityprojects.mcserver.config.PropertiesConfiguration
import de.infinityprojects.mcserver.utils.CHUNK_SAVING_THREAD_NAME
import de.infinityprojects.mcserver.utils.SERVER_BRAND
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent
import net.minestom.server.event.player.PlayerDisconnectEvent
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.block.Block
import org.slf4j.LoggerFactory

fun main() {
    val start = System.currentTimeMillis()
    val logger = LoggerFactory.getLogger("Server")
    listOf(
        "\u001B[38;5;208m███╗░░░███╗███████╗██╗░░░░░██╗██╗░░░██╗░██████╗",
        "\u001B[38;5;209m████╗░████║██╔════╝██║░░░░░██║██║░░░██║██╔════╝",
        "\u001B[38;5;210m██╔████╔██║█████╗░░██║░░░░░██║██║░░░██║╚█████╗░",
        "\u001B[38;5;211m██║╚██╔╝██║██╔══╝░░██║░░░░░██║██║░░░██║░╚═══██╗",
        "\u001B[38;5;212m██║░╚═╝░██║███████╗███████╗██║╚██████╔╝██████╔╝",
        "\u001B[38;5;213m╚═╝░░░░░╚═╝╚══════╝╚══════╝╚═╝░╚═════╝░╚═════╝░",
        "\u001B[38;5;206mThe best server platform available!",
        "\u001B[38;5;207mVisit us at MELIUSMC.INFO!",
    ).forEach(logger::info)
    logSystemInfo()

    // Config
    val config = PropertiesConfiguration()
    config.saveDefault()
    config.load()
    config.generateMissing()

    // Initialization
    val minecraftServer = MinecraftServer.init()
    MinecraftServer.setBrandName(SERVER_BRAND)

    // Create the instance
    val instanceManager = MinecraftServer.getInstanceManager()
    val instanceContainer = instanceManager.createInstanceContainer()
    instanceContainer.setChunkSupplier(::LightingChunk)
    instanceContainer.chunkLoader = AnvilLoader("worlds/world")

    for (i in -10..10) {
        for (j in -10..10) {
            instanceContainer.setBlock(Pos(i.toDouble(), 40.0, j.toDouble()), Block.STONE)
        }
    }

    // Add an event callback to specify the spawning instance (and the spawn position)
    val globalEventHandler = MinecraftServer.getGlobalEventHandler()
    globalEventHandler.addListener(AsyncPlayerConfigurationEvent::class.java) { event ->
        val player = event.player
        event.spawningInstance = instanceContainer
        player.respawnPoint = Pos(0.0, 42.0, 0.0)
        logger.info("Player ${player.username} joined the server")
    }

    globalEventHandler.addListener(PlayerDisconnectEvent::class.java) { event ->
        logger.info("Player ${event.player.username} left the server")
    }

    if (config.getBoolean("auto-save")) {
        val thread =
            Thread({
                while (true) {
                    Thread.sleep(60000)

                    val startTime = System.currentTimeMillis()
                    instanceContainer.saveChunksToStorage().thenRun {
                        val endTime = System.currentTimeMillis()
                        logger.debug("Saved chunks in ${endTime - startTime}ms")
                    }
                }
            }, CHUNK_SAVING_THREAD_NAME)

        thread.isDaemon = true
        thread.start()
    }

    registerCommands()

    val ip = config.getString("server-ip")
    val port = config.getInt("server-port")

    logger.info("Starting server on $ip:$port")

    minecraftServer.start(ip, port)

    val end = System.currentTimeMillis()
    logger.info("Server started in ${end - start}ms")
}

fun registerCommands() {
    val commands =
        listOf(
            Gamemode(),
            Ram(),
            Tps(),
            Cpu(),
            Threads(),
        )

    commands.forEach(MinecraftServer.getCommandManager()::register)
}

fun logSystemInfo() {
    val logger = LoggerFactory.getLogger("SystemInfo")
    val meliusProperties = PropertiesConfiguration("melius.properties")
    meliusProperties.load()

    logger.info(
        "Java {} ({}) on {} {} ({})",
        System.getProperty("java.version"),
        System.getProperty("java.vendor"),
        System.getProperty("os.name"),
        System.getProperty("os.version"),
        System.getProperty("os.arch"),
    )

    logger.info(
        "Minecraft version: {} - Melius version: {}",
        MinecraftServer.VERSION_NAME,
        meliusProperties.getString("version"),
    )

    logger.info(
        "{} available threads and {}MB max memory",
        Runtime.getRuntime().availableProcessors(),
        Runtime.getRuntime().maxMemory() / 1024 / 1024,
    )
}
