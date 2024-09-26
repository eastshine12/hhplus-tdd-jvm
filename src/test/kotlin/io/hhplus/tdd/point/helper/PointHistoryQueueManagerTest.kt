package io.hhplus.tdd.point.helper

import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.dto.PointHistoryQueueItem
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.testdoubles.FakePointHistoryRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointHistoryQueueManagerTest {
    private lateinit var pointHistoryRepository: PointHistoryRepository
    private lateinit var pointHistoryQueueManager: IPointHistoryQueueManager

    @BeforeEach
    fun setUp() {
        pointHistoryRepository = FakePointHistoryRepository()
        pointHistoryQueueManager = PointHistoryQueueManager(pointHistoryRepository)
    }

    @Test
    fun `put 호출 시 큐에 아이템이 추가되고 insert 처리해야 한다`() {
        // given
        val userId = 1L
        val chargeAmount = 100L
        val pointHistoryItem = PointHistoryQueueItem(userId, chargeAmount, TransactionType.CHARGE)

        // when
        pointHistoryQueueManager.put(pointHistoryItem)
        Thread.sleep(100)

        // then
        val pointHistory = pointHistoryRepository.selectAllByUserId(userId)
        assertEquals(userId, pointHistory[0].userId)
        assertEquals(chargeAmount, pointHistory[0].amount)
        assertEquals(TransactionType.CHARGE, pointHistory[0].type)
    }
}
