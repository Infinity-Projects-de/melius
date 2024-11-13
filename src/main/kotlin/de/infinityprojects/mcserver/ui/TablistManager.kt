package de.infinityprojects.mcserver.ui

import net.kyori.adventure.text.Component
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket
import org.yaml.snakeyaml.Yaml
import java.io.File

class TablistManager {
    var header: ArrayList<UpdateableComponent> = arrayListOf()
    var footer: ArrayList<UpdateableComponent> = arrayListOf()


    val animatedFrames = mutableSetOf<String>()

    init {
        load()

        val node = EventNode.event("tablist_manager", EventFilter.ALL) { event ->
            return@event event is TextAnimationEvent && event.updated.any { animatedFrames.contains(it) }
        }

        node.addListener(TextAnimationEvent::class.java) { event ->
            var updated = false
            event.updated.forEach { h ->
                header.forEach { c ->
                    if (c.updateable(h)) {
                        c.update()
                        updated = true
                    }
                }

                footer.forEach { c ->
                    if (c.updateable(h)) {
                        c.update()
                        updated = true
                    }
                }
            }


            if (updated) {
                for (player in MinecraftServer.getConnectionManager().onlinePlayers) {
                    sendTabList(player)
                }
            }
        }

        MinecraftServer.getGlobalEventHandler().addChild(node)


    }

    fun load() {
        val yaml = Yaml()
        val file = File("tablist.yml")
        val config = yaml.load(file.inputStream()) as Map<String, Any>
        header.clear()
        footer.clear()
        animatedFrames.clear()

        if (config.isNotEmpty()) {
            if (config.getOrDefault("enabled", false) == true) {
                header.addAll(getStringList(config, "header"))
                footer.addAll(getStringList(config, "footer"))

                header.forEach {
                    animatedFrames.addAll(it.animationHolders)
                }

                footer.forEach {
                    animatedFrames.addAll(it.animationHolders)
                }
            }
        }
    }

    private fun getStringList(config: Map<String, Any>, key: String): Array<UpdateableComponent> {
        val list = config.getOrDefault(key, listOf<String>()) as List<String>
        return list.map { UpdateableComponent(it) }.toTypedArray()
    }

    fun sendTabList(player: Player) {
        val headerComp = Component.text()
        header.forEachIndexed { i, it ->
            if (i > 0) headerComp.appendNewline()
            headerComp.append(it)
        }

        val footerComp = Component.text()
        footer.forEachIndexed { i, it ->
            if (i > 0) footerComp.appendNewline()
            footerComp.append(it)
        }

        val packet = PlayerListHeaderAndFooterPacket(headerComp.build(), footerComp.build())

        player.sendPacket(packet)
    }
}