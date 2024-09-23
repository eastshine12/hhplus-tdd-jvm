package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Service

@Service
class PointServiceImpl(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userPointRepository: UserPointRepository,
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
        pointHistoryRepository.insert(pointRequest.userId, pointRequest.amount, TransactionType.CHARGE, System.currentTimeMillis())
        val updateUserPoint = userPointRepository.insertOrUpdate(pointRequest.userId, userPoint.point + pointRequest.amount)
        return ChargePointResponse(
            id = updateUserPoint.id,
            point = updateUserPoint.point,
        )
    }

    override fun usePoint(pointRequest: PointRequest): UsePointResponse {
        val userPoint = userPointRepository.selectById(pointRequest.userId)
        pointHistoryRepository.insert(pointRequest.userId, pointRequest.amount, TransactionType.USE, System.currentTimeMillis())
        val updateUserPoint = userPointRepository.insertOrUpdate(pointRequest.userId, userPoint.point - pointRequest.amount)
        return UsePointResponse(
            id = updateUserPoint.id,
            point = updateUserPoint.point,
        )
    }
}
