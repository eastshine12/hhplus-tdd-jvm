package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Service

@Service
class PointServiceImpl(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userPointRepository: UserPointRepository,
) : PointService {
    override fun getUserPoint(userId: Long): UserPoint {
        return userPointRepository.selectById(userId)
    }

    override fun getPointHistory(userId: Long): List<PointHistory> {
        return pointHistoryRepository.selectAllByUserId(userId)
    }

    override fun chargePoint(
        userId: Long,
        amount: Long,
    ): UserPoint {
        val userPoint = userPointRepository.selectById(userId)
        pointHistoryRepository.insert(userId, amount, TransactionType.CHARGE, System.currentTimeMillis())
        return userPointRepository.insertOrUpdate(userId, userPoint.point + amount)
    }

    override fun usePoint(
        userId: Long,
        amount: Long,
    ): UserPoint {
        val userPoint = userPointRepository.selectById(userId)
        pointHistoryRepository.insert(userId, amount, TransactionType.USE, System.currentTimeMillis())
        return userPointRepository.insertOrUpdate(userId, userPoint.point - amount)
    }
}
