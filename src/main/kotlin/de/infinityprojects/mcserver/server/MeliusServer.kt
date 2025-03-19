package de.infinityprojects.mcserver.server

import de.infinityprojects.mcserver.config.PropertiesConfiguration
import de.infinityprojects.mcserver.entity.vehicle.VehicleHandler
import de.infinityprojects.mcserver.ui.TextAnimationEngine
import de.infinityprojects.mcserver.utils.SERVER_BRAND
import de.infinityprojects.mcserver.utils.STARTUP_MESSAGE
import de.infinityprojects.mcserver.utils.logSystemInfo
import de.infinityprojects.mcserver.world.WorldManager
import de.infinityprojects.mcserver.world.generator.FlatWorldGenerator
import de.infinityprojects.mcserver.world.generator.NoisyWorldGenerator
import de.infinityprojects.mcserver.world.generator.OverworldGenerator
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.MinecraftServer
import net.minestom.server.event.server.ServerListPingEvent
import net.minestom.server.extras.lan.OpenToLAN
import org.slf4j.LoggerFactory
import java.io.File

object MeliusServer {
    private val logger = LoggerFactory.getLogger("Server")
    lateinit var worldManager: WorldManager
    lateinit var playerManager: PlayerManager
    lateinit var commandManager: CommandManager
    lateinit var textAnimationEngine: TextAnimationEngine
    lateinit var chatManager: ChatManager
    val config = PropertiesConfiguration()

    fun init() {
        val start = System.currentTimeMillis()
        Thread.setDefaultUncaughtExceptionHandler { _, e ->
            logger.error(
                "An uncaught exception happened, you can find more details in the debug file: {}",
                e.toString()
            )
            logger.debug("Start of stacktrace", e)
        }

        val minecraftServer = MinecraftServer.init()
        MinecraftServer.setBrandName(SERVER_BRAND)

        // STARTUP MESSAGE
        STARTUP_MESSAGE.forEach(logger::info)
        logSystemInfo()

        // CONFIG
        config.saveDefault()

        saveDefault("animations.yml")
        saveDefault("scoreboard.yml")
        saveDefault("tablist.yml")

        // INIT MANAGERS
        worldManager = WorldManager()
        if (config.getBoolean("auto-save")) {
            worldManager.autoSave(true)
        }

        worldManager.createWorld("world", OverworldGenerator())
        worldManager.createWorld("flat", FlatWorldGenerator())
        worldManager.createWorld("noisy", NoisyWorldGenerator())


        playerManager = PlayerManager()
        chatManager = ChatManager()
        commandManager = CommandManager()
        textAnimationEngine = TextAnimationEngine()
        VehicleHandler()

        // MOTD
        MinecraftServer.getGlobalEventHandler().addListener(ServerListPingEvent::class.java) { event ->
            val motdString = config.getString("motd-line1") + "\n" + config.getString("motd-line2")
            val component = LegacyComponentSerializer.legacySection().deserialize(motdString)
            event.responseData.description = component
        }

        // LISTEN TO IP AND PORT
        val ip = config.getString("server-ip")
        val port = config.getInt("server-port")
        logger.info("Starting server on $ip:$port")
        minecraftServer.start(ip, port)

        if (config.getBoolean("open-to-lan")) {
            logger.info("Opening server to LAN")
            OpenToLAN.open()
        }
        // TIMING
        val end = System.currentTimeMillis()
        logger.info("Server started in ${end - start}ms")

        // SHUTDOWN LOGIC
        Runtime.getRuntime().addShutdownHook(
            Thread {
                logger.info("Shutting down server")
            },
        )
    }

    fun saveDefault(fileName: String) {
        val file = File(fileName)
        if (file.exists()) {
            return
        }

        val defaultFile = javaClass.getResourceAsStream("/default/$fileName")
            ?: throw IllegalArgumentException("Resource not found: $fileName")
        defaultFile.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }
}
