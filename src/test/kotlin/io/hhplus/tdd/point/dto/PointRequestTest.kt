package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.config.PointConfig
import io.hhplus.tdd.point.exception.InvalidAmountException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PointRequestTest {
    @Test
    fun `충전하는 포인트가 0원 이하일 때 에러 메시지를 반환한다`() {
        // given
        val userId = 1L
        val amount = 0L

        // when, then
        assertThrows<InvalidAmountException> {
            PointRequest(userId, amount)
        }.apply {
            assertEquals("금액은 0보다 커야 합니다.", message)
        }
    }

    @Test
    fun `충전하는 포인트가 최대 금액을 초과할 때 에러 메시지를 반환한다`() {
        // given
        val userId = 1L
        val amount = PointConfig.MAX_BALANCE + 1L

        // when, then
        assertThrows<InvalidAmountException> {
            PointRequest(userId, amount)
        }.apply {
            assertEquals("최대 금액을 초과할 수 없습니다. 최대 금액: ${PointConfig.MAX_BALANCE}", message)
        }
    }
}
