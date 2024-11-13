package de.infinityprojects.mcserver.ui

import net.kyori.adventure.text.logger.slf4j.ComponentLogger
import net.minestom.server.MinecraftServer
import net.minestom.server.timer.TaskSchedule
import org.yaml.snakeyaml.Yaml
import java.io.File

class TextAnimationEngine {

    private val animations = hashMapOf<String, TextAnimation>()
    private val updates = mutableSetOf<String>()

    private val logger = ComponentLogger.logger("TextAnimationEngine")

    fun loadAnimations() {
        val yaml = Yaml()
        val file = File("animations.yml")

        val config = yaml.load(file.inputStream()) as Map<String, Any>

        if (config.isEmpty()) {
            return
        }

        for ((key, value) in config) {
            if (value is Map<*, *>) {
                val frames = value["content"] as List<String>
                val time = value["time"] as Int

                animations[key] = TextAnimation(frames.toTypedArray(), time)
            }
        }
    }

    init {
        loadAnimations()

        MinecraftServer.getSchedulerManager().submitTask {
            updates.clear()
            for ((name, animation) in animations) {
                if (animation.tick()) {
                    updates.add(name)
                }
            }
            if (updates.isNotEmpty()) {
                MinecraftServer.getGlobalEventHandler().call(TextAnimationEvent(updates)) // get handle avoids map lookups
            }

            return@submitTask TaskSchedule.tick(1)
        }
    }

    fun getAnimationFrame(name: String): Pair<String, Boolean> {
        val string = animations[name]?.getFrame() ?: ""
        val updated = updates.contains(name)
        return Pair(string, updated)
    }

    class TextAnimation(
        val frames: Array<String>,
        val time: Int,
    ) {
        var tick: Int = 0
        var frame: Int = 0

        fun getFrame(): String {
            return frames[frame]
        }

        fun tick(): Boolean {
            tick++
            if (tick >= time) {
                tick = 0
                frame++
                if (frame >= frames.size) {
                    frame = 0
                }

                return true
            }
            return false
        }
    }
}