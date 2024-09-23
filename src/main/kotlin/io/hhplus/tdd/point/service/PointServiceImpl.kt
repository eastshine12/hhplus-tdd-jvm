package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import io.hhplus.tdd.point.exception.InvalidAmountException
import io.hhplus.tdd.point.lock.UserLockManager
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Service

@Service
class PointServiceImpl(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userPointRepository: UserPointRepository,
    private val userLockManager: UserLockManager,
) : PointService {
    override fun getUserPoint(userId: Long): UserPointResponse {
        val userPoint = userPointRepository.selectById(userId)
        return UserPointResponse(
            id = userPoint.id,
            point = userPoint.point,
        )
    }

    override fun getPointHistory(userId: Long): List<PointHistoryResponse> {
        val pointHistory = pointHistoryRepository.selectAllByUserId(userId)
        return pointHistory.map { history ->
            PointHistoryResponse(
                id = history.id,
                userId = history.userId,
                type = history.type,
                amount = history.amount,
                timeMillis = history.timeMillis,
            )
        }
    }

    override fun chargePoint(pointRequest: PointRequest): ChargePointResponse {
        val userPoint = userPointRepository.selectById(pointRequest.userId)

        userLockManager.lock(userPoint.id)
        try {
            pointHistoryRepository.insert(pointRequest.userId, pointRequest.amount, TransactionType.CHARGE, System.currentTimeMillis())
            val total = userPoint.point + pointRequest.amount
            if (total > UserPoint.MAX_BALANCE) {
                throw InvalidAmountException("최대 잔액을 초과할 수 없습니다. 최대 잔액: ${UserPoint.MAX_BALANCE}")
            }
            val updateUserPoint = userPointRepository.insertOrUpdate(pointRequest.userId, total)
            return ChargePointResponse(
                id = updateUserPoint.id,
                point = updateUserPoint.point,
            )
        } finally {
            userLockManager.unlock(userPoint.id)
        }
    }

    override fun usePoint(pointRequest: PointRequest): UsePointResponse {
        val userPoint = userPointRepository.selectById(pointRequest.userId)

        userLockManager.lock(userPoint.id)
        try {
            pointHistoryRepository.insert(pointRequest.userId, pointRequest.amount, TransactionType.USE, System.currentTimeMillis())
            val total = userPoint.point - pointRequest.amount
            if (total < 0) {
                throw InvalidAmountException("포인트 잔액이 0보다 작을 수 없습니다.")
            }
            val updateUserPoint = userPointRepository.insertOrUpdate(pointRequest.userId, total)
            return UsePointResponse(
                id = updateUserPoint.id,
                point = updateUserPoint.point,
            )
        } finally {
            userLockManager.unlock(userPoint.id)
        }
    }
}
