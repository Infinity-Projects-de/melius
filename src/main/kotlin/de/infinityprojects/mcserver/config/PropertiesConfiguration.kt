package de.infinityprojects.mcserver.config

import java.io.File

class PropertiesConfiguration(
    val fileName: String = "server.properties",
) {
    val values = mutableMapOf<String, String>()

    init {
        require(fileName.isNotBlank()) { "fileName cannot be blank" }
        require(fileName.endsWith(".properties")) { "fileName must end with .properties" }
    }

    fun load() {
        val file = File(fileName)
        val input =
            if (file.exists()) {
                file.inputStream()
            } else {
                this.javaClass.getResourceAsStream("/$fileName")
            }

        input.use { inputStream ->
            inputStream.bufferedReader().use { reader ->
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
        }
    }

    fun generateMissing() {
        val file = File(fileName)
        if (!file.exists()) {
            throw IllegalArgumentException("File $fileName does not exist")
        }

        val notExisting = mutableMapOf<String, String>()

        this.javaClass.getResourceAsStream("/$fileName").use { inputStream ->
            if (inputStream == null) {
                throw IllegalArgumentException("Resource not found: $fileName")
            }

            inputStream.bufferedReader().readLines().forEach { line ->
                if (!line.startsWith("#") && line.isNotBlank()) {
                    val split = line.split("=")
                    val key = split[0].trim()
                    if (!values.containsKey(key)) {
                        val default = split[1].split("#")[0].trim()
                        values[key] = default
                        notExisting[key] = default
                    }
                }
            }
        }

        if (notExisting.isNotEmpty()) {
            file.appendText("\n")
            notExisting.forEach { (key, value) ->
                file.appendText("$key=$value\n")
            }
        }
    }

    fun getString(key: String): String = values[key] ?: throw IllegalArgumentException("Key $key not found")

    fun getInt(key: String): Int = getString(key).toIntOrNull() ?: throw IllegalArgumentException("Key $key is not an integer")

    fun getBoolean(key: String): Boolean =
        getString(key).toBooleanStrictOrNull() ?: throw IllegalArgumentException("Key $key is not a boolean")

    fun saveDefault() {
        this.javaClass.getResourceAsStream("/$fileName").use { inputStream ->
            if (inputStream == null) {
                throw IllegalArgumentException("Resource not found: $fileName")
            }

            val outputFile = File(fileName)
            if (!outputFile.exists()) {
                outputFile.createNewFile()
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }
}
