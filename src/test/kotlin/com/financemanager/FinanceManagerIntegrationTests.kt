package com.financemanager

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.financemanager.dto.*
import com.financemanager.model.TransactionType
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class FinanceManagerIntegrationTests {

    @Autowired
    lateinit var mockMvc: MockMvc

    private val mapper = ObjectMapper().registerModule(JavaTimeModule())

    companion object {
        var token = ""
        var transactionId = 0L
        var goalId = 0L
    }

    // ==================== AUTH TESTS ====================

    @Test
    @Order(1)
    fun `register new user - success`() {
        val request = RegisterRequest("testuser", "test@example.com", "password123", "Test User")
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").isNotEmpty)
            .andExpect(jsonPath("$.data.username").value("testuser"))
    }

    @Test
    @Order(2)
    fun `register duplicate username - conflict`() {
        val request = RegisterRequest("testuser", "other@example.com", "password123", "Other User")
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.success").value(false))
    }

    @Test
    @Order(3)
    fun `register with invalid email - bad request`() {
        val request = RegisterRequest("newuser2", "not-an-email", "password123", "New User")
        mockMvc.perform(
            post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(4)
    fun `login with valid credentials - success`() {
        val request = LoginRequest("testuser", "password123")
        val result = mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.token").isNotEmpty)
            .andReturn()

        val response = mapper.readTree(result.response.contentAsString)
        token = response["data"]["token"].asText()
    }

    @Test
    @Order(5)
    fun `login with invalid password - unauthorized`() {
        val request = LoginRequest("testuser", "wrongpassword")
        mockMvc.perform(
            post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isUnauthorized)
    }

    // ==================== USER TESTS ====================

    @Test
    @Order(6)
    fun `get user profile - success`() {
        mockMvc.perform(
            get("/api/users/profile")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.username").value("testuser"))
            .andExpect(jsonPath("$.data.email").value("test@example.com"))
    }

    @Test
    @Order(7)
    fun `get profile without token - unauthorized`() {
        mockMvc.perform(get("/api/users/profile"))
            .andExpect(status().isUnauthorized)
    }

    // ==================== TRANSACTION TESTS ====================

    @Test
    @Order(8)
    fun `create income transaction - success`() {
        val request = TransactionRequest(
            amount = BigDecimal("5000.00"),
            type = TransactionType.INCOME,
            category = "Salary",
            description = "Monthly salary",
            transactionDate = LocalDateTime.now()
        )
        mockMvc.perform(
            post("/api/transactions")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.amount").value(5000.00))
            .andExpect(jsonPath("$.data.type").value("INCOME"))
            .andExpect(jsonPath("$.data.category").value("Salary"))
    }

    @Test
    @Order(9)
    fun `create expense transaction - success`() {
        val request = TransactionRequest(
            amount = BigDecimal("150.00"),
            type = TransactionType.EXPENSE,
            category = "Food",
            description = "Groceries",
            transactionDate = LocalDateTime.now()
        )
        val result = mockMvc.perform(
            post("/api/transactions")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.type").value("EXPENSE"))
            .andReturn()

        val response = mapper.readTree(result.response.contentAsString)
        transactionId = response["data"]["id"].asLong()
    }

    @Test
    @Order(10)
    fun `create transaction with negative amount - bad request`() {
        val request = TransactionRequest(
            amount = BigDecimal("-100.00"),
            type = TransactionType.EXPENSE,
            category = "Food",
            transactionDate = LocalDateTime.now()
        )
        mockMvc.perform(
            post("/api/transactions")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    @Order(11)
    fun `get all transactions - success`() {
        mockMvc.perform(
            get("/api/transactions")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(2))
    }

    @Test
    @Order(12)
    fun `get transactions filtered by type - success`() {
        mockMvc.perform(
            get("/api/transactions?type=INCOME")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.length()").value(1))
            .andExpect(jsonPath("$.data[0].type").value("INCOME"))
    }

    @Test
    @Order(13)
    fun `get transaction by id - success`() {
        mockMvc.perform(
            get("/api/transactions/$transactionId")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(transactionId))
    }

    @Test
    @Order(14)
    fun `get nonexistent transaction - not found`() {
        mockMvc.perform(
            get("/api/transactions/99999")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isNotFound)
    }

    @Test
    @Order(15)
    fun `update transaction - success`() {
        val request = TransactionRequest(
            amount = BigDecimal("200.00"),
            type = TransactionType.EXPENSE,
            category = "Food",
            description = "Updated groceries",
            transactionDate = LocalDateTime.now()
        )
        mockMvc.perform(
            put("/api/transactions/$transactionId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.amount").value(200.00))
            .andExpect(jsonPath("$.data.description").value("Updated groceries"))
    }

    @Test
    @Order(16)
    fun `delete transaction - success`() {
        mockMvc.perform(
            delete("/api/transactions/$transactionId")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    // ==================== SAVINGS GOAL TESTS ====================

    @Test
    @Order(17)
    fun `create savings goal - success`() {
        val request = SavingsGoalRequest(
            name = "Emergency Fund",
            description = "6 months expenses",
            targetAmount = BigDecimal("10000.00"),
            currentAmount = BigDecimal("2000.00"),
            targetDate = LocalDate.now().plusYears(1)
        )
        val result = mockMvc.perform(
            post("/api/savings-goals")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.data.name").value("Emergency Fund"))
            .andExpect(jsonPath("$.data.progressPercentage").value(20.0))
            .andReturn()

        val response = mapper.readTree(result.response.contentAsString)
        goalId = response["data"]["id"].asLong()
    }

    @Test
    @Order(18)
    fun `get all savings goals - success`() {
        mockMvc.perform(
            get("/api/savings-goals")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isArray)
            .andExpect(jsonPath("$.data.length()").value(1))
    }

    @Test
    @Order(19)
    fun `get savings goal by id - success`() {
        mockMvc.perform(
            get("/api/savings-goals/$goalId")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.id").value(goalId))
            .andExpect(jsonPath("$.data.name").value("Emergency Fund"))
    }

    @Test
    @Order(20)
    fun `update savings goal progress - success`() {
        val request = UpdateGoalProgressRequest(currentAmount = BigDecimal("5000.00"))
        mockMvc.perform(
            patch("/api/savings-goals/$goalId/progress")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.currentAmount").value(5000.00))
            .andExpect(jsonPath("$.data.progressPercentage").value(50.0))
    }

    @Test
    @Order(21)
    fun `update goal to completed when target reached - success`() {
        val request = UpdateGoalProgressRequest(currentAmount = BigDecimal("10000.00"))
        mockMvc.perform(
            patch("/api/savings-goals/$goalId/progress")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.status").value("COMPLETED"))
            .andExpect(jsonPath("$.data.progressPercentage").value(100.0))
    }

    @Test
    @Order(22)
    fun `update savings goal details - success`() {
        val request = SavingsGoalRequest(
            name = "Updated Emergency Fund",
            description = "Updated description",
            targetAmount = BigDecimal("15000.00"),
            currentAmount = BigDecimal("5000.00"),
            targetDate = LocalDate.now().plusYears(2)
        )
        mockMvc.perform(
            put("/api/savings-goals/$goalId")
                .header("Authorization", "Bearer $token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.name").value("Updated Emergency Fund"))
            .andExpect(jsonPath("$.data.targetAmount").value(15000.00))
    }

    @Test
    @Order(23)
    fun `delete savings goal - success`() {
        mockMvc.perform(
            delete("/api/savings-goals/$goalId")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
    }

    // ==================== REPORT TESTS ====================

    @Test
    @Order(24)
    fun `get summary report - success`() {
        mockMvc.perform(
            get("/api/reports/summary")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.totalIncome").isNumber)
            .andExpect(jsonPath("$.data.totalExpenses").isNumber)
            .andExpect(jsonPath("$.data.netBalance").isNumber)
    }

    @Test
    @Order(25)
    fun `get monthly report - success`() {
        val now = LocalDateTime.now()
        mockMvc.perform(
            get("/api/reports/monthly?year=${now.year}&month=${now.monthValue}")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.year").value(now.year))
            .andExpect(jsonPath("$.data.month").value(now.monthValue))
            .andExpect(jsonPath("$.data.transactions").isArray)
    }

    @Test
    @Order(26)
    fun `get category report for expenses - success`() {
        mockMvc.perform(
            get("/api/reports/categories?type=EXPENSE")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isMap)
    }

    @Test
    @Order(27)
    fun `get category report for income - success`() {
        mockMvc.perform(
            get("/api/reports/categories?type=INCOME")
                .header("Authorization", "Bearer $token")
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data").isMap)
    }

    // ==================== SECURITY TESTS ====================

    @Test
    @Order(28)
    fun `access protected endpoint without token - unauthorized`() {
        mockMvc.perform(get("/api/transactions"))
            .andExpect(status().isUnauthorized)
    }

    @Test
    @Order(29)
    fun `access protected endpoint with invalid token - unauthorized`() {
        mockMvc.perform(
            get("/api/transactions")
                .header("Authorization", "Bearer invalid.token.here")
        )
            .andExpect(status().isUnauthorized)
    }
}
