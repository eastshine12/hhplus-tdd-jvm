package io.hhplus.tdd.point.helper

import io.hhplus.tdd.point.dto.PointHistoryQueueItem

interface IPointHistoryQueueManager {
    fun put(history: PointHistoryQueueItem)

    fun processQueue()

    fun processPointRequest(history: PointHistoryQueueItem)

    fun isQueueEmpty(): Boolean
}
