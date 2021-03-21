package io.mailsmr.application

import io.mailsmr.common.infrastructure.entities.User
import io.mailsmr.common.infrastructure.repositories.UserRepository
import io.mailsmr.errors.InvalidPasswordException
import io.mailsmr.interfaces.rest.dtos.PatchUserRequestDto
import org.apache.commons.lang3.RandomStringUtils
import org.springframework.dao.DuplicateKeyException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import javax.servlet.http.HttpServletRequest

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {
    fun getUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun getUserFromHttpServletRequest(request: HttpServletRequest): User? {
        val username = request.userPrincipal.name
        return getUserByUsername(username)
    }


    fun userExists(username: String): Boolean {
        return userRepository.existsByUsername(username)
    }

    fun createUser(username: String, password: String, recoveryEmailAddress: String): User {
        if (userExists(username)) throw DuplicateKeyException("Username already taken")

        val passwordHashWithSalt = passwordEncoder.encode(password)

        val masterKeyEncryptionSecret: String = RandomStringUtils.random(64, true, true)


        val user = User(
            username = username,
            recoveryEmailAddress = recoveryEmailAddress,
            passwordHashWithSalt = passwordHashWithSalt,
            masterKeyEncryptionSecret = masterKeyEncryptionSecret
        )

        userRepository.saveAndFlush(user)
        return user
    }

    fun deleteUser(user: User) {
        userRepository.delete(user)
        userRepository.flush()
    }

    fun patchUser(user: User, patchUserDto: PatchUserRequestDto) {
        user.username = patchUserDto.username ?: user.username
        user.recoveryEmailAddress = patchUserDto.recoveryEmailAddress ?: user.recoveryEmailAddress

        if (patchUserDto.passwordChange != null) {
            val (previousPassword, newPassword) = patchUserDto.passwordChange
            if (passwordEncoder.matches(previousPassword, user.passwordHashWithSalt)) {
                user.passwordHashWithSalt = passwordEncoder.encode(newPassword)
            } else {
                throw InvalidPasswordException("Provided previous password does not match the current.")
            }
        }

        userRepository.saveAndFlush(user)
    }


}
