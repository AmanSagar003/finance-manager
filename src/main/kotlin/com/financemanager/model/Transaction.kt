package com.financemanager.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

enum class TransactionType { INCOME, EXPENSE }

@Entity
@Table(name = "transactions")
data class Transaction(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(nullable = false)
    val amount: BigDecimal = BigDecimal.ZERO,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: TransactionType = TransactionType.EXPENSE,

    @Column(nullable = false)
    val category: String = "",

    @Column
    val description: String = "",

    @Column(nullable = false)
    val transactionDate: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
