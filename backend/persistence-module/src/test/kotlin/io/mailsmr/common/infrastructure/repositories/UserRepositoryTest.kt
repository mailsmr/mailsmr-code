package io.mailsmr.common.infrastructure.repositories

import io.mailsmr.common.infrastructure.entities.User
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.transaction.annotation.Transactional

@Transactional
@DataJpaTest
internal class UserRepositoryTest {

    @Autowired
    private lateinit var userRepository: UserRepository

   @Test
   fun findByUsername_shouldFindUser_ifExists() {
       // arrange
       val username = "TestUser"
       val user = User(
           username, "test@test.com", "passwordWithHash", "masterSecret"
       )
       userRepository.saveAndFlush(user)

       // act
       val foundUser = userRepository.findByUsername(username)

       // assert
       assertEquals(user, foundUser)
   }

    @Test
    fun findByUsername_shouldReturnNull_asItShouldBeCaseSensitive() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.findByUsername(username.toLowerCase())

        // assert
        assertNull(foundUser)
    }

    @Test
    fun findByUsername_shouldReturnNull_ifNotFound() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.findByUsername("SomeRandomUsername")

        // assert
        assertNull(foundUser)
    }

    @Test
    fun findByUsername_shouldReturnNull_ifUsernameIsEmpty() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.findByUsername("")

        // assert
        assertNull(foundUser)
    }


    @Test
    fun existsByUsername_shouldFindUserIfExists() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.existsByUsername(username)

        // assert
        assertTrue(foundUser)
    }

    @Test
    fun existsByUsername_shouldBeCaseSensitive() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.existsByUsername(username.toLowerCase())

        // assert
        assertFalse(foundUser)
    }

    @Test
    fun existsByUsername_shouldReturnFalse_IfNotFound() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.existsByUsername("SomeRandomUsername")

        // assert
        assertFalse(foundUser)
    }

    @Test
    fun existsByUsername_shouldReturnFalse_IfUsernameIsEmpty() {
        // arrange
        val username = "TestUser"
        val user = User(
            username, "test@test.com", "passwordWithHash", "masterSecret"
        )
        userRepository.saveAndFlush(user)

        // act
        val foundUser = userRepository.existsByUsername("")

        // assert
        assertFalse(foundUser)
    }

}
