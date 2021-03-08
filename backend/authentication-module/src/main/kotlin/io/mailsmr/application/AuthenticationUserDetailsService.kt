package io.mailsmr.application

import io.mailsmr.common.infrastructure.entities.User
import io.mailsmr.common.infrastructure.repositories.UserRepository
import io.mailsmr.domain.JwtAccessToken
import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service

@Service
class AuthenticationUserDetailsService(
    private val userRepository: UserRepository
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {
        val user: User = userRepository.findByUsername(username)
            ?: throw UsernameNotFoundException("User not found with username: $username")

        return org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.passwordHashWithSalt)
            .authorities(listOf<GrantedAuthority>())
            .build();
    }

    fun getAuthenticationTokenFromAccessToken(accessToken: JwtAccessToken?): AbstractAuthenticationToken? {
        if (accessToken != null && accessToken.isViable() && !accessToken.isExpired() && accessToken.isUsernameSet()
        ) {
            val userDetails: UserDetails = loadUserByUsername(accessToken.getUsername()!!)

            return UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.authorities
            )
        }
        return null
    }
}
