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

package backend

import backend.utils.CustomOAuth2AuthorizationHelper
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jooq.JooqAutoConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.server.resource.authentication.JwtBearerTokenAuthenticationConverter
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.web.servlet.config.annotation.CorsRegistry
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Configuration
@EnableTransactionManagement
@ImportAutoConfiguration(JooqAutoConfiguration::class)
@EnableFeignClients
class Configuration

@EnableWebSecurity
@Profile("security")
class SecurityConfig(
    private val oauthHelper: CustomOAuth2AuthorizationHelper,
    private val clientRegistrationRepository: ClientRegistrationRepository
) : WebSecurityConfigurerAdapter() {


    override fun configure(http: HttpSecurity) {
        http
            .csrf().disable()
            .cors().and()
            .headers().frameOptions().disable()
            .and()
            .authorizeRequests()
            .antMatchers("/oauth/token").permitAll()
            .anyRequest().authenticated()
            .and()
            .oauth2Login()
            .clientRegistrationRepository(clientRegistrationRepository)
            .authorizedClientService(oauthHelper)
            .successHandler(oauthHelper)
            .and()
            .sessionManagement()
            .sessionCreationPolicy(SessionCreationPolicy.NEVER)
            .and()
            .oauth2ResourceServer()
            .jwt()
            .jwtAuthenticationConverter(JwtBearerTokenAuthenticationConverter())
    }

}

@Configuration
@EnableWebMvc
class WebConfig : WebMvcConfigurer {

    override fun addCorsMappings(registry: CorsRegistry) {
        registry.addMapping("/**")
            .allowedOrigins("*")
            .allowedMethods("GET", "POST", "PUT", "DELETE")
            .allowedHeaders("authorization", "content-type", "xsrf-token")
            .exposedHeaders("xsrf-token")
            .allowCredentials(false)
            .maxAge(3600)
    }
}