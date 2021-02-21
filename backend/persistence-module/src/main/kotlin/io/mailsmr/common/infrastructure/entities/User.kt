package io.mailsmr.common.infrastructure.entities

import javax.persistence.*
import javax.validation.constraints.Size

@Entity(name = "users")
data class User(
    @field:Size(min = 2)
    @Column(nullable = false, unique = true)
    var username: String,

    @Column(nullable = false)
    var recoveryEmailAddress: String,

    @Column(nullable = false)
    var passwordHashWithSalt: String,

    @Column(nullable = false)
    var masterKeyEncryptionSecret: String,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L

    @Version
    private val version: Long = 0L
}
