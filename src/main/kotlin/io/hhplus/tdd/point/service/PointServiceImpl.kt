package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import io.hhplus.tdd.point.helper.IPointConverter
import io.hhplus.tdd.point.helper.IPointValidateHelper
import io.hhplus.tdd.point.lock.IUserLockManager
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.springframework.stereotype.Service

@Service
class PointServiceImpl(
    private val pointHistoryRepository: PointHistoryRepository,
    private val userPointRepository: UserPointRepository,
    private val userLockManager: IUserLockManager,
    private val pointValidateHelper: IPointValidateHelper,
    private val pointConverter: IPointConverter,
) : PointService {
    override fun getUserPoint(userId: Long): UserPointResponse {
        return userLockManager.executeWithLock(userId) {
            val userPoint = userPointRepository.selectById(userId)
            pointConverter.toUserPointResponse(userPoint)
        }
    }

    override fun getPointHistory(userId: Long): List<PointHistoryResponse> {
        return userLockManager.executeWithLock(userId) {
            val pointHistory = pointHistoryRepository.selectAllByUserId(userId)
            pointConverter.toPointHistoryResponse(pointHistory)
        }
    }

    override fun chargePoint(pointRequest: PointRequest): ChargePointResponse {
        return userLockManager.executeWithLock(pointRequest.userId) {
            val userPoint = userPointRepository.selectById(pointRequest.userId)
            pointValidateHelper.validateMaxBalance(userPoint.point, pointRequest.amount)
            val updateUserPoint = userPointRepository.insertOrUpdate(pointRequest.userId, userPoint.point + pointRequest.amount)
            pointHistoryRepository.insert(pointRequest.userId, pointRequest.amount, TransactionType.CHARGE, System.currentTimeMillis())
            pointConverter.toChargePointResponse(updateUserPoint)
        }
    }

    override fun usePoint(pointRequest: PointRequest): UsePointResponse {
        return userLockManager.executeWithLock(pointRequest.userId) {
            val userPoint = userPointRepository.selectById(pointRequest.userId)
            pointValidateHelper.validateMinBalance(userPoint.point, pointRequest.amount)
            val updateUserPoint = userPointRepository.insertOrUpdate(pointRequest.userId, userPoint.point - pointRequest.amount)
            pointHistoryRepository.insert(pointRequest.userId, pointRequest.amount, TransactionType.USE, System.currentTimeMillis())
            pointConverter.toUsePointResponse(updateUserPoint)
        }
    }
}
