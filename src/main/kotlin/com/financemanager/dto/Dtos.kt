package com.financemanager.dto

import com.financemanager.model.GoalStatus
import com.financemanager.model.TransactionType
import jakarta.validation.constraints.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

// Auth DTOs
data class RegisterRequest(
    @field:NotBlank val username: String = "",
    @field:Email val email: String = "",
    @field:NotBlank @field:Size(min = 6) val password: String = "",
    @field:NotBlank val fullName: String = ""
)

data class LoginRequest(
    @field:NotBlank val username: String = "",
    @field:NotBlank val password: String = ""
)

data class AuthResponse(
    val token: String,
    val userId: Long,
    val username: String,
    val email: String,
    val fullName: String
)

// User DTOs
data class UserResponse(
    val id: Long,
    val username: String,
    val email: String,
    val fullName: String,
    val createdAt: LocalDateTime
)

// Transaction DTOs
data class TransactionRequest(
    @field:NotNull @field:Positive val amount: BigDecimal = BigDecimal.ZERO,
    @field:NotNull val type: TransactionType = TransactionType.EXPENSE,
    @field:NotBlank val category: String = "",
    val description: String = "",
    val transactionDate: LocalDateTime = LocalDateTime.now()
)

data class TransactionResponse(
    val id: Long,
    val amount: BigDecimal,
    val type: TransactionType,
    val category: String,
    val description: String,
    val transactionDate: LocalDateTime,
    val createdAt: LocalDateTime
)

// Savings Goal DTOs
data class SavingsGoalRequest(
    @field:NotBlank val name: String = "",
    val description: String = "",
    @field:NotNull @field:Positive val targetAmount: BigDecimal = BigDecimal.ZERO,
    val currentAmount: BigDecimal = BigDecimal.ZERO,
    @field:NotNull val targetDate: LocalDate = LocalDate.now()
)

data class UpdateGoalProgressRequest(
    @field:NotNull @field:PositiveOrZero val currentAmount: BigDecimal = BigDecimal.ZERO
)

data class SavingsGoalResponse(
    val id: Long,
    val name: String,
    val description: String,
    val targetAmount: BigDecimal,
    val currentAmount: BigDecimal,
    val targetDate: LocalDate,
    val status: GoalStatus,
    val progressPercentage: Double,
    val createdAt: LocalDateTime
)

// Report DTOs
data class SummaryReport(
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netBalance: BigDecimal,
    val transactionCount: Int,
    val categoryBreakdown: Map<String, BigDecimal>
)

data class MonthlyReport(
    val year: Int,
    val month: Int,
    val totalIncome: BigDecimal,
    val totalExpenses: BigDecimal,
    val netBalance: BigDecimal,
    val transactions: List<TransactionResponse>
)

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T? = null
)
