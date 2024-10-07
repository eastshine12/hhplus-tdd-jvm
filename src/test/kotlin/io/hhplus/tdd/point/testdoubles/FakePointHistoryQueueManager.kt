package io.hhplus.tdd.point.testdoubles

import io.hhplus.tdd.point.dto.PointHistoryQueueItem
import io.hhplus.tdd.point.helper.IPointHistoryQueueManager
import io.hhplus.tdd.point.repository.PointHistoryRepository

class FakePointHistoryQueueManager(
    private val pointHistoryRepository: PointHistoryRepository,
) : IPointHistoryQueueManager {
    override fun put(history: PointHistoryQueueItem) {
        pointHistoryRepository.insert(history.userId, history.amount, history.type, System.currentTimeMillis())
    }

    override fun processQueue() {
        return
    }

    override fun processPointRequest(history: PointHistoryQueueItem) {
        return
    }

    override fun isQueueEmpty(): Boolean {
        return true
    }
}
