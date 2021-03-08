package io.mailsmr.config

import io.mailsmr.application.JWTAuthenticationEntryPoint
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.AUTHENTICATION_BASE_PATH
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.POST__CREATE_AUTHENTICATION_TOKEN_PATH
import io.mailsmr.interfaces.rest.AuthenticationApiPaths.POST__USE_REFRESH_TOKEN_PATH
import io.mailsmr.interfaces.rest.UserApiPaths.USER_BASE_PATH
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
internal class WebSecurityConfig(
    private val jwtAuthenticationEntryPoint: JWTAuthenticationEntryPoint,
    private val authenticationRequestFilter: io.mailsmr.application.AuthenticationRequestFilter,
) : WebSecurityConfigurerAdapter() {

    override fun configure(httpSecurity: HttpSecurity) {
        httpSecurity
            .csrf().disable()
            .authorizeRequests()
            .antMatchers("/docs/**").permitAll()
            .antMatchers(
                HttpMethod.POST,
                "$AUTHENTICATION_BASE_PATH$POST__CREATE_AUTHENTICATION_TOKEN_PATH",
                "$AUTHENTICATION_BASE_PATH$POST__USE_REFRESH_TOKEN_PATH",
            ).permitAll() // authentication endpiont
            .antMatchers(HttpMethod.POST, USER_BASE_PATH).permitAll() // user endpoint
            .antMatchers("/ws/**").permitAll() // web socket TODO make more specific
            .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        httpSecurity.addFilterBefore(authenticationRequestFilter, UsernamePasswordAuthenticationFilter::class.java)
        httpSecurity.cors().configurationSource(corsConfigurationSource())
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowCredentials = true
        configuration.addAllowedOrigin("http://localhost:4200")
        configuration.addAllowedHeader("*")
        configuration.addAllowedMethod("*")
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
