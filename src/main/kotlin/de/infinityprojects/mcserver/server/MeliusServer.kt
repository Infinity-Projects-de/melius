package de.infinityprojects.mcserver.server

import de.infinityprojects.mcserver.config.PropertiesConfiguration
import de.infinityprojects.mcserver.utils.SERVER_BRAND
import de.infinityprojects.mcserver.utils.STARTUP_MESSAGE
import de.infinityprojects.mcserver.utils.logSystemInfo
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory

object MeliusServer {
    private val logger = LoggerFactory.getLogger("Server")
    lateinit var worldManager: WorldManager
    lateinit var playerManager: PlayerManager
    lateinit var commandManager: CommandManager
    val config = PropertiesConfiguration()

    fun init() {
        val start = System.currentTimeMillis()
        val minecraftServer = MinecraftServer.init()
        MinecraftServer.setBrandName(SERVER_BRAND)

        // STARTUP MESSAGE
        STARTUP_MESSAGE.forEach(logger::info)
        logSystemInfo()

        // CONFIG
        config.saveDefault()
        config.load()
        config.generateMissing()

        // INIT MANAGERS
        worldManager = WorldManager()
        if (config.getBoolean("auto-save")) {
            worldManager.autoSave(true)
        }

        worldManager.createWorld("world")

        playerManager = PlayerManager()
        commandManager = CommandManager()

        // LISTEN TO IP AND PORT
        val ip = config.getString("server-ip")
        val port = config.getInt("server-port")
        logger.info("Starting server on $ip:$port")
        minecraftServer.start(ip, port)

        // TIMING
        val end = System.currentTimeMillis()
        logger.info("Server started in ${end - start}ms")
    }
}
