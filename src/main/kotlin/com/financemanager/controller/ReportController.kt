package com.financemanager.controller

import com.financemanager.dto.ApiResponse
import com.financemanager.dto.MonthlyReport
import com.financemanager.dto.SummaryReport
import com.financemanager.model.TransactionType
import com.financemanager.service.ReportService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*
import java.math.BigDecimal

@RestController
@RequestMapping("/api/reports")
class ReportController(private val reportService: ReportService) {

    @GetMapping("/summary")
    fun getSummary(
        @AuthenticationPrincipal userDetails: UserDetails
    ): ResponseEntity<ApiResponse<SummaryReport>> {
        val report = reportService.getSummaryReport(userDetails.username)
        return ResponseEntity.ok(ApiResponse(true, "Summary report retrieved", report))
    }

    @GetMapping("/monthly")
    fun getMonthlyReport(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam year: Int,
        @RequestParam month: Int
    ): ResponseEntity<ApiResponse<MonthlyReport>> {
        val report = reportService.getMonthlyReport(userDetails.username, year, month)
        return ResponseEntity.ok(ApiResponse(true, "Monthly report retrieved", report))
    }

    @GetMapping("/categories")
    fun getCategoryReport(
        @AuthenticationPrincipal userDetails: UserDetails,
        @RequestParam(defaultValue = "EXPENSE") type: TransactionType
    ): ResponseEntity<ApiResponse<Map<String, BigDecimal>>> {
        val report = reportService.getCategoryReport(userDetails.username, type)
        return ResponseEntity.ok(ApiResponse(true, "Category report retrieved", report))
    }
}
