package com.financemanager.repository

import com.financemanager.model.Transaction
import com.financemanager.model.TransactionType
import com.financemanager.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.LocalDateTime

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {
    fun findByUserOrderByTransactionDateDesc(user: User): List<Transaction>
    fun findByUserAndTypeOrderByTransactionDateDesc(user: User, type: TransactionType): List<Transaction>
    fun findByUserAndCategoryOrderByTransactionDateDesc(user: User, category: String): List<Transaction>

    @Query("SELECT t FROM Transaction t WHERE t.user = :user AND t.transactionDate BETWEEN :start AND :end ORDER BY t.transactionDate DESC")
    fun findByUserAndDateRange(user: User, start: LocalDateTime, end: LocalDateTime): List<Transaction>

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type")
    fun sumByUserAndType(user: User, type: TransactionType): BigDecimal

    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.user = :user AND t.type = :type AND t.transactionDate BETWEEN :start AND :end")
    fun sumByUserAndTypeAndDateRange(user: User, type: TransactionType, start: LocalDateTime, end: LocalDateTime): BigDecimal

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.user = :user AND t.type = :type GROUP BY t.category")
    fun sumByCategoryForUserAndType(user: User, type: TransactionType): List<Array<Any>>

    fun findByUserAndTransactionDateBetweenOrderByTransactionDateDesc(
        user: User, start: LocalDateTime, end: LocalDateTime
    ): List<Transaction>
}
