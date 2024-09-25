package io.hhplus.tdd.point.testdoubles

import io.hhplus.tdd.point.config.PointConfig
import io.hhplus.tdd.point.exception.InvalidAmountException
import io.hhplus.tdd.point.helper.IPointValidateHelper

class FakePointValidateHelper : IPointValidateHelper {
    override fun validatePositiveAmount(amount: Long) {
        if (amount <= 0) {
            throw InvalidAmountException("포인트는 0보다 커야 합니다.")
        }
        if (amount > PointConfig.MAX_BALANCE) {
            throw InvalidAmountException("최대 금액을 초과할 수 없습니다. 최대 금액: ${PointConfig.MAX_BALANCE}")
        }
    }

    override fun validateMaxBalance(
        currentBalance: Long,
        amountToAdd: Long,
    ) {
        val total = currentBalance + amountToAdd
        if (total > PointConfig.MAX_BALANCE) {
            throw InvalidAmountException("최대 잔액을 초과할 수 없습니다. 최대 잔액: ${PointConfig.MAX_BALANCE}")
        }
    }

    override fun validateMinBalance(
        currentBalance: Long,
        amountToUse: Long,
    ) {
        if (currentBalance - amountToUse < 0) {
            throw InvalidAmountException("포인트 잔액이 0보다 작을 수 없습니다.")
        }
    }
}
