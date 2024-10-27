package de.infinityprojects.mcserver.server

import net.kyori.adventure.text.Component
import net.minestom.server.entity.Player
import net.minestom.server.scoreboard.Sidebar
import org.yaml.snakeyaml.Yaml
import java.io.File

class ScoreboardManager {
    val sidebars: HashMap<String, Sidebar> = hashMapOf()

    init {
        val yaml = Yaml()
        val file = File("scoreboard.yml")
        val config = yaml.load(file.inputStream()) as Map<String, Any>

        if (config.isNotEmpty()) {

            config.entries.forEach { (key, value) ->
                if (value is Map<*, *>) {
                    if (value["enabled"] == true) {
                        val lines = value["content"]?.let {
                            if (it is List<*>) {
                                it.filterIsInstance<String>()
                            } else {
                                listOf<String>()
                            }
                        } ?: listOf<String>()

                        val title = value["title"] as String

                        val sidebar = Sidebar(Component.text(title))
                        var i = lines.size - 1
                        lines.forEach {
                            sidebar.createLine(
                                Sidebar.ScoreboardLine(
                                    "line_$i",
                                    Component.text(it),
                                    i,
                                )
                            )
                            i--
                        }

                        sidebars[key] = sidebar
                    }
                }
             }
        }
    }

    fun sendScoreboard(player: Player) {
        sidebars.values.firstOrNull()?.addViewer(player)
    }
}