package com.financemanager.service

import com.financemanager.dto.MonthlyReport
import com.financemanager.dto.SummaryReport
import com.financemanager.dto.TransactionResponse
import com.financemanager.model.Transaction
import com.financemanager.model.TransactionType
import com.financemanager.repository.TransactionRepository
import org.springframework.stereotype.Service
import java.math.BigDecimal
import java.time.LocalDateTime

@Service
class ReportService(
    private val transactionRepository: TransactionRepository,
    private val userService: UserService
) {

    fun getSummaryReport(username: String): SummaryReport {
        val user = userService.getUserByUsername(username)
        val totalIncome = transactionRepository.sumByUserAndType(user, TransactionType.INCOME)
        val totalExpenses = transactionRepository.sumByUserAndType(user, TransactionType.EXPENSE)
        val transactions = transactionRepository.findByUserOrderByTransactionDateDesc(user)
        val categoryBreakdown = transactionRepository
            .sumByCategoryForUserAndType(user, TransactionType.EXPENSE)
            .associate { row -> row[0].toString() to (row[1] as BigDecimal) }

        return SummaryReport(
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netBalance = totalIncome.subtract(totalExpenses),
            transactionCount = transactions.size,
            categoryBreakdown = categoryBreakdown
        )
    }

    fun getMonthlyReport(username: String, year: Int, month: Int): MonthlyReport {
        val user = userService.getUserByUsername(username)
        val start = LocalDateTime.of(year, month, 1, 0, 0)
        val end = start.plusMonths(1).minusSeconds(1)

        val transactions = transactionRepository
            .findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(user, start, end)

        val totalIncome = transactions.filter { it.type == TransactionType.INCOME }
            .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        val totalExpenses = transactions.filter { it.type == TransactionType.EXPENSE }
            .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }

        return MonthlyReport(
            year = year,
            month = month,
            totalIncome = totalIncome,
            totalExpenses = totalExpenses,
            netBalance = totalIncome.subtract(totalExpenses),
            transactions = transactions.map { it.toResponse() }
        )
    }

    fun getCategoryReport(username: String, type: TransactionType): Map<String, BigDecimal> {
        val user = userService.getUserByUsername(username)
        return transactionRepository.sumByCategoryForUserAndType(user, type)
            .associate { row -> row[0].toString() to (row[1] as BigDecimal) }
    }

    private fun Transaction.toResponse() = TransactionResponse(
        id, amount, type, category, description, transactionDate, createdAt
    )
}
