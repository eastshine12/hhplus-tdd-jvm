package io.hhplus.tdd.point.exception

class InvalidAmountException(message: String) : IllegalArgumentException(message)

class LockAcquisitionException(message: String) : IllegalStateException(message)
