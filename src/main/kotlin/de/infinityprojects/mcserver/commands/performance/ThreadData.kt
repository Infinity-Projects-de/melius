package de.infinityprojects.mcserver.commands.performance

import java.lang.management.ManagementFactory
import java.lang.management.ThreadInfo

class ThreadData(
    val name: String,
    var cpuTime: Long,
    var userTime: Long,
    var blockedTime: Long,
    var waitedTime: Long,
    var cpuPercentage: Float,
    var userPercentage: Float,
    var blockedPercentage: Float,
    var waitedPercentage: Float,
) {
    var lastQuery = System.currentTimeMillis()

    constructor(info: ThreadInfo) : this(
        info.threadName,
        THREAD_MX_BEAN.getThreadCpuTime(info.threadId),
        THREAD_MX_BEAN.getThreadUserTime(info.threadId),
        info.blockedTime,
        info.waitedTime,
        0f,
        0f,
        0f,
        0f,
    )

    fun update(info: ThreadInfo) {
        update(
            THREAD_MX_BEAN.getThreadCpuTime(info.threadId),
            THREAD_MX_BEAN.getThreadUserTime(info.threadId),
            info.blockedTime,
            info.waitedTime,
        )
    }

    fun update(
        cpuTime: Long,
        userTime: Long,
        blockedTime: Long,
        waitedTime: Long,
    ) {
        val totalCpuTime = (cpuTime - this.cpuTime) / 1000000f
        val totalUserTime = (userTime - this.userTime) / 1000000f
        val totalBlockedTime = blockedTime - this.blockedTime.toFloat()
        val totalWaitedTime = waitedTime - this.waitedTime.toFloat()

        val time = System.currentTimeMillis()
        val timeBetween = time - lastQuery
        lastQuery = time

        cpuPercentage = totalCpuTime / timeBetween * 100L
        userPercentage = totalUserTime / timeBetween * 100L
        blockedPercentage = totalBlockedTime / timeBetween * 100L
        waitedPercentage = totalWaitedTime / timeBetween * 100L

        this.cpuTime = cpuTime
        this.userTime = userTime
        this.blockedTime = blockedTime
        this.waitedTime = waitedTime
    }

    companion object {
        private val THREAD_MX_BEAN = ManagementFactory.getThreadMXBean()

        fun getThreads() = THREAD_MX_BEAN.getThreadInfo(THREAD_MX_BEAN.allThreadIds)
    }
}
