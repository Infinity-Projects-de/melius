package de.infinityprojects.mcserver.config

class ConfigurationSection(
    defaults: Map<String, Any> = emptyMap(),
) : Configuration() {
    init {
        defaults.forEach { (key, value) ->
            values[key] = value
        }
    }
}
