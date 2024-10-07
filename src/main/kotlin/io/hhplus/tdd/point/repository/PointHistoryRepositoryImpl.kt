package io.hhplus.tdd.point.repository

import io.hhplus.tdd.database.PointHistoryTable
import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import org.springframework.stereotype.Repository

@Repository
class PointHistoryRepositoryImpl(
    private val pointHistoryTable: PointHistoryTable,
) : PointHistoryRepository {
    override fun insert(
        id: Long,
        amount: Long,
        transactionType: TransactionType,
        updateMillis: Long,
    ): PointHistory {
        return pointHistoryTable.insert(id, amount, transactionType, updateMillis)
    }

    override fun selectAllByUserId(userId: Long): List<PointHistory> {
        return pointHistoryTable.selectAllByUserId(userId)
    }
}
