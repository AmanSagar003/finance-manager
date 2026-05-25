package com.financemanager.controller

import com.financemanager.dto.*
import com.financemanager.service.UserService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(private val userService: UserService) {

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val auth = userService.register(request)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse(true, "User registered successfully", auth))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<AuthResponse>> {
        val auth = userService.login(request)
        return ResponseEntity.ok(ApiResponse(true, "Login successful", auth))
    }
}
