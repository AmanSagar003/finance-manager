package com.financemanager.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String = "",

    @Column(unique = true, nullable = false)
    val email: String = "",

    @Column(nullable = false)
    val password: String = "",

    @Column(nullable = false)
    val fullName: String = "",

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
