package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.domain.TransactionType

data class PointHistoryQueueItem(
    val userId: Long,
    val amount: Long,
    val type: TransactionType,
)
