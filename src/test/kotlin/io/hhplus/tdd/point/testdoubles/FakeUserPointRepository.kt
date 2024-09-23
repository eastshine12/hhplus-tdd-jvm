package io.hhplus.tdd.point.testdoubles

import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.repository.UserPointRepository

class FakeUserPointRepository : UserPointRepository {
    private val userPoints = mutableMapOf<Long, UserPoint>()

    override fun selectById(id: Long): UserPoint {
        return userPoints[id] ?: UserPoint(id = id, point = 0, updateMillis = System.currentTimeMillis())
    }

    override fun insertOrUpdate(
        id: Long,
        amount: Long,
    ): UserPoint {
        val userPoint = UserPoint(id = id, point = amount, updateMillis = System.currentTimeMillis())
        userPoints[id] = userPoint
        return userPoint
    }
}
