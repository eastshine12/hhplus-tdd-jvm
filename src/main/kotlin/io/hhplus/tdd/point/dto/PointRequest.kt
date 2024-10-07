package io.hhplus.tdd.point.dto

import io.hhplus.tdd.point.config.PointConfig
import io.hhplus.tdd.point.exception.InvalidAmountException

data class PointRequest(
    val userId: Long,
    val amount: Long,
) {
    init {
        validate()
    }

    private fun validate() {
        if (amount <= 0) {
            throw InvalidAmountException("금액은 0보다 커야 합니다.")
        }
        if (amount > PointConfig.MAX_BALANCE) {
            throw InvalidAmountException("최대 금액을 초과할 수 없습니다. 최대 금액: ${PointConfig.MAX_BALANCE}")
        }
    }
}
