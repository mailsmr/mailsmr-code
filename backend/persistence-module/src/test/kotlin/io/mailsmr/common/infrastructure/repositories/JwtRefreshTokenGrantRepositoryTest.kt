package io.mailsmr.common.infrastructure.repositories

import io.mailsmr.common.infrastructure.entities.JwtRefreshTokenGrant
import io.mailsmr.common.infrastructure.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import javax.persistence.EntityManager

@Transactional
@DataJpaTest
internal class JwtRefreshTokenGrantRepositoryTest {

    @Autowired
    private lateinit var jwtRefreshTokenGrantRepository: JwtRefreshTokenGrantRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    private lateinit var testUser: User

    @BeforeEach
    fun beforeEach() {
        testUser = User(
            "TestUser", "test@test.com", "passwordWithHash", "masterSecret"
        )
        entityManager.persist(testUser)
        entityManager.flush()
    }

   @Test
   fun findByUsername_shouldFindGrant_ifExists() {
       // arrange
       val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
       val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
           jti,
           null,
           LocalDateTime.now().plusDays(30),
           LocalDateTime.now(),
           testUser
       )

       jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

       // act
       val foundGrant = jwtRefreshTokenGrantRepository.findByJti(jti)

       // assert
       assertEquals(jwtRefreshTokenGrant, foundGrant)
   }

    @Test
    fun findByUsername_shouldReturnNull_ifNotFound() {
        // arrange
        val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
            jti,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

        // act
        val foundGrant = jwtRefreshTokenGrantRepository.findByJti("jti")

        // assert
        assertNull(foundGrant)
    }


    @Test
    fun existsByUsername_shouldFindGrant_ifExists() {
        // arrange
        val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
            jti,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

        // act
        val foundGrant = jwtRefreshTokenGrantRepository.existsByJti(jti)

        // assert
        assertTrue(foundGrant)
    }

    @Test
    fun existsByJti_shouldReturnNull_ifNotFound() {
        // arrange
        val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
            jti,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

        // act
        val foundGrant = jwtRefreshTokenGrantRepository.existsByJti("jti")

        // assert
        assertFalse(foundGrant)
    }

    @Test
    fun deleteByJti_shouldFindGrant_ifExists() {
        // arrange
        val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
            jti,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

        // act
        jwtRefreshTokenGrantRepository.deleteByJti(jti)
        jwtRefreshTokenGrantRepository.flush()

        // assert
        assertFalse(jwtRefreshTokenGrantRepository.existsByJti(jti))
    }

    @Test
    fun deleteByJti_shouldReturnNull_ifNotFound() {
        // arrange
        val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
            jti,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

        // act
        jwtRefreshTokenGrantRepository.deleteByJti("jti")
        jwtRefreshTokenGrantRepository.flush()

        // assert
        assertTrue(jwtRefreshTokenGrantRepository.existsByJti(jti))
    }

    @Test
    fun findAllByUser_shouldReturnOneGrant_ifOnlyHasOne() {
        // arrange
        val jti = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant = JwtRefreshTokenGrant(
            jti,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant)

        // act
        val allByUser = jwtRefreshTokenGrantRepository.findAllByUser(testUser)

        // assert
        assertTrue(allByUser.isNotEmpty())
        assertTrue(allByUser.size == 1)
        assertEquals(jwtRefreshTokenGrant, allByUser[0])
    }

    @Test
    fun findAllByUser_shouldReturnAllGrants_ifHasTwo() {
        // arrange
        val jti1 = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant1 = JwtRefreshTokenGrant(
            jti1,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        val jti2 = "12345678-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant2 = JwtRefreshTokenGrant(
            jti2,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        jwtRefreshTokenGrantRepository.save(jwtRefreshTokenGrant1)
        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant2)

        // act
        val allByUser = jwtRefreshTokenGrantRepository.findAllByUser(testUser)

        // assert
        assertTrue(allByUser.isNotEmpty())
        assertTrue(allByUser.size == 2)
    }

    @Test
    fun findAllByUser_shouldReturnOnlyTheGrantsOfThisUser() {
        // arrange
        val jti1 = "0a0a0a0a-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant1 = JwtRefreshTokenGrant(
            jti1,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser
        )

        val testUser2 = User(
            "TestUser2", "test@test.com", "passwordWithHash", "masterSecret"
        )
        entityManager.persist(testUser2)

        val jti2 = "12345678-1b1b-2c2c-3d3d-4e4e4e4e4e4e"
        val jwtRefreshTokenGrant2 = JwtRefreshTokenGrant(
            jti2,
            null,
            LocalDateTime.now().plusDays(30),
            LocalDateTime.now(),
            testUser2
        )

        jwtRefreshTokenGrantRepository.save(jwtRefreshTokenGrant1)
        jwtRefreshTokenGrantRepository.saveAndFlush(jwtRefreshTokenGrant2)

        // act
        val allByUser = jwtRefreshTokenGrantRepository.findAllByUser(testUser)

        // assert
        assertTrue(allByUser.isNotEmpty())
        assertTrue(allByUser.size == 1)
    }

    @Test
    fun findAllByUser_shouldReturnEmptyList_ifUserHasNone() {
        // arrange & act
        val allByUser = jwtRefreshTokenGrantRepository.findAllByUser(testUser)

        // assert
        assertTrue(allByUser.isEmpty())
    }



}
