package de.infinityprojects.mcserver.server

import de.infinityprojects.mcserver.utils.CHUNK_SAVING_THREAD_NAME
import net.minestom.server.MinecraftServer
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import org.slf4j.LoggerFactory
import java.io.File

class WorldManager {
    val logger = LoggerFactory.getLogger("WorldManager")
    val worlds = hashMapOf<String, InstanceContainer>()
    var autoSaveEnabled = false

    init {
        val worldsFolder = File("worlds")
        if (!worldsFolder.exists()) {
            worldsFolder.mkdirs()
        }

        worldsFolder.listFiles()?.forEach {
            if (it.isDirectory) {
                if (it.listFiles { _, name -> name == "region" }?.isNotEmpty() == true) {
                    createWorld(it.name)
                }
            }
        }
    }

    fun autoSave(enable: Boolean) {
        autoSaveEnabled = enable

        if (enable) {
            val thread =
                Thread({
                    while (autoSaveEnabled) {
                        for ((name, world) in worlds) {
                            val startTime = System.currentTimeMillis()
                            world.saveChunksToStorage().thenRun {
                                val endTime = System.currentTimeMillis()
                                logger.debug("Saved chunks for $name in ${endTime - startTime}ms")
                            }
                        }
                        Thread.sleep(60000)
                    }
                }, CHUNK_SAVING_THREAD_NAME)
            thread.start()
        }
    }

    fun createWorld(name: String): InstanceContainer {
        if (worlds.containsKey(name)) {
            return worlds[name]!!
        }

        logger.info("Creating world $name")
        // Create the instance
        val instanceManager = MinecraftServer.getInstanceManager()
        val instanceContainer = instanceManager.createInstanceContainer()
        instanceContainer.setChunkSupplier(::LightingChunk)
        instanceContainer.chunkLoader = AnvilLoader("worlds/$name")

        worlds[name] = instanceContainer
        return instanceContainer
    }

    fun getWorld(name: String): InstanceContainer? {
        if (!worlds.containsKey(name)) {
            val folder = File("worlds/$name")
            return if (folder.exists()) {
                createWorld(name)
            } else {
                null
            }
        }
        return worlds[name]
    }

    fun unloadWorld(name: String) {
        val instance = worlds[name]
        if (instance != null) {
            instance.saveChunksToStorage()
            worlds.remove(name)
        }
    }

    fun getDefaultWorld(): InstanceContainer = worlds.values.first()
}
