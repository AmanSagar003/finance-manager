package com.financemanager.controller

import com.financemanager.dto.ApiResponse
import com.financemanager.dto.UserResponse
import com.financemanager.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(private val userService: UserService) {

    @GetMapping("/profile")
    fun getProfile(@AuthenticationPrincipal userDetails: UserDetails): ResponseEntity<ApiResponse<UserResponse>> {
        val profile = userService.getUserProfile(userDetails.username)
        return ResponseEntity.ok(ApiResponse(true, "Profile retrieved", profile))
    }
}
