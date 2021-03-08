package io.mailsmr.application

import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class JWTAuthenticationEntryPoint : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val isExpired = request.getAttribute("expired") as Boolean? ?: false

        if (isExpired) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access token is expired")
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }

    }
}
