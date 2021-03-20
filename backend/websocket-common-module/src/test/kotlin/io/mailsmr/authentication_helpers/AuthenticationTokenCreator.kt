package io.mailsmr.authentication_helpers

import org.springframework.security.authentication.AbstractAuthenticationToken
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.User

object AuthenticationTokenCreator {
    fun create(): AbstractAuthenticationToken {
        val grantedAuthorities = listOf<GrantedAuthority>()

        return UsernamePasswordAuthenticationToken(
            User
                .withUsername("TestUser")
                .password("testPassword")
                .authorities(grantedAuthorities)
                .build(),
            null,
            grantedAuthorities
        )
    }
}
