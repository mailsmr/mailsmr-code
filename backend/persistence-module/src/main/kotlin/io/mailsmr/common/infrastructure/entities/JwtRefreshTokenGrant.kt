package io.mailsmr.common.infrastructure.entities

import org.hibernate.annotations.NaturalId
import org.hibernate.annotations.OnDelete
import org.hibernate.annotations.OnDeleteAction
import java.time.LocalDateTime
import javax.persistence.*
import javax.validation.constraints.Pattern

@Entity(name = "jwt_refresh_token_grants")
data class JwtRefreshTokenGrant(
    @NaturalId
    @field:Pattern(regexp = "^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}\$")
    val jti: String,

    @Column(nullable = true)
    val description: String? = null,

    @Column(nullable = false)
    val expirationDateTime: LocalDateTime,

    @Column(nullable = false, updatable = false)
    val creationDateTime: LocalDateTime,

    @ManyToOne(fetch = FetchType.LAZY)
    @field:OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "fk_user")
    val user: User,
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0L
}
