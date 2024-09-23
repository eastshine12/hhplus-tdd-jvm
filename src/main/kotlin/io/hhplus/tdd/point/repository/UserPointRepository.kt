package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.UserPoint

interface UserPointRepository {
    fun selectById(id: Long): UserPoint

    fun insertOrUpdate(
        id: Long,
        amount: Long,
    ): UserPoint
}
