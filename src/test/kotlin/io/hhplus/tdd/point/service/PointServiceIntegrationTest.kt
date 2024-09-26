package io.hhplus.tdd.point.service

import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.repository.PointHistoryRepository
import io.hhplus.tdd.point.repository.UserPointRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class PointServiceIntegrationTest
    @Autowired
    constructor(
        val pointService: PointService,
        val userPointRepository: UserPointRepository,
        val pointHistoryRepository: PointHistoryRepository,
    ) {
        @Nested
        @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
        @DisplayName("한명의 유저에게 포인트 충전과 사용을 동시에 할 때")
        inner class OneUserPointTest {
            private val threadPools = Executors.newFixedThreadPool(1000)
            private val chargeExceptionCount = AtomicInteger(0)
            private val useExceptionCount = AtomicInteger(0)
            private val initPoint = 100_000L
            private val operationCount = 500

            @BeforeEach
            fun setup() {
                chargeExceptionCount.set(0)
                useExceptionCount.set(0)
            }

            @Test
            @Order(1)
            @DisplayName("남은 포인트가 정상적으로 반환되고")
            fun remainingPointsMustBeCorrect() {
                // given
                val userId = 1L
                val chargeAmount = 100L
                val useAmount = 50L
                userPointRepository.insertOrUpdate(userId, initPoint)

                // when
                repeat(operationCount) {
                    threadPools.execute {
                        pointService.chargePoint(PointRequest(userId, chargeAmount))
                    }
                    threadPools.execute {
                        pointService.usePoint(PointRequest(userId, useAmount))
                    }
                }

                threadPools.shutdown()
                threadPools.awaitTermination(1, TimeUnit.MINUTES)

                // then
                val finalUserPoint = userPointRepository.selectById(userId)
                val expectedPoint = initPoint + (chargeAmount * operationCount) - (useAmount * operationCount)

                assertEquals(expectedPoint, finalUserPoint.point)
            }

            @Test
            @Order(2)
            @DisplayName("포인트 범위 이외의 요청은 취소하며")
            fun outsideRangeShouldBeCancelled() {
                // given
                val userId = 2L
                val chargeAmount = 200_003L
                val useAmount = 200_007L
                userPointRepository.insertOrUpdate(userId, initPoint)

                // when
                repeat(operationCount) {
                    threadPools.execute {
                        try {
                            pointService.chargePoint(PointRequest(userId, chargeAmount))
                        } catch (e: Exception) {
                            chargeExceptionCount.incrementAndGet()
                        }
                    }
                    threadPools.execute {
                        try {
                            pointService.usePoint(PointRequest(userId, useAmount))
                        } catch (e: Exception) {
                            useExceptionCount.incrementAndGet()
                        }
                    }
                }

                threadPools.shutdown()
                threadPools.awaitTermination(1, TimeUnit.MINUTES)

                // then
                val finalUserPoint = userPointRepository.selectById(userId)
                val expectedPoint =
                    initPoint +
                        (chargeAmount * operationCount) -
                        (useAmount * operationCount) -
                        (chargeAmount * chargeExceptionCount.get()) +
                        (useAmount * useExceptionCount.get())

                assertEquals(expectedPoint, finalUserPoint.point)
            }

            @Test
            @Order(3)
            @DisplayName("포인트 이력에는 정상적으로 수행된 요청만 남겨야 한다")
            fun pointHistoryMustOnlyContainSuccessfully() {
                // given
                val userId = 3L
                val chargeAmount = 700_003L
                val useAmount = 700_007L
                userPointRepository.insertOrUpdate(userId, initPoint)

                // when
                repeat(operationCount) {
                    threadPools.execute {
                        try {
                            pointService.chargePoint(PointRequest(userId, chargeAmount))
                        } catch (e: Exception) {
                            chargeExceptionCount.incrementAndGet()
                        }
                    }
                    threadPools.execute {
                        try {
                            pointService.usePoint(PointRequest(userId, useAmount))
                        } catch (e: Exception) {
                            useExceptionCount.incrementAndGet()
                        }
                    }
                }

                threadPools.shutdown()
                threadPools.awaitTermination(1, TimeUnit.MINUTES)

                // then
                val history = pointHistoryRepository.selectAllByUserId(userId)
                assertEquals(operationCount * 2 - chargeExceptionCount.get() - useExceptionCount.get(), history.size)
                assertEquals(history.count { it.type == TransactionType.CHARGE }, operationCount - chargeExceptionCount.get())
                assertEquals(history.count { it.type == TransactionType.USE }, operationCount - useExceptionCount.get())
            }
        }

        @Nested
        @DisplayName("두명의 유저가 동시에 포인트 충전을 할 때")
        inner class TwoUserPointsTest {
            private val threadPools = Executors.newFixedThreadPool(1000)
            private val initPoint = 100_000L
            private val operationCount = 10_000

            @Test
            @DisplayName("포인트 잔액과 이력이 정상적으로 저장되어야 한다.")
            fun mustNeverBeAffectedByOtherUsersLock() {
                // given
                val userId1 = 4L
                val userId2 = 5L
                userPointRepository.insertOrUpdate(userId1, initPoint)
                userPointRepository.insertOrUpdate(userId2, initPoint)

                // when
                repeat(operationCount) {
                    threadPools.execute {
                        pointService.chargePoint(PointRequest(userId1, 10L))
                    }
                    threadPools.execute {
                        pointService.chargePoint(PointRequest(userId2, 10L))
                    }
                }
                threadPools.shutdown()
                threadPools.awaitTermination(1, TimeUnit.MINUTES)

                // then
                val userPoint1 = pointService.getUserPoint(userId1)
                val userPoint2 = pointService.getUserPoint(userId2)
                assertEquals(200_000, userPoint1.point)
                assertEquals(200_000, userPoint2.point)

                val pointHistory1 = pointService.getPointHistory(userId1)
                val pointHistory2 = pointService.getPointHistory(userId2)
                assertEquals(10_000, pointHistory1.size)
                assertEquals(10_000, pointHistory2.size)
            }
        }
    }
