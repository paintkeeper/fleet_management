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

package backend.utils

import io.fusionauth.jwt.Signer
import io.fusionauth.jwt.Verifier
import io.fusionauth.jwt.domain.JWT
import io.fusionauth.jwt.hmac.HMACSigner
import io.fusionauth.jwt.hmac.HMACVerifier
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.client.InMemoryOAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestRedirectFilter
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.util.function.Consumer
import java.util.function.Function
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Component
@Profile("security")
class CustomOAuth2AuthorizationHelper(
    clientRegistrationRepository: ClientRegistrationRepository,
    @Value("\${oauth2.secret-key}")
    secretKey: String,
    @Value("\${oauth2.default-scope}")
    private val defaultScope: String,
    @Value("\${oauth2.issuer}")
    private val issuer: String,
    @Value("\${oauth2.max-age}")
    private val maxAge: Long
) : OAuth2AuthorizationRequestResolver,
    OAuth2AuthorizedClientService,
    AuthenticationSuccessHandler,
    JwtDecoder {

    private val logger = KotlinLogging.logger {}

    private val requestHandler = DefaultOAuth2AuthorizationRequestResolver(
        clientRegistrationRepository,
        OAuth2AuthorizationRequestRedirectFilter.DEFAULT_AUTHORIZATION_REQUEST_BASE_URI
    )

    private val signer: Signer = HMACSigner.newSHA384Signer(secretKey)
    private val verifiers = mapOf(signer.algorithm.name to HMACVerifier.newVerifier(secretKey))
    private val decoder = JWT.getDecoder()

    private val authorizationClientService = InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository)

    private val idGenerator = IdGenerator()

    override fun resolve(request: HttpServletRequest?): OAuth2AuthorizationRequest {
        return requestHandler.resolve(request)
    }

    override fun resolve(request: HttpServletRequest?, clientRegistrationId: String?): OAuth2AuthorizationRequest {
        val oauthReq = requestHandler.resolve(request, clientRegistrationId)
        return oauthReq
    }

    override fun <T : OAuth2AuthorizedClient> loadAuthorizedClient(
        clientRegistrationId: String?,
        principalName: String?
    ): T {
        return authorizationClientService.loadAuthorizedClient(clientRegistrationId, principalName)
    }

    override fun removeAuthorizedClient(clientRegistrationId: String?, principalName: String?) {
        authorizationClientService.removeAuthorizedClient(clientRegistrationId, principalName)
    }

    override fun saveAuthorizedClient(authorizedClient: OAuth2AuthorizedClient?, principal: Authentication?) {
        authorizationClientService.saveAuthorizedClient(authorizedClient, principal)
    }

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        authentication.runCatching {
            this.principal.let { it as OAuth2User }
                .let { user ->
                    val scopes = mutableSetOf(defaultScope)
                    val roles = mutableSetOf("USER")
                    user.authorities.forEach {
                        when (it.authority.startsWith("ROLE_", ignoreCase = true)) {
                            true -> roles.add(it.authority.substring(5))
                            else -> scopes.add(it.authority.substring(6))
                        }
                    }
                    JWT()
                        .setUniqueId(idGenerator())
                        .setIssuer(issuer)
                        .setIssuedAt(ZonedDateTime.now(ZoneOffset.UTC))
                        .setSubject(user.name)
                        .setExpiration(ZonedDateTime.now(ZoneOffset.UTC).plusMinutes(60))
                        .addClaim("data", "flow")
                        .addClaim("principal", user.attributes)
                        .addClaim("authenticated", authentication.isAuthenticated)
                        .addClaim("scope", scopes)
                        .addClaim("role", roles)
                }
                .let { JWT.getEncoder().encode(it, signer) }
        }.fold({
            response.sendRedirect("${request.getHeader("Referer")}?jwt=$it")
        }, { logger.error { it.message } })
    }

    override fun decode(token: String): Jwt {
        val decoded = Jwt.withTokenValue(token)
        val jwt = decoder.decode(token, verifiers) {
            val alg = it.algorithm.name
            val typ = it.type.name
            decoded.header("alg", alg)
            decoded.header("typ", typ)
            alg
        }

        return decoded
            .jti(jwt.uniqueId)
            .issuer(jwt.issuer)
            .issuedAt(jwt.issuedAt.toInstant())
            .expiresAt(jwt.expiration.toInstant())
            .subject(jwt.subject)
            .claims { it.putAll(jwt.otherClaims) }.build()
    }

}

class IdGenerator : () -> String {
    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
    override fun invoke(): String {
        return (0..16)
            .map { kotlin.random.Random.nextInt(0, chars.size) }
            .map(chars::get)
            .joinToString("")
    }

}