package io.hhplus.tdd.point.helper

import io.hhplus.tdd.point.config.PointConfig
import io.hhplus.tdd.point.exception.InvalidAmountException
import org.junit.jupiter.api.Assertions.assertDoesNotThrow
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class PointValidateHelperTest {
    private lateinit var pointValidateHelper: PointValidateHelper

    @BeforeEach
    fun setup() {
        pointValidateHelper = PointValidateHelper()
    }

    @Test
    fun `요청받은 포인트 값이 지정 범위를 벗어나면 예외가 발생해야 한다`() {
        // given
        val minPoint = 1L
        val middlePoint = PointConfig.MAX_BALANCE / 2L
        val maxPoint = PointConfig.MAX_BALANCE

        val negativePoint = -1L
        val overPoint = PointConfig.MAX_BALANCE + 1L

        // when & then
        assertDoesNotThrow { pointValidateHelper.validatePositiveAmount(minPoint) }
        assertDoesNotThrow { pointValidateHelper.validatePositiveAmount(middlePoint) }
        assertDoesNotThrow { pointValidateHelper.validatePositiveAmount(maxPoint) }

        assertThrows<InvalidAmountException> {
            pointValidateHelper.validatePositiveAmount(negativePoint)
        }.apply {
            assertEquals("포인트는 0보다 커야 합니다.", message)
        }

        assertThrows<InvalidAmountException> {
            pointValidateHelper.validatePositiveAmount(overPoint)
        }.apply {
            assertEquals("최대 금액을 초과할 수 없습니다. 최대 금액: ${PointConfig.MAX_BALANCE}", message)
        }
    }

    @Test
    fun `잔액과 포인트 충전 금액의 합이 최대 잔액을 넘으면 예외가 발생해야 한다`() {
        // given
        val currentPoint = PointConfig.MAX_BALANCE / 2
        val amountToAdd = PointConfig.MAX_BALANCE / 2
        val amountToAddOverMax = PointConfig.MAX_BALANCE / 2 + 2L

        // when & then
        assertDoesNotThrow { pointValidateHelper.validateMaxBalance(currentPoint, amountToAdd) }

        assertThrows<InvalidAmountException> {
            pointValidateHelper.validateMaxBalance(currentPoint, amountToAddOverMax)
        }.apply {
            assertEquals("최대 잔액을 초과할 수 없습니다. 최대 잔액: ${PointConfig.MAX_BALANCE}", message)
        }
    }

    @Test
    fun `사용하려는 포인트가 잔액보다 크면 예외가 발생해야 한다`() {
        // given
        val currentPoint = 100L
        val amountToUse = 100L
        val amountToUseOver = 101L

        // when & then
        assertDoesNotThrow { pointValidateHelper.validateMinBalance(currentPoint, amountToUse) }

        assertThrows<InvalidAmountException> {
            pointValidateHelper.validateMinBalance(currentPoint, amountToUseOver)
        }.apply {
            assertEquals("포인트 잔액이 0보다 작을 수 없습니다.", message)
        }
    }
}
