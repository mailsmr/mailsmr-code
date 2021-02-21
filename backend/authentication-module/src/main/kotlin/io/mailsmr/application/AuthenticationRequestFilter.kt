package io.mailsmr.application

import io.mailsmr.domain.JwtTokenFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@Component
class AuthenticationRequestFilter(
    private val authenticationUserDetailsService: AuthenticationUserDetailsService,
    private val jwtTokenFactory: JwtTokenFactory
) : OncePerRequestFilter() {


    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, chain: FilterChain) {
        setAuthenticationForContext(request)
        chain.doFilter(request, response)
    }

    private fun setAuthenticationForContext(request: HttpServletRequest) {
        val accessToken = jwtTokenFactory.fromRequest(request)

        if (hasAuthenticationInformation() &&
            accessToken != null && accessToken.isViable() && !accessToken.isExpired() && accessToken.isUsernameSet()
        ) {
            val userDetails: UserDetails =
                authenticationUserDetailsService.loadUserByUsername(accessToken.getUsername()!!)

            val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )

            usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)

            SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
        }
    }


    private fun hasAuthenticationInformation() = SecurityContextHolder.getContext().authentication == null
}
