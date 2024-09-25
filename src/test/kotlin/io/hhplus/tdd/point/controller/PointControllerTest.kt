package io.hhplus.tdd.point.controller

import io.hhplus.tdd.point.config.PointConfig
import io.hhplus.tdd.point.domain.TransactionType
import io.hhplus.tdd.point.dto.ChargePointResponse
import io.hhplus.tdd.point.dto.PointHistoryResponse
import io.hhplus.tdd.point.dto.PointRequest
import io.hhplus.tdd.point.dto.UsePointResponse
import io.hhplus.tdd.point.dto.UserPointResponse
import io.hhplus.tdd.point.service.PointService
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@WebMvcTest(PointController::class)
class PointControllerTest {
    @Autowired
    private lateinit var mockMvc: MockMvc

    @MockBean
    private lateinit var pointService: PointService

    @Test
    fun `특정 유저의 포인트를 조회할 수 있어야 한다`() {
        // given
        val userId = 1L
        val expectedResponse = UserPointResponse(1L, 100L)

        // when
        `when`(pointService.getUserPoint(userId)).thenReturn(expectedResponse)

        // then
        mockMvc.perform(
            get("/point/$userId"),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(expectedResponse.id.toInt())))
            .andExpect(jsonPath("$.point", `is`(expectedResponse.point.toInt())))

        verify(pointService).getUserPoint(userId)
    }

    @Test
    fun `특정 유저의 포인트 충전,이용 내역을 조회할 수 있어야 한다`() {
        // given
        val userId = 1L
        val expectedResponse =
            listOf(
                PointHistoryResponse(
                    1L,
                    1L,
                    TransactionType.CHARGE,
                    100L,
                    System.currentTimeMillis(),
                ),
            )

        // when
        `when`(pointService.getPointHistory(userId)).thenReturn(expectedResponse)

        // then
        mockMvc.perform(
            get("/point/$userId/histories"),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$[0].userId", `is`(expectedResponse[0].userId.toInt())))
            .andExpect(jsonPath("$[0].type", `is`(expectedResponse[0].type.toString())))
            .andExpect(jsonPath("$[0].amount", `is`(expectedResponse[0].amount.toInt())))

        verify(pointService).getPointHistory(userId)
    }

    @Test
    fun `특정 유저의 범위 금액 내 포인트를 충전할 수 있어야 한다`() {
        // given
        val userId = 1L
        val amount = PointConfig.MAX_BALANCE
        val expectedResponse = ChargePointResponse(1L, 100L)

        // when
        `when`(pointService.chargePoint(PointRequest(userId, amount))).thenReturn(expectedResponse)

        // then
        mockMvc.perform(
            patch("/point/$userId/charge")
                .content(amount.toString())
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(expectedResponse.id.toInt())))
            .andExpect(jsonPath("$.point", `is`(expectedResponse.point.toInt())))

        verify(pointService).chargePoint(PointRequest(userId, amount))
    }

    @Test
    fun `특정 유저의 포인트를 사용할 수 있어야 한다`() {
        // given
        val userId = 1L
        val amount = 100L
        val expectedResponse = UsePointResponse(1L, 100L)

        // when
        `when`(pointService.usePoint(PointRequest(userId, amount))).thenReturn(expectedResponse)

        // then
        mockMvc.perform(
            patch("/point/$userId/use")
                .content(amount.toString())
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isOk)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id", `is`(expectedResponse.id.toInt())))
            .andExpect(jsonPath("$.point", `is`(expectedResponse.point.toInt())))

        verify(pointService).usePoint(PointRequest(userId, amount))
    }

    @Test
    fun `충전하는 포인트가 0원 이하일 때 에러 메시지를 반환한다`() {
        // given
        val userId = 1L
        val amount = 0L

        // when, then
        mockMvc.perform(
            patch("/point/$userId/charge")
                .content(amount.toString())
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code", `is`("400")))
            .andExpect(jsonPath("$.message", `is`("금액은 0보다 커야 합니다.")))
    }

    @Test
    fun `충전하는 포인트가 최대 금액을 초과할 때 에러 메시지를 반환한다`() {
        // given
        val userId = 1L
        val amount = PointConfig.MAX_BALANCE + 1L

        // when, then
        mockMvc.perform(
            patch("/point/$userId/use")
                .content(amount.toString())
                .contentType(MediaType.APPLICATION_JSON),
        )
            .andExpect(status().isBadRequest)
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.code", `is`("400")))
            .andExpect(jsonPath("$.message", `is`("최대 금액을 초과할 수 없습니다. 최대 금액: ${PointConfig.MAX_BALANCE}")))
    }
}
