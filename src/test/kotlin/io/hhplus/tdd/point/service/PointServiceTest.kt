package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.domain.UserPoint
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.exception.InvalidAmountException
import io.hhplus.tdd.point.lock.UserLockManager
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import io.hhplus.tdd.point.testdoubles.FakePointHistoryRepository
import io.hhplus.tdd.point.testdoubles.FakeUserPointRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PointServiceTest {
    private lateinit var pointHistoryRepository: PointHistoryRepository
    private lateinit var userPointRepository: UserPointRepository
    private lateinit var pointService: PointService
    private lateinit var userLockManager: UserLockManager

    @BeforeEach
    fun setup() {
        pointHistoryRepository = FakePointHistoryRepository()
        userPointRepository = FakeUserPointRepository()
        userLockManager = UserLockManager()
        pointService = PointServiceImpl(pointHistoryRepository, userPointRepository, userLockManager)
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
        val expectedHistory = mutableListOf<PointHistoryResponse>()
        val insert1 = pointHistoryRepository.insert(userId, 500L, TransactionType.CHARGE, now)
        val insert2 = pointHistoryRepository.insert(userId, 300L, TransactionType.USE, now)
        expectedHistory.add(
            PointHistoryResponse(
                id = insert1.id,
                userId = insert1.userId,
                type = insert1.type,
                amount = insert1.amount,
                timeMillis = insert1.timeMillis,
            ),
        )
        expectedHistory.add(
            PointHistoryResponse(
                id = insert2.id,
                userId = insert2.userId,
                type = insert2.type,
                amount = insert2.amount,
                timeMillis = insert2.timeMillis,
            ),
        )

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
        val actualUserPoint = pointService.chargePoint(PointRequest(userId, chargeAmount))

        // then
        assertEquals(userId, actualUserPoint.id)
        assertEquals(existPoint + chargeAmount, actualUserPoint.point)

        val history = pointHistoryRepository.selectAllByUserId(actualUserPoint.id)
        assertEquals(2, history.size)
        assertEquals(chargeAmount, history[1].amount)
        assertEquals(TransactionType.CHARGE, history[1].type)
    }

    @Test
    fun `포인트 충전 시 최대 금액을 초과하면 예외를 발생한다`() {
        // given
        val userId = 1L
        val existPoint = UserPoint.MAX_BALANCE - 100L
        val chargeAmount = 101L
        userPointRepository.insertOrUpdate(userId, existPoint)

        // when & then
        assertThrows<InvalidAmountException> {
            pointService.chargePoint(PointRequest(userId, chargeAmount))
        }.apply {
            assertEquals("최대 잔액을 초과할 수 없습니다. 최대 잔액: ${UserPoint.MAX_BALANCE}", message)
        }
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
        val actualUserPoint = pointService.usePoint(PointRequest(userId, useAmount))

        // then
        assertEquals(userId, actualUserPoint.id)
        assertEquals(existPoint - useAmount, actualUserPoint.point)

        val history = pointHistoryRepository.selectAllByUserId(actualUserPoint.id)
        assertEquals(2, history.size)
        assertEquals(useAmount, history[1].amount)
        assertEquals(TransactionType.USE, history[1].type)
    }

    @Test
    fun `포인트 사용 시 0원 미만이 남게되면 예외를 발생한다`() {
        // given
        val userId = 1L
        val existPoint = 100L
        val useAmount = 101L
        userPointRepository.insertOrUpdate(userId, existPoint)

        // when & then
        assertThrows<InvalidAmountException> {
            pointService.usePoint(PointRequest(userId, useAmount))
        }.apply {
            assertEquals("포인트 잔액이 0보다 작을 수 없습니다.", message)
        }
    }

    @Test
    fun `한 유저에게 동시에 포인트 충전과 사용을 할 때 남은 포인트가 정상적으로 반영되어야 한다`() {
        // given
        val userId = 1L
        val initialPoint = 1000L
        userPointRepository.insertOrUpdate(userId, initialPoint)

        val chargeAmount = 101L
        val useAmount = 100L
        val threads = mutableListOf<Thread>()

        threads.add(Thread { pointService.chargePoint(PointRequest(userId, chargeAmount)) })
        threads.add(Thread { pointService.usePoint(PointRequest(userId, useAmount)) })

        // when
        threads.forEach(Thread::start)
        threads.forEach(Thread::join)

        // then
        val finalUserPoint = userPointRepository.selectById(userId)
        val expectedPoint = initialPoint + chargeAmount - useAmount
        assertEquals(expectedPoint, finalUserPoint.point)
    }
}
