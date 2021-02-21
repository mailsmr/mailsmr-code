package io.mailsmr.common.infrastructure.entities

import io.mailsmr.common.infrastructure.repositories.JwtRefreshTokenGrantRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.persistence.EntityManager
import javax.validation.ConstraintViolationException

@Transactional
@DataJpaTest
internal class JwtRefreshTokenGrantTest {
    @Autowired
    private lateinit var jwtRefreshTokenGrantRepository: JwtRefreshTokenGrantRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Test
    fun constructor_shouldCreateTokenGrant() {
        // arrange & act
        val user = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )

        entityManager.persist(user)


        val jtiString = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val description = "description"
        val expirationDateTime = LocalDateTime.now().plusDays(30)
        val creationDateTime = LocalDateTime.now()
        val tokenGrant = JwtRefreshTokenGrant(
            jtiString, description, expirationDateTime, creationDateTime, user
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(tokenGrant)

        // assert
        assertEquals(jtiString, tokenGrant.jti)
        assertEquals(description, tokenGrant.description)
        assertEquals(expirationDateTime, tokenGrant.expirationDateTime)
        assertEquals(user, tokenGrant.user)
        assertEquals(
            LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES),
            tokenGrant.creationDateTime.truncatedTo(ChronoUnit.MINUTES)
        )
    }

    @Test
    fun constructor_shouldCreateTokenGrant_ifDescriptionIsNull() {
        // arrange & act
        val user = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )

        entityManager.persist(user)

        val jtiString = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val expirationDateTime = LocalDateTime.now().plusDays(30)
        val creationDateTime = LocalDateTime.now()
        val tokenGrant = JwtRefreshTokenGrant(
            jtiString, null, expirationDateTime, creationDateTime, user
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(tokenGrant)

        // assert
        assertEquals(jtiString, tokenGrant.jti)
        assertEquals(null, tokenGrant.description)
        assertEquals(expirationDateTime, tokenGrant.expirationDateTime)
        assertEquals(user, tokenGrant.user)
        assertEquals(
            LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES),
            tokenGrant.creationDateTime.truncatedTo(ChronoUnit.MINUTES)
        )
    }

    @Test
    fun constructor_shouldThrow_ifJtiDoesNotMatchRequiredPattern() {
        // arrange
        val user = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )

        entityManager.persist(user)

        val jtiString = "idOfJwtToken1"
        val description = "description"
        val expirationDateTime = LocalDateTime.now().plusDays(30)
        val creationDateTime = LocalDateTime.now()
        val tokenGrant = JwtRefreshTokenGrant(
            jtiString, description, expirationDateTime, creationDateTime, user
        )

        // act && assert
        assertThrows(ConstraintViolationException::class.java) {
            jwtRefreshTokenGrantRepository.saveAndFlush(
                tokenGrant
            )
        }
    }

    @Test
    fun constructor_shouldThrow_ifJtiIsDuplicate() {
        // arrange
        val user = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )

        entityManager.persist(user)

        val jtiString = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val expirationDateTime = LocalDateTime.now().plusDays(30)
        val creationDateTime = LocalDateTime.now()
        val tokenGrant1 = JwtRefreshTokenGrant(
            jtiString, null, expirationDateTime, creationDateTime, user
        )
        jwtRefreshTokenGrantRepository.saveAndFlush(tokenGrant1)


        val tokenGrant2 = JwtRefreshTokenGrant(
            jtiString, null, expirationDateTime, creationDateTime, user
        )

        // act && assert
        assertThrows(DataIntegrityViolationException::class.java) {
            jwtRefreshTokenGrantRepository.saveAndFlush(tokenGrant2)
        }
    }

    @Test
    fun deletingTheUser_shouldDeleteAllItsAssociatedJtwRefreshTokenGrants() {
        // arrange
        val user = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )

        entityManager.persist(user)

        val userId = user.id

        val jtiString1 = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val expirationDateTime = LocalDateTime.now().plusDays(30)
        val creationDateTime = LocalDateTime.now()

        val jtiString2 = "0a0a0a0a-1b1b-2c2c-3d3d-5e5e5e5e5e5e"

        entityManager.persist(
            JwtRefreshTokenGrant(
                jtiString1, null, expirationDateTime, creationDateTime, user
            )
        )
        entityManager.persist(
            JwtRefreshTokenGrant(
                jtiString2, null, expirationDateTime, creationDateTime, user
            )
        )

        entityManager.flush()

        // act
        entityManager.remove(user)
        entityManager.flush()
        entityManager.clear()

        // assert
        assertNull(entityManager.find(User::class.java, userId))

        assertFalse(jwtRefreshTokenGrantRepository.existsByJti(jtiString1), "jtiString1")
        assertFalse(jwtRefreshTokenGrantRepository.existsByJti(jtiString2), "jtiString2")
    }

    @Test
    fun deletingTheToken_shouldNotDeleteUser() {
        // arrange
        val user = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )

        entityManager.persist(user)
        val userId = user.id

        val jtiString = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val expirationDateTime = LocalDateTime.now().plusDays(30)
        val creationDateTime = LocalDateTime.now()
        val tokenGrant1 = JwtRefreshTokenGrant(
            jtiString, "Test1", expirationDateTime, creationDateTime, user
        )
        jwtRefreshTokenGrantRepository.saveAndFlush(tokenGrant1)


        // act
        entityManager.remove(tokenGrant1)
        entityManager.flush()
        entityManager.clear()

        // assert
        assertNotNull(entityManager.find(User::class.java, userId))
    }
}
