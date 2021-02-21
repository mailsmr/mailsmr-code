package io.mailsmr.application

import io.mailsmr.common.infrastructure.entities.User
import io.mailsmr.common.infrastructure.repositories.UserRepository
import io.mailsmr.errors.InvalidPasswordException
import io.mailsmr.interfaces.rest.dtos.PatchUserRequestDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.dao.DuplicateKeyException
import java.security.Principal
import javax.servlet.http.HttpServletRequest
import javax.transaction.Transactional


@Transactional
@SpringBootTest
internal class UserServiceTest {

    @Autowired
    private lateinit var userService: UserService

    @Autowired
    private lateinit var userRepository: UserRepository


    @Test
    fun getUserFromHttpServletRequest_returnsUserIfHeExists() {
        // arrange
        val user = userService.createUser("TestUser", "password", "test@test.com")

        val request: HttpServletRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.userPrincipal).thenReturn(Principal { "TestUser" })

        // act
        val userUnderTest = userService.getUserFromHttpServletRequest(request)

        // assert
        assertEquals(user, userUnderTest)
    }

    @Test
    fun getUserFromHttpServletRequest_returnsNullIfUserDoesNotExists() {
        // arrange
        userService.createUser("TestUser", "password", "test@test.com")

        val request: HttpServletRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.userPrincipal).thenReturn(Principal { "NullUser" })

        // act
        val userUnderTest = userService.getUserFromHttpServletRequest(request)

        // assert
        assertNull(userUnderTest)
    }

    @Test
    fun getUserFromHttpServletRequest() {
        // arrange
        val user = userService.createUser("TestUser", "password", "test@test.com")

        val request: HttpServletRequest = Mockito.mock(HttpServletRequest::class.java)
        Mockito.`when`(request.userPrincipal).thenReturn(Principal { "TestUser" })

        // act
        val userUnderTest = userService.getUserFromHttpServletRequest(request)

        // assert
        assertEquals(user, userUnderTest)
    }

    @Test
    fun userExists_true_ifExactMatch() {
        // arrange
        userService.createUser("TestUser", "password", "test@test.com")

        // act
        val userExists = userService.userExists("TestUser")

        // assert
        assertTrue(userExists)
    }

    @Test
    fun userExists_false_ifNotExactMatch() {
        // arrange
        userService.createUser("TestUser", "password", "test@test.com")

        // act
        val userExists = userService.userExists("TestUser2")

        // assert
        assertFalse(userExists)
    }

    @Test
    fun createUser_shouldCreateUser_ifUsernameIsNotTaken() {
        // arrange, act & assert
        assertDoesNotThrow { userService.createUser("TestUser", "password", "test@test.com") }
    }

    @Test
    fun createUser_shouldFail_ifUsernameIsTaken() {
        // arrange
        userService.createUser("TestUser", "password", "test@test.com")

        // act & assert
        assertThrows(DuplicateKeyException::class.java) {
            userService.createUser(
                "TestUser",
                "password",
                "test@test.com"
            )
        }
    }

    @Test
    fun createUser_shouldCreateUsersWithDifferent64CharactersLongMasterEncryptionKey() {
        // arrange & act
        val user1 = userService.createUser("TestUser1", "password", "test@test.com")
        val user2 = userService.createUser("TestUser2", "password", "test@test.com")

        // assert
        assertNotEquals(user1, user2)

        assertEquals(64, user1.masterKeyEncryptionSecret.length)
        assertEquals(64, user2.masterKeyEncryptionSecret.length)

        assertNotEquals(user1.masterKeyEncryptionSecret, user2.masterKeyEncryptionSecret)
    }

    @Test
    fun createUser_shouldCreateUserWithPasswordHashIncludingSalt_bcrypt() {
        // arrange & act
        val user = userService.createUser("TestUser", "password", "test@test.com")

        // assert
        assertPasswordHashWithSaltMeetsRequirements(user)
    }

    @Test
    fun createUser_shouldCreateUsersWithDifferentPasswordHashIncludingSaltForSamePassword() {
        // arrange & act
        val user1 = userService.createUser("TestUser1", "password", "test@test.com")
        val user2 = userService.createUser("TestUser2", "password", "test@test.com")

        // assert
        assertNotEquals(user1, user2)
        assertNotEquals(user1.passwordHashWithSalt, user2.passwordHashWithSalt)
    }

    @Test
    fun createUser_shouldCreateUsersWithCorrectData() {
        // arrange & act
        val username = "TestUser"
        val recoveryEmailAddress = "test@test.com"
        val user = userService.createUser(username, "password", recoveryEmailAddress)

        // assert
        assertNotEquals(user.id, 0)
        assertEquals(user.username, username)
        assertEquals(user.recoveryEmailAddress, recoveryEmailAddress)
    }

    @Test
    fun createUser_shouldStoreTheCreatedUserToTheDatabase() {
        // arrange
        val username = "TestUser"
        val createdUser = userService.createUser(username, "password", "test@test.com")

        // act
        val userFromDatabase = userRepository.findByUsername(username)

        // assert
        assertEquals(createdUser, userFromDatabase)
    }

    @Test
    fun deleteUser_shouldDeleteUserIfHeExists() {
        // arrange
        val user = userService.createUser("TestUser", "password", "test@test.com")

        // act
        userService.deleteUser(user)

        // assert
        assertFalse(userRepository.existsByUsername(user.username))
    }

    @Test
    fun deleteUser_shouldDoNothingIfUserIsAlreadyDeleted() {
        // arrange
        val user = userService.createUser("TestUser", "password", "test@test.com")
        userService.deleteUser(user)

        // act & assert
        assertDoesNotThrow { userService.deleteUser(user) }
        assertFalse(userRepository.existsByUsername(user.username))
    }

    @Test
    fun patchUser_shouldPatchOnlyUsername_ifOnlyUsernameIsSet() {
        // arrange
        val initialUsername = "TestUser"
        val initialRecoveryEmailAddress = "test@test.com"
        val user = userService.createUser(initialUsername, "password", initialRecoveryEmailAddress)

        val prePatchPasswordHash = user.passwordHashWithSalt
        val prePatchMasterKeySecret = user.masterKeyEncryptionSecret

        val patchedUsername = "ChangedUser"
        val userPatch = PatchUserRequestDto(username = patchedUsername)

        // act
        userService.patchUser(user, userPatch)

        // assert
        assertEquals(userPatch.username, user.username)
        assertFalse(userRepository.existsByUsername(initialUsername))
        assertTrue(userRepository.existsByUsername(patchedUsername))

        assertNotEquals(initialUsername, user.username)
        assertEquals(initialRecoveryEmailAddress, user.recoveryEmailAddress)
        assertEquals(prePatchPasswordHash, user.passwordHashWithSalt)
        assertEquals(prePatchMasterKeySecret, user.masterKeyEncryptionSecret)
    }

    @Test
    fun patchUser_shouldPatchOnlyRecoveryEmail_ifOnlyRecoveryEmailIsSet() {
        // arrange
        val initialUsername = "TestUser"
        val initialRecoveryEmailAddress = "test@test.com"
        val user = userService.createUser(initialUsername, "password", initialRecoveryEmailAddress)

        val prePatchPasswordHash = user.passwordHashWithSalt
        val prePatchMasterKeySecret = user.masterKeyEncryptionSecret

        val patchedRecoveryEmail = "changed@test.com"
        val userPatch = PatchUserRequestDto(recoveryEmailAddress = patchedRecoveryEmail)

        // act
        userService.patchUser(user, userPatch)

        // assert
        assertEquals(user.recoveryEmailAddress, userPatch.recoveryEmailAddress)

        assertEquals(initialUsername, user.username)
        assertNotEquals(initialRecoveryEmailAddress, user.recoveryEmailAddress)
        assertEquals(prePatchPasswordHash, user.passwordHashWithSalt)
        assertEquals(prePatchMasterKeySecret, user.masterKeyEncryptionSecret)
    }

    @Test
    fun patchUser_shouldPatchOnlyPassword_ifOnlyPasswordIsSet() {
        // arrange
        val initialUsername = "TestUser"
        val initialRecoveryEmailAddress = "test@test.com"
        val initialPassword = "password"
        val user = userService.createUser(initialUsername, initialPassword, initialRecoveryEmailAddress)

        val prePatchPasswordHash = user.passwordHashWithSalt
        val prePatchMasterKeySecret = user.masterKeyEncryptionSecret

        val patchedPassword = "patchedPassword"
        val userPatch = PatchUserRequestDto(
            passwordChange = PatchUserRequestDto.PatchUserRequestDtoPasswordChange(
                initialPassword,
                patchedPassword
            )
        )

        // act
        userService.patchUser(user, userPatch)

        // assert
        assertEquals(initialUsername, user.username)
        assertEquals(initialRecoveryEmailAddress, user.recoveryEmailAddress)
        assertNotEquals(prePatchPasswordHash, user.passwordHashWithSalt)
        assertNotEquals(patchedPassword, user.passwordHashWithSalt)
        assertEquals(prePatchMasterKeySecret, user.masterKeyEncryptionSecret)

        assertPasswordHashWithSaltMeetsRequirements(user)
    }

    @Test
    fun patchUser_shouldPatchNothing_ifNothingIsSet() {
        // arrange
        val initialUsername = "TestUser"
        val initialRecoveryEmailAddress = "test@test.com"
        val user = userService.createUser(initialUsername, "password", initialRecoveryEmailAddress)

        val prePatchPasswordHash = user.passwordHashWithSalt
        val prePatchMasterKeySecret = user.masterKeyEncryptionSecret

        val userPatch = PatchUserRequestDto()

        // act
        userService.patchUser(user, userPatch)

        // assert
        assertEquals(initialUsername, user.username)
        assertEquals(initialRecoveryEmailAddress, user.recoveryEmailAddress)
        assertEquals(prePatchPasswordHash, user.passwordHashWithSalt)
        assertEquals(prePatchMasterKeySecret, user.masterKeyEncryptionSecret)
    }

    @Test
    fun patchUser_shouldThrow_ifPreviousPasswordDoesNotMatch() {
        // arrange
        val initialUsername = "TestUser"
        val initialRecoveryEmailAddress = "test@test.com"
        val initialPassword = "password"
        val user = userService.createUser(initialUsername, initialPassword, initialRecoveryEmailAddress)

        val patchedPassword = "patchedPassword"
        val userPatch = PatchUserRequestDto(
            passwordChange = PatchUserRequestDto.PatchUserRequestDtoPasswordChange(
                initialPassword + "error",
                patchedPassword
            )
        )

        // act && assert
        assertThrows(InvalidPasswordException::class.java) { userService.patchUser(user, userPatch) }
    }


    private fun assertPasswordHashWithSaltMeetsRequirements(user: User) {
        assertTrue(user.passwordHashWithSalt.startsWith("$2a$10$"))
        assertTrue(user.passwordHashWithSalt.count { c -> c == '$' } == 3)
        assertEquals(60, user.passwordHashWithSalt.length)
    }

}
