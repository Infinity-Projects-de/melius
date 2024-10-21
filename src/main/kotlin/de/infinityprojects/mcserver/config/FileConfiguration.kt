package de.infinityprojects.mcserver.config

import java.io.File
import java.io.InputStream

abstract class FileConfiguration(
    val fileName: String,
    private val defaultResourceLocation: String = "/default/$fileName",
    generateDefault: Boolean,
) : Configuration() {
    private fun getDefault(): InputStream =
        javaClass.getResourceAsStream(defaultResourceLocation)
            ?: throw IllegalArgumentException("Resource not found: $defaultResourceLocation")

    init {
        if (generateDefault) {
            saveDefault()
        }
        reload()
        generateMissing()
    }

    fun saveDefault() {
        val file = File(fileName)
        if (file.exists()) {
            return
        }

        getDefault().use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    fun reload() {
        val file = File(fileName)
        val input =
            if (file.exists()) {
                file.inputStream()
            } else {
                getDefault()
            }

        input.use {
            getMap(it).values.forEach(this::set)
        }
    }

    private fun generateMissing() {
        getDefault().use {
            generateMissing(getMissing(this, getMap(it)))
        }
    }

    private fun getMissing(
        base: Configuration,
        other: Configuration,
    ): Configuration {
        val missing = ConfigurationSection()
        base.values.forEach { (key, value) ->
            if (value is ConfigurationSection) {
                val otherSection = other.values[key]
                if (otherSection == null) {
                    missing.values[key] = value
                } else {
                    if (otherSection !is ConfigurationSection) {
                        throw IllegalArgumentException("Section is not a section")
                    }
                    val missingSection = getMissing(value, otherSection)
                    if (missingSection.values.isNotEmpty()) {
                        missing.values[key] = missingSection
                    }
                }
            } else {
                if (!other.values.containsKey(key)) {
                    missing.values[key] = value
                }
            }
        }
        return missing
    }

    abstract fun getMap(input: InputStream): Configuration

    internal abstract fun generateMissing(missing: Configuration)

    abstract fun save()
}
