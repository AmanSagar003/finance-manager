package com.financemanager.controller

import com.financemanager.dto.ApiResponse
import com.financemanager.dto.TransactionRequest
import com.financemanager.dto.TransactionResponse
import com.financemanager.model.TransactionType
import com.financemanager.service.TransactionService
import jakarta.validation.Valid
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.time.LocalDateTime

@RestController
@RequestMapping("/api/transactions")
class TransactionController(private val transactionService: TransactionService) {

    @PostMapping
    fun createTransaction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: TransactionRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.createTransaction(userDetails.username, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Transaction created", transaction))
    }

    @GetMapping
    fun getAllTransactions(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(required = false) type: TransactionType?,
        @RequestParam(required = false) category: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) start: LocalDateTime?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) end: LocalDateTime?
    ): ResponseEntity<ApiResponse<List<TransactionResponse>>> {
        val transactions = when {
            start != null && end != null ->
                transactionService.getTransactionsByDateRange(userDetails.username, start, end)
            type != null ->
                transactionService.getTransactionsByType(userDetails.username, type)
            category != null ->
                transactionService.getTransactionsByCategory(userDetails.username, category)
            else ->
                transactionService.getUserTransactions(userDetails.username)
        }
        return ResponseEntity.ok(ApiResponse(true, "Transactions retrieved", transactions))
    }

    @GetMapping("/{id}")
    fun getTransaction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.getTransactionById(userDetails.username, id)
        return ResponseEntity.ok(ApiResponse(true, "Transaction retrieved", transaction))
    }

    @PutMapping("/{id}")
    fun updateTransaction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody request: TransactionRequest
    ): ResponseEntity<ApiResponse<TransactionResponse>> {
        val transaction = transactionService.updateTransaction(userDetails.username, id, request)
        return ResponseEntity.ok(ApiResponse(true, "Transaction updated", transaction))
    }

    @DeleteMapping("/{id}")
    fun deleteTransaction(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Nothing>> {
        transactionService.deleteTransaction(userDetails.username, id)
        return ResponseEntity.ok(ApiResponse(true, "Transaction deleted"))
    }
}
