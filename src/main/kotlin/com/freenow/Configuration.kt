/*******************************************************************************
 * Copyright (c) 2020.
 * This program is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License
 * as published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 ******************************************************************************/

package com.freenow

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter
import org.springframework.security.web.DefaultRedirectStrategy
import org.springframework.security.web.RedirectStrategy
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer
import java.time.Instant
import java.util.*
import javax.servlet.http.Cookie


/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Configuration
@EnableTransactionManagement
@ImportAutoConfiguration(JooqAutoConfiguration::class)
@EnableFeignClients
class Configuration

@EnableWebSecurity
class SecurityConfig(
    private val clientRegistrationRepository: ClientRegistrationRepository
) : WebSecurityConfigurerAdapter() {

    private val logger = KotlinLogging.logger {}

    override fun configure(http: HttpSecurity) {
        val successHandler = SimpleUrlAuthenticationSuccessHandler()
        successHandler.setUseReferer(true)
        val redirectStrategy = DefaultRedirectStrategy()
        http
            .csrf().disable()
            .cors().and()
            .authorizeRequests()
            .anyRequest()
            .authenticated()
            .and()
            .oauth2Login()
            .clientRegistrationRepository(clientRegistrationRepository)
            .authorizedClientService(InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository))

            .successHandler(AuthenticationSuccessHandler { request, response, authentication ->
                logger.debug { "$authentication" }
                val token = Jwts.builder()
                    .setSubject("1234567890")
                    .setId("8cd78dc9-f022-4308-8a2a-c2fced954117")
                    .setIssuedAt(Date.from(Instant.ofEpochSecond(1597957712)))
                    .setExpiration(Date.from(Instant.ofEpochSecond(1597961312)))
                    .claim("name", "John Doe")
                    .claim("admin", true)
                    // .signWith(SignatureAlgorithm.HS256, "UTF-8")
                    .compact()
                response.addCookie(Cookie("JWT", token))
                response.setTrailerFields {
                    mapOf(
                        "token" to token
                    )
                }
                redirectStrategy.sendRedirect(
                    request, response,
                    request.getHeader("Referer")
                )


                logger.debug { token }
            })
            .and()
            //.sessionManagement()
            //.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            //.and()
            .oauth2ResourceServer()
            .jwt()
            .decoder { token ->
                Jwt.withTokenValue(token).build()
            }
            .jwtAuthenticationConverter(JwtBearerTokenAuthenticationConverter())
    }

}
