package io.mailsmr.common.infrastructure.entities

import io.mailsmr.common.infrastructure.repositories.UserRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.transaction.annotation.Transactional
import javax.validation.ConstraintViolationException

@Transactional
@DataJpaTest
internal class UserTest {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun constructor_shouldCreateUser_withId0() {
        // arrange & act
        val username = "TestUser"
        val recoveryEmailAddress = "test@test.com"
        val passwordHashWithSalt = "passwordWithHash"
        val masterKeyEncryptionSecret = "masterSecret"
        val user = User(
            username, recoveryEmailAddress, passwordHashWithSalt, masterKeyEncryptionSecret
        )

        // assert
        assertTrue(0L == user.id)
        assertEquals(username, user.username)
        assertEquals(recoveryEmailAddress, user.recoveryEmailAddress)
        assertEquals(passwordHashWithSalt, user.passwordHashWithSalt)
        assertEquals(masterKeyEncryptionSecret, user.masterKeyEncryptionSecret)
    }

    @Test
    fun constructor_shouldCreateUser_withSetIdWhenSavedToDatabase() {
        // arrange & act
        val username = "TestUser"
        val recoveryEmailAddress = "test@test.com"
        val passwordHashWithSalt = "passwordWithHash"
        val masterKeyEncryptionSecret = "masterSecret"
        val user = User(
            username, recoveryEmailAddress, passwordHashWithSalt, masterKeyEncryptionSecret
        )

        userRepository.saveAndFlush(user)

        // assert
        assertFalse(0L == user.id)
        assertEquals(username, user.username)
        assertEquals(recoveryEmailAddress, user.recoveryEmailAddress)
        assertEquals(passwordHashWithSalt, user.passwordHashWithSalt)
        assertEquals(masterKeyEncryptionSecret, user.masterKeyEncryptionSecret)
    }

    @Test
    fun storingUserToDatabase_shouldNotAllowTwoUsersWithSameUsername() {
        // arrange & act
        val username = "TestUser"
        val recoveryEmailAddress = "test@test.com"
        val passwordHashWithSalt = "passwordWithHash"
        val masterKeyEncryptionSecret = "masterSecret"
        val user1 = User(
            username, recoveryEmailAddress, passwordHashWithSalt, masterKeyEncryptionSecret
        )

        val user2 = User(
            username, recoveryEmailAddress, passwordHashWithSalt, masterKeyEncryptionSecret
        )

        userRepository.saveAndFlush(user1)

        // assert
        assertThrows(DataIntegrityViolationException::class.java) { userRepository.saveAndFlush(user2) }
    }

    @Test
    fun storingUserToDatabase_shouldNotAllowUserWithTooShortUsername() {
        // arrange & act
        val username = "T"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )

        // assert
        assertThrows(ConstraintViolationException::class.java) { userRepository.saveAndFlush(user) }
    }

    @Test
    fun storingUserToDatabase_shouldNotAllowUserWithEmptyUsername() {
        // arrange & act
        val username = ""
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )

        // assert
        assertThrows(ConstraintViolationException::class.java) { userRepository.saveAndFlush(user) }
    }

    @Test
    fun storingUserToDatabase_shouldAllowUserWithMinimalLengthUsername() {
        // arrange & act
        val username = "T1"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )

        // assert
        assertDoesNotThrow { userRepository.saveAndFlush(user) }
    }


}
