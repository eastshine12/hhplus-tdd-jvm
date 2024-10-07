package io.hhplus.tdd.point.config

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class PointConfig {
    companion object {
        var MAX_BALANCE: Long = 1_000_000L
    }

    @Value("\${point.max-balance}")
    private var maxBalance: Long = 1_000_000L

    @PostConstruct
    fun init() {
        MAX_BALANCE = maxBalance
    }
}
