package io.hhplus.tdd.point.lock

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.ReentrantLock

@Component
class UserLockManager {
    private val lockMap: ConcurrentHashMap<Long, ReentrantLock> = ConcurrentHashMap()

    private fun getLock(userId: Long): ReentrantLock {
        return lockMap.computeIfAbsent(userId) { ReentrantLock() }
    }

    fun lock(userId: Long) {
        val lock = getLock(userId)
        lock.lock()
    }

    fun unlock(userId: Long) {
        val lock = getLock(userId)
        if (lock.isHeldByCurrentThread) {
            lock.unlock()
        }
    }
}
