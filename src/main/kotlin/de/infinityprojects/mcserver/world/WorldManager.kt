package de.infinityprojects.mcserver.world

import de.infinityprojects.mcserver.data.WorldSpawn
import de.infinityprojects.mcserver.utils.CHUNK_SAVING_THREAD_NAME
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.coordinate.Pos
import net.minestom.server.event.EventNode
import net.minestom.server.instance.Instance
import net.minestom.server.instance.InstanceContainer
import net.minestom.server.instance.LightingChunk
import net.minestom.server.instance.anvil.AnvilLoader
import net.minestom.server.instance.block.Block
import net.minestom.server.instance.generator.Generator
import java.io.File

class WorldManager {
    val logger = ComponentLogger.logger("WorldManager")
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

        val node = EventNode.all("world_manager")
        MinecraftServer.getGlobalEventHandler().addChild(node)
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

    fun createWorld(name: String, generator: Generator): InstanceContainer {
        val world = createWorld(name)

        world.setGenerator(generator)

        return world
    }

    fun createWorld(name: String): InstanceContainer {
        if (worlds.containsKey(name)) {
            return worlds[name]!!
        }

        logger.info(
            Component
                .empty()
                .append(Component.text("Creating world "))
                .append(Component.text(name).color(NamedTextColor.BLUE).decorate(TextDecoration.BOLD)),
        )
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

    fun getDefaultWorldName(): String = worlds.keys.first()

    fun getWorldName(instance: Instance): String =
        worlds.entries.firstOrNull { it.value == instance }?.key
            ?: throw IllegalArgumentException("Instance not found")

    fun setSpawn(
        instance: Instance,
        coords: Pos,
    ) {
        val worldSpawn = WorldSpawn(coords)
        instance.setTag(WorldSpawn.getTag(), worldSpawn)
    }

    fun getSpawn(instance: Instance): Pos {
        val worldSpawn = instance.getTag(WorldSpawn.getTag())
        var highestBlock = 319
        instance.loadChunk(0, 0).join()
        while (instance.getBlock(0, highestBlock, 0) == Block.AIR) {
            highestBlock--
        }


        return worldSpawn?.pos ?: Pos(0.0, highestBlock + 1.0, 0.0)
    }
}
