//package io.mailsmr.domain
//
//import com.google.common.primitives.Ints
//import com.google.common.util.concurrent.SettableFuture
//import org.joda.time.DateTime
//import org.joda.time.DateTimeUtils
//import org.joda.time.Duration
//import org.joda.time.MutableDateTime
//import java.util.*
//import java.util.concurrent.*
//import java.util.concurrent.atomic.AtomicBoolean
//
///**
// * Fake implementation of [ScheduledExecutorService] that allows tests control the reference
// * time of the executor and decide when to execute any outstanding task.
// */
//class FakeScheduledExecutorService : AbstractExecutorService(), ScheduledExecutorService {
//    private val shutdown = AtomicBoolean(false)
//    private val pendingCallables = PriorityQueue<PendingCallable<*>>()
//    private val currentTime = MutableDateTime.now()
//    override fun schedule(command: Runnable, delay: Long, unit: TimeUnit): ScheduledFuture<*> {
//        return schedulePendingCallable(
//            PendingCallable<Any>(
//                Duration(unit.toMillis(delay)), command, PendingCallableType.NORMAL
//            )
//        )
//    }
//
//    override fun <V> schedule(callable: Callable<V>, delay: Long, unit: TimeUnit): ScheduledFuture<V> {
//        return schedulePendingCallable(
//            PendingCallable(
//                Duration(unit.toMillis(delay)), callable, PendingCallableType.NORMAL
//            )
//        )
//    }
//
//    override fun scheduleAtFixedRate(
//        command: Runnable, initialDelay: Long, period: Long, unit: TimeUnit
//    ): ScheduledFuture<*> {
//        return schedulePendingCallable(
//            PendingCallable<Any>(
//                Duration(unit.toMillis(initialDelay)), command, PendingCallableType.FIXED_RATE
//            )
//        )
//    }
//
//    override fun scheduleWithFixedDelay(
//        command: Runnable, initialDelay: Long, delay: Long, unit: TimeUnit
//    ): ScheduledFuture<*> {
//        return schedulePendingCallable(
//            PendingCallable<Any>(
//                Duration(unit.toMillis(initialDelay)), command, PendingCallableType.FIXED_DELAY
//            )
//        )
//    }
//
//    fun tick(time: Long, unit: TimeUnit) {
//        advanceTime(Duration.millis(unit.toMillis(time)))
//    }
//
//    /**
//     * This will advance the reference time of the executor and execute (in the same thread) any
//     * outstanding callable which execution time has passed.
//     */
//    fun advanceTime(toAdvance: Duration?) {
//        currentTime.add(toAdvance)
//        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)
//        synchronized(pendingCallables) {
//            while (!pendingCallables.isEmpty()
//                && (pendingCallables.peek()).scheduledTime <= currentTime
//            ) {
//                try {
//                    pendingCallables.poll().call()
//                    if (shutdown.get() && pendingCallables.isEmpty()) {
//                        pendingCallables.notifyAll()
//                    }
//                } catch (e: Exception) {
//                    // We ignore any callable exception, which should be set to the future but not relevant to
//                    // advanceTime.
//                }
//            }
//        }
//    }
//
//    override fun shutdown() {
//        check(!shutdown.getAndSet(true)) { "This executor has been shutdown already" }
//    }
//
//    override fun shutdownNow(): List<Runnable> {
//        check(!shutdown.getAndSet(true)) { "This executor has been shutdown already" }
//        val pending: MutableList<Runnable> = ArrayList()
//        for (pendingCallable in pendingCallables) {
//            pending.add(
//                Runnable { pendingCallable.call() })
//        }
//        synchronized(pendingCallables) {
//            pendingCallables.notifyAll()
//            pendingCallables.clear()
//        }
//        return pending
//    }
//
//    override fun isShutdown(): Boolean {
//        return shutdown.get()
//    }
//
//    override fun isTerminated(): Boolean {
//        return pendingCallables.isEmpty()
//    }
//
//    @Throws(InterruptedException::class)
//    override fun awaitTermination(timeout: Long, unit: TimeUnit): Boolean {
//        synchronized(pendingCallables) {
//            if (pendingCallables.isEmpty()) {
//                return true
//            }
//            pendingCallables.wait(unit.toMillis(timeout))
//            return pendingCallables.isEmpty()
//        }
//    }
//
//    override fun execute(command: Runnable) {
//        check(!shutdown.get()) { "This executor has been shutdown" }
//        command.run()
//    }
//
//    internal fun <V> schedulePendingCallable(callable: PendingCallable<V>): ScheduledFuture<V> {
//        check(!shutdown.get()) { "This executor has been shutdown" }
//        synchronized(pendingCallables) { pendingCallables.add(callable) }
//        return callable.scheduledFuture
//    }
//
//    internal enum class PendingCallableType {
//        NORMAL, FIXED_RATE, FIXED_DELAY
//    }
//
//    /**
//     * Class that saves the state of an scheduled pending callable.
//     */
//    internal inner class PendingCallable<T> : Comparable<PendingCallable<T>> {
//        var creationTime = currentTime.toDateTime()
//        var delay: Duration
//        var pendingCallable: Callable<T>
//        var future: SettableFuture<T> = SettableFuture.create()
//        var cancelled = AtomicBoolean(false)
//        var done = AtomicBoolean(false)
//        var type: PendingCallableType
//
//        constructor(delay: Duration, runnable: Runnable, type: PendingCallableType) {
//            pendingCallable = Callable {
//                runnable.run()
//                null
//            }
//            this.type = type
//            this.delay = delay
//        }
//
//        constructor(delay: Duration, callable: Callable<T>, type: PendingCallableType) {
//            pendingCallable = callable
//            this.type = type
//            this.delay = delay
//        }
//
//        internal val scheduledTime: DateTime
//            get() = creationTime.plus(delay)
//
//        val scheduledFuture: ScheduledFuture<T>
//            get() = object : ScheduledFuture<T> {
//                override fun getDelay(unit: TimeUnit): Long {
//                    return unit.convert(
//                        Duration(currentTime, scheduledTime).millis, TimeUnit.MILLISECONDS
//                    )
//                }
//
//                override fun compareTo(o: Delayed): Int {
//                    return Ints.saturatedCast(
//                        getDelay(TimeUnit.MILLISECONDS) - o.getDelay(TimeUnit.MILLISECONDS)
//                    )
//                }
//
//                override fun cancel(mayInterruptIfRunning: Boolean): Boolean {
//                    synchronized(this) {
//                        cancelled.set(true)
//                        return !done.get()
//                    }
//                }
//
//                override fun isCancelled(): Boolean {
//                    return cancelled.get()
//                }
//
//                override fun isDone(): Boolean {
//                    return done.get()
//                }
//
//                @Throws(InterruptedException::class, ExecutionException::class)
//                override fun get(): T {
//                    return future.get()
//                }
//
//                @Throws(InterruptedException::class, ExecutionException::class, TimeoutException::class)
//                override fun get(timeout: Long, unit: TimeUnit): T {
//                    return future[timeout, unit]
//                }
//            }
//
//        fun call(): T? {
//            var result: T? = null
//            synchronized(this) {
//                if (cancelled.get()) {
//                    return null
//                }
//                try {
//                    result = pendingCallable.call()
//                    future.set(result)
//                } catch (e: Exception) {
//                    future.setException(e)
//                } finally {
//                    when (type) {
//                        PendingCallableType.NORMAL -> done.set(true)
//                        PendingCallableType.FIXED_DELAY -> {
//                            creationTime = currentTime.toDateTime()
//                            schedulePendingCallable(this)
//                        }
//                        PendingCallableType.FIXED_RATE -> {
//                            creationTime = creationTime.plus(delay)
//                            schedulePendingCallable(this)
//                        }
//                        else -> {
//                        }
//                    }
//                }
//            }
//            return result
//        }
//
//        override fun compareTo(other: PendingCallable<T>): Int {
//            return scheduledTime.compareTo(other.scheduledTime)
//        }
//    }
//
//    init {
//        DateTimeUtils.setCurrentMillisFixed(currentTime.millis)
//    }
//}
