package io.mailsmr.interfaces.rest

import io.mailsmr.interfaces.rest.constants.OpenApiConfigConstants.SECURITY_SCHEME_NAME
import io.mailsmr.interfaces.rest.UserApiPaths.DELETE__DELETE_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.GET__GET_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.PATCH__PATCH_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.POST__CREATE_USER_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.USER_BASE_PATH
import io.mailsmr.interfaces.rest.dtos.NewUserRequestDto
import io.mailsmr.interfaces.rest.dtos.PatchUserRequestDto
import io.mailsmr.interfaces.rest.dtos.UserDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest

@Tag(name = "user")
@RequestMapping(value = [USER_BASE_PATH])
internal interface UserApi {
    @Operation(
        summary = "Create a new user",
        operationId = "createUser"
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "201", description = "User created"),
            ApiResponse(responseCode = "409", description = "Username already taken"),
            ApiResponse(
                responseCode = "422",
                description = "Something is wrong with the username, recovery email address or the password."
            )
        ]
    )
    @PostMapping(value = [POST__CREATE_USER_PATH])
    fun createUser(@RequestBody userDto: NewUserRequestDto, request: HttpServletRequest): ResponseEntity<String>


    @Operation(
        summary = "Get user information",
        operationId = "getUser",
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME)],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User data"),
            ApiResponse(responseCode = "422", description = "Username is missing from token")
        ]
    )
    @GetMapping(value = [GET__GET_USER_PATH])
    fun getUser(request: HttpServletRequest): ResponseEntity<UserDto>


    @Operation(
        summary = "Deletes the user",
        operationId = "deleteUser",
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME)],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "204", description = "User deleted successfully"),
            ApiResponse(responseCode = "422", description = "Username is missing from token")
        ]
    )
    @DeleteMapping(value = [DELETE__DELETE_USER_PATH])
    fun deleteUser(request: HttpServletRequest): ResponseEntity<Void>


    @Operation(
        summary = "Updates the users data",
        operationId = "patchUser",
        security = [SecurityRequirement(name = SECURITY_SCHEME_NAME)],
    )
    @ApiResponses(
        value = [
            ApiResponse(responseCode = "200", description = "User deleted successfully"),
            ApiResponse(responseCode = "422", description = "Username is missing from token")
        ]
    )
    @PatchMapping(value = [PATCH__PATCH_USER_PATH])
    fun patchUser(@RequestBody userDto: PatchUserRequestDto, request: HttpServletRequest): ResponseEntity<UserDto>
}
