package io.hhplus.tdd.point.helper

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse

interface IPointConverter {
    fun toUserPointResponse(userPoint: UserPoint): UserPointResponse

    fun toPointHistoryResponse(pointHistory: List<PointHistory>): List<PointHistoryResponse>

    fun toChargePointResponse(userPoint: UserPoint): ChargePointResponse

    fun toUsePointResponse(userPoint: UserPoint): UsePointResponse
}
