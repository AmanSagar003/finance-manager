package com.financemanager.repository

import com.financemanager.model.GoalStatus
import com.financemanager.model.SavingsGoal
import com.financemanager.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SavingsGoalRepository : JpaRepository<SavingsGoal, Long> {
    fun findByUserOrderByCreatedAtDesc(user: User): List<SavingsGoal>
    fun findByUserAndStatusOrderByCreatedAtDesc(user: User, status: GoalStatus): List<SavingsGoal>
}
