package io.hhplus.tdd.point.domain

import io.hhplus.tdd.point.exception.InvalidAmountException

data class UserPoint(
    val id: Long,
    val point: Long,
    val updateMillis: Long,
) {
    companion object {
        const val MAX_BALANCE: Long = 1_000_000L
    }

    init {
        validate()
    }

    private fun validate() {
        if (point < 0) {
            throw InvalidAmountException("포인트 잔액이 0보다 작을 수 없습니다.")
        }
    }
}
