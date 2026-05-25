package com.financemanager.model

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

enum class GoalStatus { ACTIVE, COMPLETED, CANCELLED }

@Entity
@Table(name = "savings_goals")
data class SavingsGoal(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(nullable = false)
    val name: String = "",

    @Column
    val description: String = "",

    @Column(nullable = false)
    val targetAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    var currentAmount: BigDecimal = BigDecimal.ZERO,

    @Column(nullable = false)
    val targetDate: LocalDate = LocalDate.now(),

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: GoalStatus = GoalStatus.ACTIVE,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
