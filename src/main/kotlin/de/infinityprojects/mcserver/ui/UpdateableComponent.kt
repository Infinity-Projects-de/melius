package de.infinityprojects.mcserver.ui

import de.infinityprojects.mcserver.server.MeliusServer
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

class UpdateableComponent(private var text: String) : ComponentLike {
    private var component: Component
    private val split = text.split("%")
    val animationHolders: Set<String> = split.filterIndexed { index, _ -> index % 2 != 0 }.toSet()

    fun updateable(holder: String): Boolean {
        return animationHolders.contains(holder)
    }

    init {
        component = Component.text(text)
    }

    fun update() {
        var newText = ""
        for (i in 0 until split.size) {
            if (i % 2 == 0) {
                newText += split[i]
            } else {
                val updateFrame = MeliusServer.textAnimationEngine.getAnimationFrame(split[i])
                newText += updateFrame.first
            }
        }

        val parsedAmpersand = LegacyComponentSerializer.legacyAmpersand().deserialize(newText)
        val unparsed = LegacyComponentSerializer.legacySection().serialize(parsedAmpersand)

        component = LegacyComponentSerializer.legacySection().deserialize(unparsed)
    }

    override fun asComponent(): Component {
        return component
    }

}