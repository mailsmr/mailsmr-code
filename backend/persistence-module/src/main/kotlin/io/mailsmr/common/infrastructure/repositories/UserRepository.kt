package io.mailsmr.common.infrastructure.repositories

import io.mailsmr.common.infrastructure.entities.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?

    fun existsByUsername(username: String): Boolean
}
