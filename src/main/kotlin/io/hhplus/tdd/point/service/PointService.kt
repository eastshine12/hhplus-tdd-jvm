package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse

interface PointService {
    fun getUserPoint(userId: Long): UserPointResponse

    fun getPointHistory(userId: Long): List<PointHistoryResponse>

    fun chargePoint(pointRequest: PointRequest): ChargePointResponse

    fun usePoint(pointRequest: PointRequest): UsePointResponse
}
