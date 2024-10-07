package io.hhplus.tdd.point.helper

import io.hhplus.tdd.point.dto.PointHistoryQueueItem
import io.hhplus.tdd.point.repository.PointHistoryRepository
import org.springframework.stereotype.Component
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue

@Component
class PointHistoryQueueManager(
    private val pointHistoryRepository: PointHistoryRepository,
) : IPointHistoryQueueManager {
    private val queue: BlockingQueue<PointHistoryQueueItem> = ArrayBlockingQueue(10_000)

    init {
        Thread { processQueue() }.start()
    }

    override fun put(history: PointHistoryQueueItem) {
        queue.put(history)
    }

    override fun processQueue() {
        while (true) {
            try {
                val pointRequest = queue.take()
                processPointRequest(pointRequest)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun processPointRequest(history: PointHistoryQueueItem) {
        pointHistoryRepository.insert(history.userId, history.amount, history.type, System.currentTimeMillis())
    }

    override fun isQueueEmpty(): Boolean {
        return queue.isEmpty()
    }
}
