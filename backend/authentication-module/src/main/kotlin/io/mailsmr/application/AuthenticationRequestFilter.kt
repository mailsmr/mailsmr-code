package io.mailsmr.application

import io.mailsmr.domain.JwtTokenFactoryService
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationRequestFilter(
    private val authenticationUserDetailsService: AuthenticationUserDetailsService,
    private val jwtTokenFactoryService: JwtTokenFactoryService
) : OncePerRequestFilter() {


    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        setAuthenticationForContext(request)
        chain.doFilter(request, response)
    }

    private fun setAuthenticationForContext(request: HttpServletRequest) {
        val accessToken = jwtTokenFactoryService.fromRequest(request)

        if (accessToken != null) request.setAttribute("expired", accessToken.isExpired())

        if (hasNoAuthenticationInformation()) {
            val abstractAuthenticationToken =
                authenticationUserDetailsService.getAuthenticationTokenFromAccessToken(accessToken)

            if (abstractAuthenticationToken != null) {
                abstractAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = abstractAuthenticationToken
            }
        }
    }


    private fun hasNoAuthenticationInformation() = SecurityContextHolder.getContext().authentication == null
}
