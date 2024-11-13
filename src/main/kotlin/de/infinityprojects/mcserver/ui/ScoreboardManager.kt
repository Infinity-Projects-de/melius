package de.infinityprojects.mcserver.ui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.entity.Player
import net.minestom.server.event.EventFilter
import net.minestom.server.event.EventNode
import net.minestom.server.scoreboard.Sidebar
import org.yaml.snakeyaml.Yaml
import java.io.File
import kotlin.collections.get

class ScoreboardManager {
    val sidebars: HashMap<String, Sidebar> = hashMapOf()
    val components: HashMap<String, ArrayList<UpdateableComponent>> = hashMapOf()
    val animatedFrames: MutableMap<String, MutableSet<String>> = mutableMapOf()

    val logger = ComponentLogger.logger("ScoreboardManager")

    fun load() {
        val yaml = Yaml()
        val file = File("scoreboard.yml")
        val config = yaml.load(file.inputStream()) as Map<String, Any>

        sidebars.clear()
        components.clear()
        animatedFrames.clear()

        if (config.isNotEmpty()) {
            config.entries.forEach { (key, value) ->
                if (value is Map<*, *>) {
                    if (value["enabled"] == true) {
                        val lines = value["content"] as List<String>
                        val title = value["title"] as String

                        val sidebar = Sidebar(Component.text(title))
                        sidebars[key] = sidebar

                        val comps = arrayListOf<UpdateableComponent>()
                        lines.reversed().forEachIndexed { i, it ->
                            val comp = UpdateableComponent(it)
                            animatedFrames.computeIfAbsent(key) { mutableSetOf() } += comp.animationHolders
                            comps.add(comp)
                            sidebar.createLine(
                                Sidebar.ScoreboardLine(
                                    "line$i",
                                    Component.text(it),
                                    i
                                )
                            )
                        }
                        components[key] = comps

                        updateSidebar(key, lines.toSet())
                    }
                }
            }
        }
    }

    init {
        load()

        val node = EventNode.event("scoreboard_manager", EventFilter.ALL) { event ->
            return@event event is TextAnimationEvent
        }

        node.addListener(TextAnimationEvent::class.java) { event ->
            animatedFrames.forEach { (key, value) ->
                val updated = mutableSetOf<String>()
                event.updated.forEach { h ->
                    if (value.contains(h)) {
                        updated += h
                    }
                }
                if (updated.isNotEmpty()) {
                    updateSidebar(key, updated)
                }
            }
        }

        MinecraftServer.getGlobalEventHandler().addChild(node)
    }

    fun updateSidebar(key: String, updates: Set<String>) {

        val sidebar = sidebars[key] ?: return
        val comps = components[key] ?: return

        val lines = mutableSetOf<Int>()
        updates.forEach { h ->
            comps.forEachIndexed { i, c ->
                if (c.updateable(h)) {
                    c.update()
                    lines.add(i)
                }
            }
        }

        lines.forEach {
            sidebar.updateLineContent("line$it", comps[it].asComponent())
        }
    }

    fun sendScoreboard(player: Player) {
        sidebars.values.firstOrNull()?.addViewer(player)
    }
}