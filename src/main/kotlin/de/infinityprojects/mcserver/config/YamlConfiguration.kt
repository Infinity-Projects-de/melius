package de.infinityprojects.mcserver.config

import de.infinityprojects.mcserver.utils.MAX_YAML_INDENT
import java.io.File
import java.io.InputStream

class YamlConfiguration(
    filePath: String,
    defaultResourceLocation: String = "/default/$filePath",
    generateDefault: Boolean = true,
) : FileConfiguration(
        filePath,
        defaultResourceLocation,
        generateDefault,
    ) {
    private var spacesForLevelChange = 0

    override fun getMap(input: InputStream): Configuration {
        var spacesBefore = 0
        var level = 0
        val sections = arrayOfNulls<String>(MAX_YAML_INDENT)
        val root = ConfigurationSection()
        input.bufferedReader().lines().forEach { line ->
            if (line.startsWith("#") || line.isBlank()) {
                return@forEach
            }

            val split = line.split(":")
            val name = split[0]

            val spaces = line.takeWhile { it == ' ' }.length
            if (spacesForLevelChange == 0) {
                spacesForLevelChange = spaces
            }

            if (spacesForLevelChange != 0) {
                val spaceDiff = (spaces - spacesBefore)
                spacesBefore = spaces
                if (spaceDiff % spacesForLevelChange != 0) {
                    throw IllegalArgumentException("Invalid indentation")
                }
                val levels = spaceDiff / spacesForLevelChange
                level += levels

                sections[level] = name
                sections.dropLast(MAX_YAML_INDENT - level)
            }

            if (split.size == 2 && split[1].isNotBlank()) {
                val value = split[1].trim()
                sections[level] = null
                val section = computeSection(root, sections.filterNotNull(), 0)
                section.values[name] = value
            }
        }

        return root
    }

    fun computeSection(
        root: Configuration,
        keys: List<String>,
        index: Int,
    ): Configuration {
        if (index >= keys.size - 1) {
            return root
        }

        val key = keys[index]
        var section = root.values[key]
        if (section == null) {
            section = ConfigurationSection()
            root.values[key] = section
        }
        if (section !is ConfigurationSection) {
            throw IllegalArgumentException("Section is not a section")
        }

        return computeSection(section, keys, index + 1)
    }

    override fun generateMissing(missing: Configuration) {
        val file = File(fileName)
        val values = missing.values
        if (values.isNotEmpty()) {
            file.appendText("\n# Generated automatically\n")
            values.forEach { (key, value) ->
                if (value is ConfigurationSection) {
                    file.appendText("$key:\n")
                    file.appendText(generateWrittable(value, 1))
                } else {
                    file.appendText("$key: $value\n")
                }
                set(key, value)
            }
        }
    }

    private fun generateWrittable(
        configuration: Configuration,
        level: Int,
    ): String {
        val builder = StringBuilder()
        configuration.values.forEach { (key, value) ->
            builder.append(" ".repeat(level * spacesForLevelChange))
            if (value is ConfigurationSection) {
                builder.append("$key:\n")
                builder.append(generateWrittable(value, level + 1))
            } else {
                builder.append("$key: $value\n")
            }
        }
        return builder.toString()
    }

    override fun save() {
        TODO("Not yet implemented")
    }
}
