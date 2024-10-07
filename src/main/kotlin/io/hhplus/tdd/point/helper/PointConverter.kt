package io.hhplus.tdd.point.helper

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import org.springframework.stereotype.Component

@Component
class PointConverter : IPointConverter {
    override fun toUserPointResponse(userPoint: UserPoint): UserPointResponse {
        return UserPointResponse(id = userPoint.id, point = userPoint.point)
    }

    override fun toPointHistoryResponse(pointHistory: List<PointHistory>): List<PointHistoryResponse> {
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

    override fun toChargePointResponse(userPoint: UserPoint): ChargePointResponse {
        return ChargePointResponse(id = userPoint.id, point = userPoint.point)
    }

    override fun toUsePointResponse(userPoint: UserPoint): UsePointResponse {
        return UsePointResponse(id = userPoint.id, point = userPoint.point)
    }
}
