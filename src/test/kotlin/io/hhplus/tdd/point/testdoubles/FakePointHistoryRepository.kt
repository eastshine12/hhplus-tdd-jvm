package io.hhplus.tdd.point.testdoubles

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.repository.PointHistoryRepository

class FakePointHistoryRepository : PointHistoryRepository {
    private val pointHistories = mutableListOf<PointHistory>()
    private var cursor: Long = 1L

    override fun insert(
        id: Long,
        amount: Long,
        transactionType: TransactionType,
        updateMillis: Long,
    ): PointHistory {
        val pointHistory =
            PointHistory(
                id = cursor++,
                userId = id,
                amount = amount,
                type = transactionType,
                timeMillis = updateMillis,
            )
        pointHistories.add(pointHistory)
        return pointHistory
    }

    override fun selectAllByUserId(userId: Long): List<PointHistory> {
        return pointHistories.filter { it.userId == userId }
    }
}
