package com.financemanager.service

import com.financemanager.dto.TransactionRequest
import com.financemanager.dto.TransactionResponse
import com.financemanager.exception.ResourceNotFoundException
import com.financemanager.exception.UnauthorizedException
import com.financemanager.model.Transaction
import com.financemanager.model.TransactionType
import com.financemanager.model.User
import com.financemanager.repository.TransactionRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TransactionService(
    private val transactionRepository: TransactionRepository,
    private val userService: UserService
) {

    @Transactional
    fun createTransaction(username: String, request: TransactionRequest): TransactionResponse {
        val user = userService.getUserByUsername(username)
        val transaction = transactionRepository.save(
            Transaction(
                user = user,
                amount = request.amount,
                type = request.type,
                category = request.category,
                description = request.description,
                transactionDate = request.transactionDate
            )
        )
        return transaction.toResponse()
    }

    fun getUserTransactions(username: String): List<TransactionResponse> {
        val user = userService.getUserByUsername(username)
        return transactionRepository.findByUserOrderByTransactionDateDesc(user).map { it.toResponse() }
    }

    fun getTransactionById(username: String, id: Long): TransactionResponse {
        val user = userService.getUserByUsername(username)
        val transaction = transactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Transaction not found with id: $id") }
        if (transaction.user.id != user.id) throw UnauthorizedException("Access denied")
        return transaction.toResponse()
    }

    fun getTransactionsByType(username: String, type: TransactionType): List<TransactionResponse> {
        val user = userService.getUserByUsername(username)
        return transactionRepository.findByUserAndTypeOrderByTransactionDateDesc(user, type).map { it.toResponse() }
    }

    fun getTransactionsByCategory(username: String, category: String): List<TransactionResponse> {
        val user = userService.getUserByUsername(username)
        return transactionRepository.findByUserAndCategoryOrderByTransactionDateDesc(user, category).map { it.toResponse() }
    }

    @Transactional
    fun updateTransaction(username: String, id: Long, request: TransactionRequest): TransactionResponse {
        val user = userService.getUserByUsername(username)
        val existing = transactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Transaction not found with id: $id") }
        if (existing.user.id != user.id) throw UnauthorizedException("Access denied")

        val updated = transactionRepository.save(
            existing.copy(
                amount = request.amount,
                type = request.type,
                category = request.category,
                description = request.description,
                transactionDate = request.transactionDate
            )
        )
        return updated.toResponse()
    }

    @Transactional
    fun deleteTransaction(username: String, id: Long) {
        val user = userService.getUserByUsername(username)
        val transaction = transactionRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Transaction not found with id: $id") }
        if (transaction.user.id != user.id) throw UnauthorizedException("Access denied")
        transactionRepository.delete(transaction)
    }

    fun getTransactionsByDateRange(
        username: String, start: java.time.LocalDateTime, end: java.time.LocalDateTime
    ): List<TransactionResponse> {
        val user = userService.getUserByUsername(username)
        return transactionRepository.findByUserAndDateRange(user, start, end).map { it.toResponse() }
    }

    private fun Transaction.toResponse() = TransactionResponse(
        id, amount, type, category, description, transactionDate, createdAt
    )
}
