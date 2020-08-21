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

package backend.clients

import com.fasterxml.jackson.annotation.JsonProperty
import com.freenow.model.AuthResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.PostMapping
import javax.validation.constraints.NotNull

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Component
class GithubCredentials(
    @Value("\${spring.security.oauth2.client.registration.github.client-id}")
    val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.github.client-secret}")
    val clientSecret: String
)

data class GithubOauthRequest(
    @get:NotNull
    @field:JsonProperty("code") val code: String,
    @get:NotNull
    @field:JsonProperty("client_id") val clientId: String,
    @get:NotNull
    @field:JsonProperty("client_secret") val clientSecret: String,
    @get:NotNull
    @field:JsonProperty("state") val state: String,
    @get:NotNull
    @field:JsonProperty("redirect_uri") val redirectUri: String,
    @field:JsonProperty("scope") val scope: String?
)

@FeignClient(
    name = "github-oauth-token",
    url = "https://github.com/login/oauth/access_token"
)
interface GithubOAuthClient {
    @PostMapping(consumes = ["application/json"], produces = ["application/json"])
    fun token(authRequest: GithubOauthRequest): AuthResponse
}