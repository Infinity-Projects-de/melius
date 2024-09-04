package de.infinityprojects.mcserver.utils.log

import org.apache.logging.log4j.core.LogEvent
import org.apache.logging.log4j.core.config.plugins.Plugin
import org.apache.logging.log4j.core.pattern.ConverterKeys
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter
import org.apache.logging.log4j.core.pattern.PatternConverter
import java.lang.StringBuilder

@Plugin(name = "ColorRemover", category = PatternConverter.CATEGORY)
@ConverterKeys("clr")
class ColorRemover : LogEventPatternConverter("ColorRemover", null) {
    override fun format(
        event: LogEvent?,
        toAppendTo: StringBuilder?,
    ) {
        if (event == null) return
        val message = event.message.formattedMessage

        val cleanMessage =
            message
                .replace("\u001B\\[[;\\d]*m".toRegex(), "") // remove ansi
                .replace("§[0-9a-fA-F]".toRegex(), "") // remove mc color codes
                .replace("§[l-oL-O]".toRegex(), "") // remove text decoration
                .replace("§x[0-9a-fA-F]{6}".toRegex(), "") // remove rgb mc color codes

        toAppendTo?.append(cleanMessage)
    }

    companion object {
        @JvmStatic
        fun newInstance(options: Array<String?>?): ColorRemover = ColorRemover()
    }
}
