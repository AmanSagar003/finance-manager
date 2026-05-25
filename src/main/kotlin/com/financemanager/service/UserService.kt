package com.financemanager.service

import com.financemanager.config.JwtUtil
import com.financemanager.dto.*
import com.financemanager.exception.DuplicateResourceException
import com.financemanager.exception.ResourceNotFoundException
import com.financemanager.model.User
import com.financemanager.repository.UserRepository
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil,
    private val authenticationManager: AuthenticationManager
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username)
            .orElseThrow { UsernameNotFoundException("User not found: $username") }
        return org.springframework.security.core.userdetails.User(
            user.username,
            user.password,
            listOf(SimpleGrantedAuthority("ROLE_USER"))
        )
    }

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        if (userRepository.existsByUsername(request.username))
            throw DuplicateResourceException("Username '${request.username}' already taken")
        if (userRepository.existsByEmail(request.email))
            throw DuplicateResourceException("Email '${request.email}' already registered")

        val user = userRepository.save(
            User(
                username = request.username,
                email = request.email,
                password = passwordEncoder.encode(request.password),
                fullName = request.fullName
            )
        )
        val token = jwtUtil.generateToken(user.username)
        return AuthResponse(token, user.id, user.username, user.email, user.fullName)
    }

    fun login(request: LoginRequest): AuthResponse {
        authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken(request.username, request.password)
        )
        val user = userRepository.findByUsername(request.username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        val token = jwtUtil.generateToken(user.username)
        return AuthResponse(token, user.id, user.username, user.email, user.fullName)
    }

    fun getUserProfile(username: String): UserResponse {
        val user = userRepository.findByUsername(username)
            .orElseThrow { ResourceNotFoundException("User not found") }
        return user.toResponse()
    }

    fun getUserByUsername(username: String): User =
        userRepository.findByUsername(username)
            .orElseThrow { ResourceNotFoundException("User not found") }

    private fun User.toResponse() = UserResponse(id, username, email, fullName, createdAt)
}
