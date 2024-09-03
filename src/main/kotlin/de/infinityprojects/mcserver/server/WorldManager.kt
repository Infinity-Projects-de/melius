package de.infinityprojects.mcserver.server

import de.infinityprojects.mcserver.utils.CHUNK_SAVING_THREAD_NAME
import net.minestom.server.MinecraftServer
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.batch.ChunkBatch
import net.minestom.server.instance.block.Block
import org.slf4j.LoggerFactory
import java.io.File

class WorldManager {
    val LOGGER = LoggerFactory.getLogger("WorldManager")
    val worlds = hashMapOf<String, InstanceContainer>()
    var autoSaveEnabled = false

    init {
        val node = EventNode.type("block_manager", EventFilter.BLOCK)
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
                                LOGGER.debug("Saved chunks for $name in ${endTime - startTime}ms")
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
        // Create the instance
        val instanceManager = MinecraftServer.getInstanceManager()
        val instanceContainer = instanceManager.createInstanceContainer()
        instanceContainer.setChunkSupplier(::LightingChunk)
        instanceContainer.chunkLoader = AnvilLoader("worlds/$name")

        instanceContainer.loadChunk(0, 0).thenAccept { chunk ->
            ChunkBatch().apply(instanceContainer, chunk) { batch ->
                for (x in 0 until 16) {
                    for (z in 0 until 16) {
                        batch.setBlock(x, 40, z, Block.STONE)
                    }
                }
            }
        }

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
