package com.financemanager.controller

import com.financemanager.dto.ApiResponse
import com.financemanager.dto.SavingsGoalRequest
import com.financemanager.dto.SavingsGoalResponse
import com.financemanager.dto.UpdateGoalProgressRequest
import com.financemanager.model.GoalStatus
import com.financemanager.service.SavingsGoalService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/savings-goals")
class SavingsGoalController(private val savingsGoalService: SavingsGoalService) {

    @PostMapping
    fun createGoal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @Valid @RequestBody request: SavingsGoalRequest
    ): ResponseEntity<ApiResponse<SavingsGoalResponse>> {
        val goal = savingsGoalService.createGoal(userDetails.username, request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "Savings goal created", goal))
    }

    @GetMapping
    fun getAllGoals(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<List<SavingsGoalResponse>>> {
        val goals = savingsGoalService.getUserGoals(userDetails.username)
        return ResponseEntity.ok(ApiResponse(true, "Goals retrieved", goals))
    }

    @GetMapping("/{id}")
    fun getGoal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<SavingsGoalResponse>> {
        val goal = savingsGoalService.getGoalById(userDetails.username, id)
        return ResponseEntity.ok(ApiResponse(true, "Goal retrieved", goal))
    }

    @PutMapping("/{id}")
    fun updateGoal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody request: SavingsGoalRequest
    ): ResponseEntity<ApiResponse<SavingsGoalResponse>> {
        val goal = savingsGoalService.updateGoal(userDetails.username, id, request)
        return ResponseEntity.ok(ApiResponse(true, "Goal updated", goal))
    }

    @PatchMapping("/{id}/progress")
    fun updateProgress(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateGoalProgressRequest
    ): ResponseEntity<ApiResponse<SavingsGoalResponse>> {
        val goal = savingsGoalService.updateProgress(userDetails.username, id, request)
        return ResponseEntity.ok(ApiResponse(true, "Progress updated", goal))
    }

    @PatchMapping("/{id}/status")
    fun updateStatus(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long,
        @RequestParam status: GoalStatus
    ): ResponseEntity<ApiResponse<SavingsGoalResponse>> {
        val goal = savingsGoalService.updateGoalStatus(userDetails.username, id, status)
        return ResponseEntity.ok(ApiResponse(true, "Status updated", goal))
    }

    @DeleteMapping("/{id}")
    fun deleteGoal(
        @AuthenticationPrincipal userDetails: UserDetails,
        @PathVariable id: Long
    ): ResponseEntity<ApiResponse<Nothing>> {
        savingsGoalService.deleteGoal(userDetails.username, id)
        return ResponseEntity.ok(ApiResponse(true, "Goal deleted"))
    }
}
