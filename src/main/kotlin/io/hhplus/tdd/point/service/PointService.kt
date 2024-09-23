package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint

interface PointService {
    fun getUserPoint(userId: Long): UserPoint

    fun getPointHistory(userId: Long): List<PointHistory>

    fun chargePoint(
        userId: Long,
        amount: Long,
    ): UserPoint

    fun usePoint(
        userId: Long,
        amount: Long,
    ): UserPoint
}
