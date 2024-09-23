package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.domain.TransactionType

data class UserPointResponse(
    val id: Long,
    val point: Long,
)

data class PointHistoryResponse(
    val id: Long,
    val userId: Long,
    val type: TransactionType,
    val amount: Long,
    val timeMillis: Long,
)

data class ChargePointResponse(
    val id: Long,
    val point: Long,
)

data class UsePointResponse(
    val id: Long,
    val point: Long,
)
