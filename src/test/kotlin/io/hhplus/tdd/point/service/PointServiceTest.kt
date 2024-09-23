package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.PointHistory
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import io.hhplus.tdd.point.testdoubles.FakePointHistoryRepository
import io.hhplus.tdd.point.testdoubles.FakeUserPointRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PointServiceTest {
    private lateinit var pointHistoryRepository: PointHistoryRepository
    private lateinit var userPointRepository: UserPointRepository
    private lateinit var pointService: PointService

    @BeforeEach
    fun setup() {
        pointHistoryRepository = FakePointHistoryRepository()
        userPointRepository = FakeUserPointRepository()
        pointService = PointServiceImpl(pointHistoryRepository, userPointRepository)
    }

    @Test
    fun `포인트를 조회하면 해당 유저의 포인트를 반환해야 한다`() {
        // given
        val userId = 1L
        val point = 1000L
        val expectedUserPoint = userPointRepository.insertOrUpdate(userId, point)

        // when
        val actualUserPoint = pointService.getUserPoint(userId)

        // then
        assertEquals(expectedUserPoint.id, actualUserPoint.id)
        assertEquals(expectedUserPoint.point, actualUserPoint.point)
    }

    @Test
    fun `포인트 내역을 조회하면 해당 유저의 포인트 충전, 사용 내역을 반환해야 한다`() {
        // given
        val userId = 1L
        val now = System.currentTimeMillis()
        val expectedHistory = mutableListOf<PointHistory>()
        expectedHistory.add(pointHistoryRepository.insert(userId, 500L, TransactionType.CHARGE, now))
        expectedHistory.add(pointHistoryRepository.insert(userId, 300L, TransactionType.USE, now))

        // when
        val actualHistory = pointService.getPointHistory(userId)

        // then
        assertEquals(expectedHistory, actualHistory)
    }

    @Test
    fun `포인트를 충전 후에 잔액과 충전내역이 정상적으로 반영되어야 한다`() {
        // given
        val userId = 1L
        val existPoint = 1000L
        val chargeAmount = 500L
        val now = System.currentTimeMillis()

        userPointRepository.insertOrUpdate(userId, existPoint)
        pointHistoryRepository.insert(userId, existPoint, TransactionType.CHARGE, now)

        // when
        val actualUserPoint = pointService.chargePoint(userId, chargeAmount)

        // then
        assertEquals(userId, actualUserPoint.id)
        assertEquals(existPoint + chargeAmount, actualUserPoint.point)

        val history = pointHistoryRepository.selectAllByUserId(actualUserPoint.id)
        assertEquals(2, history.size)
        assertEquals(chargeAmount, history[1].amount)
        assertEquals(TransactionType.CHARGE, history[1].type)
    }

    @Test
    fun `포인트를 사용 후에 잔액과 사용내역이 정상적으로 반영되어야 한다`() {
        // given
        val userId = 1L
        val existPoint = 1000L
        val useAmount = 500L
        val now = System.currentTimeMillis()

        userPointRepository.insertOrUpdate(userId, existPoint)
        pointHistoryRepository.insert(userId, existPoint, TransactionType.CHARGE, now)

        // when
        val actualUserPoint = pointService.usePoint(userId, useAmount)

        // then
        assertEquals(userId, actualUserPoint.id)
        assertEquals(existPoint - useAmount, actualUserPoint.point)

        val history = pointHistoryRepository.selectAllByUserId(actualUserPoint.id)
        assertEquals(2, history.size)
        assertEquals(useAmount, history[1].amount)
        assertEquals(TransactionType.USE, history[1].type)
    }
}
