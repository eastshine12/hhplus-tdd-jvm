package io.hhplus.tdd.point.helper

interface IPointValidateHelper {
    fun validatePositiveAmount(amount: Long)

    fun validateMaxBalance(
        currentBalance: Long,
        amountToAdd: Long,
    )

    fun validateMinBalance(
        currentBalance: Long,
        amountToUse: Long,
    )
}
