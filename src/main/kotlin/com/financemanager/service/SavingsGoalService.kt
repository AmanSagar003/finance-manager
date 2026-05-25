package com.financemanager.service

import com.financemanager.dto.SavingsGoalRequest
import com.financemanager.dto.SavingsGoalResponse
import com.financemanager.dto.UpdateGoalProgressRequest
import com.financemanager.exception.ResourceNotFoundException
import com.financemanager.exception.UnauthorizedException
import com.financemanager.model.GoalStatus
import com.financemanager.model.SavingsGoal
import com.financemanager.repository.SavingsGoalRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode

@Service
class SavingsGoalService(
    private val savingsGoalRepository: SavingsGoalRepository,
    private val userService: UserService
) {

    @Transactional
    fun createGoal(username: String, request: SavingsGoalRequest): SavingsGoalResponse {
        val user = userService.getUserByUsername(username)
        val goal = savingsGoalRepository.save(
            SavingsGoal(
                user = user,
                name = request.name,
                description = request.description,
                targetAmount = request.targetAmount,
                currentAmount = request.currentAmount,
                targetDate = request.targetDate
            )
        )
        return goal.toResponse()
    }

    fun getUserGoals(username: String): List<SavingsGoalResponse> {
        val user = userService.getUserByUsername(username)
        return savingsGoalRepository.findByUserOrderByCreatedAtDesc(user).map { it.toResponse() }
    }

    fun getGoalById(username: String, id: Long): SavingsGoalResponse {
        val user = userService.getUserByUsername(username)
        val goal = savingsGoalRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $id") }
        if (goal.user.id != user.id) throw UnauthorizedException("Access denied")
        return goal.toResponse()
    }

    @Transactional
    fun updateGoal(username: String, id: Long, request: SavingsGoalRequest): SavingsGoalResponse {
        val user = userService.getUserByUsername(username)
        val existing = savingsGoalRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $id") }
        if (existing.user.id != user.id) throw UnauthorizedException("Access denied")

        val updated = savingsGoalRepository.save(
            existing.copy(
                name = request.name,
                description = request.description,
                targetAmount = request.targetAmount,
                currentAmount = request.currentAmount,
                targetDate = request.targetDate
            )
        )
        return updated.toResponse()
    }

    @Transactional
    fun updateProgress(username: String, id: Long, request: UpdateGoalProgressRequest): SavingsGoalResponse {
        val user = userService.getUserByUsername(username)
        val goal = savingsGoalRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $id") }
        if (goal.user.id != user.id) throw UnauthorizedException("Access denied")

        goal.currentAmount = request.currentAmount
        if (goal.currentAmount >= goal.targetAmount) {
            goal.status = GoalStatus.COMPLETED
        }
        return savingsGoalRepository.save(goal).toResponse()
    }

    @Transactional
    fun updateGoalStatus(username: String, id: Long, status: GoalStatus): SavingsGoalResponse {
        val user = userService.getUserByUsername(username)
        val goal = savingsGoalRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $id") }
        if (goal.user.id != user.id) throw UnauthorizedException("Access denied")
        goal.status = status
        return savingsGoalRepository.save(goal).toResponse()
    }

    @Transactional
    fun deleteGoal(username: String, id: Long) {
        val user = userService.getUserByUsername(username)
        val goal = savingsGoalRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Goal not found with id: $id") }
        if (goal.user.id != user.id) throw UnauthorizedException("Access denied")
        savingsGoalRepository.delete(goal)
    }

    private fun SavingsGoal.toResponse(): SavingsGoalResponse {
        val progress = if (targetAmount > BigDecimal.ZERO) {
            currentAmount.divide(targetAmount, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100)).toDouble()
        } else 0.0
        return SavingsGoalResponse(id, name, description, targetAmount, currentAmount, targetDate, status, progress, createdAt)
    }
}
