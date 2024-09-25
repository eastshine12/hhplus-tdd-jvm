package io.hhplus.tdd.point.testdoubles

import io.hhplus.tdd.point.exception.LockAcquisitionException
import io.hhplus.tdd.point.lock.IUserLockManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

class FakeUserLockManager : IUserLockManager {
    private val lockMap: ConcurrentHashMap<Long, ReentrantLock> = ConcurrentHashMap()

    override fun <T> executeWithLock(
        userId: Long,
        action: () -> T,
    ): T {
        val lock = lockMap.computeIfAbsent(userId) { ReentrantLock() }

        if (!lock.tryLock(10, TimeUnit.SECONDS)) {
            throw LockAcquisitionException("락 획득에 실패했습니다. userId: $userId")
        }

        return try {
            action()
        } finally {
            lock.unlock()
        }
    }
}
