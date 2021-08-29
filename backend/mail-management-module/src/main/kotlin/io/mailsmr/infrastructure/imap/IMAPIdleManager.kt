package io.mailsmr.infrastructure.imap

import com.sun.mail.imap.IMAPFolder
import com.sun.mail.imap.IdleManager
import jakarta.mail.MessagingException
import jakarta.mail.Session
import java.time.Duration
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

class IMAPIdleManager(
    nativeImapSession: Session,
    private val scheduledExecutorService: ScheduledExecutorService,
    private val checkFrequencyIfIdleNotSupported: Duration
) {

    private val idleManager = IdleManager(nativeImapSession, scheduledExecutorService)
    private var die = false

    companion object {
        const val INITIAL_CHECK_IF_IDLE_NOT_SUPPORTED_IN_MS = 0L
    }

    private val fallbackWatchingScheduleMap: MutableMap<IMAPFolder, ScheduledFuture<*>> = HashMap()

    fun watch(folder: IMAPFolder) {
        if (die) throw MessagingException("IMAPIdleManger not running")

        if (fallbackWatchingScheduleMap.containsKey(folder)) return

        try {
            idleManager.watch(folder)
        } catch (e: Exception) {
            watchWithoutIdleFallback(folder)
        }
    }

    fun unwatch(folder: IMAPFolder) {
        if (fallbackWatchingScheduleMap.contains(folder)) {
            fallbackWatchingScheduleMap.remove(folder)!!.cancel(true)
        }
    }

    fun stop() {
        idleManager.stop()
        die = true
    }

    fun isRunning(): Boolean {
        return idleManager.isRunning && !die
    }

    private fun watchWithoutIdleFallback(folder: IMAPFolder) {
        fallbackWatchingScheduleMap[folder] = scheduledExecutorService.scheduleAtFixedRate(
            { triggerServerUpdate(folder) },
            INITIAL_CHECK_IF_IDLE_NOT_SUPPORTED_IN_MS,
            checkFrequencyIfIdleNotSupported.toMillis(),
            TimeUnit.MILLISECONDS
        )
    }

    private fun triggerServerUpdate(folder: IMAPFolder) {
        folder.messageCount
    }


}
