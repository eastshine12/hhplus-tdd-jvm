package io.hhplus.tdd.point.lock

import io.hhplus.tdd.point.exception.LockAcquisitionException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.concurrent.Executors

class UserLockManagerTest {
    private lateinit var userLockManager: IUserLockManager

    @BeforeEach
    fun setup() {
        userLockManager = UserLockManager()
    }

    @Test
    fun `executeWithLock()을 호출하여 락을 얻었을 때 내부 로직이 수행된다`() {
        // given
        val userId = 1L
        val expectedValue = true
        var isExecuted = false

        // when
        userLockManager.executeWithLock(userId) {
            isExecuted = true
        }

        // then
        assertEquals(expectedValue, isExecuted)
    }

    @Test
    fun `한 유저의 락 획득 시도 시간이 10초를 넘어가면 에러를 발생한다`() {
        // given
        val userId = 1L
        val expectedValue = false
        var isExecuted = false

        Executors.newSingleThreadExecutor().submit {
            userLockManager.executeWithLock(userId) {
                Thread.sleep(20000)
            }
        }

        Thread.sleep(100)

        // when, then
        assertThrows<LockAcquisitionException> {
            userLockManager.executeWithLock(userId) {
                isExecuted = true
            }
        }.apply {
            assertEquals("락 획득에 실패했습니다. userId: $userId", message)
        }
        assertEquals(expectedValue, isExecuted)
    }

    @Test
    fun `두 명의 유저가 각각 락을 독립적으로 사용해야 한다`() {
        // given
        val userId1 = 1L
        val userId2 = 2L

        Executors.newSingleThreadExecutor().submit {
            userLockManager.executeWithLock(userId1) {
                Thread.sleep(1000)
            }
        }

        Thread.sleep(100)

        // when
        val startTime = System.currentTimeMillis()
        userLockManager.executeWithLock(userId2) {
            Thread.sleep(500)
        }

        val endTime = System.currentTimeMillis()
        val executionTime = endTime - startTime

        // then
        assertTrue(executionTime < 1000)
    }
}
