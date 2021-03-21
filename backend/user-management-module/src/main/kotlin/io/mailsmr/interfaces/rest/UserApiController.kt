package io.mailsmr.interfaces.rest

import io.mailsmr.application.UserService
import io.mailsmr.interfaces.rest.dtos.NewUserRequestDto
import io.mailsmr.interfaces.rest.dtos.PatchUserRequestDto
import io.mailsmr.interfaces.rest.dtos.UserDto
import org.springframework.dao.DuplicateKeyException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import javax.servlet.http.HttpServletRequest


@RestController
internal class UserApiController(
    private val userService: UserService,
) : UserApi {
    override fun createUser(
        @RequestBody userDto: NewUserRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<String> {
        val (username, password, recoveryEmailAddress) = userDto

        return try {
            userService.createUser(username, password, recoveryEmailAddress)
            ResponseEntity.status(HttpStatus.CREATED).build()
        } catch (e: DuplicateKeyException) {
            ResponseEntity.status(HttpStatus.CONFLICT).body(e.localizedMessage)
        }
    }

    override fun getUser(request: HttpServletRequest): ResponseEntity<UserDto> {
        val user = userService.getUserFromHttpServletRequest(request)!! // checked by security context

        return ResponseEntity.status(HttpStatus.OK).body(UserDto.fromUser(user))
    }

    override fun deleteUser(request: HttpServletRequest): ResponseEntity<Void> {
        val user = userService.getUserFromHttpServletRequest(request)!! // checked by security context

        userService.deleteUser(user)

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build()
    }

    override fun patchUser(
        @RequestBody userDto: PatchUserRequestDto,
        request: HttpServletRequest
    ): ResponseEntity<UserDto> {
        val user = userService.getUserFromHttpServletRequest(request)!! // checked by security context

        userService.patchUser(user, userDto)

        return ResponseEntity.status(HttpStatus.OK).body(UserDto.fromUser(user))
    }
}
