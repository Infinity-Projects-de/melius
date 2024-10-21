package de.infinityprojects.mcserver.config

import java.io.File
import java.io.InputStream
import java.util.Properties

class PropertiesConfiguration(
    fileName: String = "server.properties",
    defaultResourceLocation: String = "/default/$fileName",
    generateDefault: Boolean = true,
) : FileConfiguration(fileName, defaultResourceLocation, generateDefault) {
    init {
        require(fileName.isNotBlank()) { "fileName cannot be blank" }
        require(fileName.endsWith(".properties")) { "fileName must end with .properties" }
    }

    override fun save(): Unit = throw UnsupportedOperationException("Not implemented")

    override fun generateMissing(missing: Configuration) {
        val file = File(fileName)
        val values = missing.values
        if (values.isNotEmpty()) {
            file.appendText("\n# Generated automatically\n")
            values.forEach { (key, value) ->
                set(key, value)
                file.appendText("$key=$value\n")
            }
        }
    }

    override fun getMap(input: InputStream): Configuration {
        val values = mutableMapOf<String, Any>()
        input.bufferedReader().use { reader ->
            reader.readLines().forEach { line ->
                if (line.startsWith("#") || line.isBlank()) {
                    return@forEach
                }

                val parts = line.split("=")
                if (parts.size != 2) {
                    return@forEach
                }

                val key = parts[0].trim()
                val value = parts[1].split("#")[0].trim()
                values[key] = value
            }
        }
        return ConfigurationSection(values)
    }

    fun getProperties(): Properties {
        val properties = Properties()
        values.forEach { (key, value) ->
            properties[key] = value
        }
        return properties
    }
}
