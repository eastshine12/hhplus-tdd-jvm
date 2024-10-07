package io.hhplus.tdd.point.lock

interface IUserLockManager {
    fun <T> executeWithLock(
        userId: Long,
        action: () -> T,
    ): T
}
