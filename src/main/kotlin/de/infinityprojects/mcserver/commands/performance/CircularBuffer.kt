package de.infinityprojects.mcserver.commands.performance

class CircularBuffer(
    size: Int,
) {
    private val buffer: FloatArray = FloatArray(size)
    private var index = 0
    private var sum = 0f
    private var full = false

    fun add(value: Float) {
        if (full) {
            sum -= buffer[index]
        }
        buffer[index] = value
        sum += value
        index = (index + 1) % buffer.size
        if (index == 0) {
            full = true
        }
    }

    fun average(): Float = sum / if (full) buffer.size else index

    fun averageOfLast(last: Int): Float {
        var sum = 0f
        if (last > buffer.size) {
            throw IllegalArgumentException("last must be less than or equal to the buffer size")
        }

        for (i in 0 until last) {
            sum += buffer[(index - i + buffer.size) % buffer.size]
        }
        return sum / last
    }

    fun max(): Float {
        var max = 0f
        for (i in buffer.indices) {
            if (buffer[i] > max) {
                max = buffer[i]
            }
        }
        return max
    }

    fun last(): Float = buffer[(index - 1 + buffer.size) % buffer.size]
}
