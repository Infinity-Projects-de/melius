package de.infinityprojects.mcserver.server

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.minestom.server.entity.Player
import net.minestom.server.network.packet.server.play.PlayerListHeaderAndFooterPacket
import org.yaml.snakeyaml.Yaml
import java.io.File

class TablistManager {
    val header: TextComponent
    val footer: TextComponent


    init {
        val yaml = Yaml()
        val file = File("tablist.yml")
        val config = yaml.load(file.inputStream()) as Map<String, Any>

        if (config.isEmpty()) {
            header = Component.empty()
            footer = Component.empty()
        } else {
            if (config.getOrDefault("enabled", false) == true) {
                val headerLines: List<String> = config["header"]?.let {
                    if (it is List<*>) {
                        it.filterIsInstance<String>()
                    } else {
                        listOf<String>()
                    }
                } ?: listOf<String>()

                val footerLines: List<String> = config["footer"]?.let {
                    if (it is List<*>) {
                        it.filterIsInstance<String>()
                    } else {
                        listOf<String>()
                    }
                } ?: listOf<String>()

                var component = Component.empty()
                headerLines.forEach {
                    val parsedAmpersand = LegacyComponentSerializer.legacyAmpersand().deserialize(it)
                    val unparsed = LegacyComponentSerializer.legacySection().serialize(parsedAmpersand)
                    val parsed = LegacyComponentSerializer.legacySection().deserialize(unparsed)

                    component = component.append(parsed)
                }

                header = component

                component = Component.empty()
                footerLines.forEach {
                    val parsedAmpersand = LegacyComponentSerializer.legacyAmpersand().deserialize(it)
                    val unparsed = LegacyComponentSerializer.legacySection().serialize(parsedAmpersand)
                    val parsed = LegacyComponentSerializer.legacySection().deserialize(unparsed)

                    component = component.append(parsed)
                }

                footer = component
            } else {
                header = Component.empty()
                footer = Component.empty()
            }
        }
    }

    fun sendTabList(player: Player) {
        val tablistPacket = PlayerListHeaderAndFooterPacket(header, footer)
        player.sendPacket(tablistPacket)
    }
}