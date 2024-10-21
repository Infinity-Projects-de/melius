package de.infinityprojects.mcserver.config

abstract class Configuration {
    internal val values = mutableMapOf<String, Any>()

    fun getBoolean(
        key: String,
        default: Boolean = false,
    ): Boolean = get(key, default)

    fun getInt(
        key: String,
        default: Int = 0,
    ): Int = get(key, default)

    fun getString(
        key: String,
        default: String = "",
    ): String = get(key, default)

    fun getConfigurationSection(key: String): ConfigurationSection = get(key, ConfigurationSection())

    fun <T> get(
        key: String,
        default: T? = null,
    ): T {
        val keys = key.split(".")
        val value = values[keys[0]]
        if (keys.size > 1) {
            return if (value is ConfigurationSection) {
                value.get(keys.drop(1).joinToString("."), default)
            } else {
                throw IllegalArgumentException("Key $key is not a section")
            }
        }
        return if (value != null) {
            @Suppress("UNCHECKED_CAST")
            value as T
        } else {
            default ?: throw IllegalArgumentException("Key $key not found")
        }
    }

    fun setString(
        key: String,
        value: String,
    ) = set(key, value)

    fun setInt(
        key: String,
        value: Int,
    ) = set(key, value)

    fun setBoolean(
        key: String,
        value: Boolean,
    ) = set(key, value)

    fun contains(key: String): Boolean = values.containsKey(key)

    fun set(
        key: String,
        value: Any,
    ) {
        val actualValue =
            if (value is String) {
                when {
                    value.toIntOrNull() != null -> value.toInt()
                    value.toBooleanStrictOrNull() != null -> value.toBoolean()
                    else -> value
                }
            } else {
                value
            }

        values[key] = actualValue
    }
}
