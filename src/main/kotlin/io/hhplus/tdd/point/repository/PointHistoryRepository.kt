package io.hhplus.tdd.point.repository

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType

interface PointHistoryRepository {
    fun insert(
        id: Long,
        amount: Long,
        transactionType: TransactionType,
        updateMillis: Long,
    ): PointHistory

    fun selectAllByUserId(userId: Long): List<PointHistory>
}
