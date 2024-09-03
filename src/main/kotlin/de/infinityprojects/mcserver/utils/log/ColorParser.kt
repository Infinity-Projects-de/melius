package de.infinityprojects.mcserver.utils.log

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.ConverterKeys
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter
import org.apache.logging.log4j.core.pattern.PatternConverter
import java.lang.StringBuilder

@Plugin(name = "ColorParser", category = PatternConverter.CATEGORY)
@ConverterKeys("clp")
class ColorParser : LogEventPatternConverter("ColorParser", null) {
    override fun format(
        event: LogEvent?,
        toAppendTo: StringBuilder?,
    ) {
        if (event == null) return
        val message = event.message.formattedMessage

        // convert mc color code to ansi TODO: make it more efficient
        val cleanMessage =
            message
                .replace("§0", "\u001B[30m")
                .replace("§1", "\u001B[34m")
                .replace("§2", "\u001B[32m")
                .replace("§3", "\u001B[36m")
                .replace("§4", "\u001B[31m")
                .replace("§5", "\u001B[35m")
                .replace("§6", "\u001B[33m")
                .replace("§7", "\u001B[37m")
                .replace("§8", "\u001B[90m")
                .replace("§9", "\u001B[94m")
                .replace("§a", "\u001B[92m")
                .replace("§b", "\u001B[96m")
                .replace("§c", "\u001B[91m")
                .replace("§d", "\u001B[95m")
                .replace("§e", "\u001B[93m")
                .replace("§f", "\u001B[97m")
                .replace("§l", "\u001B[1m")
                .replace("§m", "\u001B[9m")
                .replace("§n", "\u001B[4m")
                .replace("§o", "\u001B[3m")
                .replace("§r", "\u001B[0m")

        // TODO: convert rgb mc color code to ansi

        toAppendTo?.append(cleanMessage)
    }

    companion object {
        @JvmStatic
        fun newInstance(options: Array<String?>?): ColorParser = ColorParser()
    }
}
