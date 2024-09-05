package de.infinityprojects.mcserver.utils

import de.infinityprojects.mcserver.config.PropertiesConfiguration
import net.minestom.server.MinecraftServer
import org.slf4j.LoggerFactory

fun logSystemInfo() {
    val logger = LoggerFactory.getLogger("SystemInfo")
    val meliusProperties = PropertiesConfiguration("melius.properties", "/melius.properties", false)

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
