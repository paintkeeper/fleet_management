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

package com.freenow.services

import com.freenow.api.AuthApiService
import com.freenow.clients.GithubCredentials
import com.freenow.clients.GithubOAuthClient
import com.freenow.clients.GithubOauthRequest
import com.freenow.exceptions.NotFoundException
import com.freenow.model.AuthRequest
import com.freenow.model.AuthResponse
import org.springframework.stereotype.Service

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Service
class AuthApiServiceImpl(
    private val githubOAuthClient: GithubOAuthClient,
    private val githubCredentials: GithubCredentials
) : AuthApiService {

    override fun obtainToken(referer: String, authRequest: AuthRequest): AuthResponse {
        if (authRequest.clientId == githubCredentials.clientId)
            return githubOAuthClient.token(
                GithubOauthRequest(
                    clientId = githubCredentials.clientId,
                    clientSecret = githubCredentials.clientSecret,
                    code = authRequest.code,
                    redirectUri = referer,
                    scope = authRequest.scope,
                    state = authRequest.state
                )
            ) else throw NotFoundException("Unexpected Client ID.")
    }


}