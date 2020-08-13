////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2020.
// This program is free software: you can redistribute it and/or modify it
// under the terms of the GNU General Public License
// as published by the Free Software Foundation, either version 3 of the License,
// or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
// See the GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program.  If not, see <https://www.gnu.org/licenses/>.
////////////////////////////////////////////////////////////////////////////////

package com.freenow.services

import com.freenow.api.NotFoundException
import com.freenow.repositories.DriverRepository
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

import java.util.*

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@SpringBootTest
@ActiveProfiles("test")
internal class DriverApiServiceImplTest {
    @Autowired
    lateinit var driverRepository: DriverRepository

    @BeforeEach
    fun setUp() {
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun getDriver() {
        val sut = DriverApiServiceImpl(driverRepository)
        val driver = sut.getDriver(UUID.fromString("be598ce2-cf29-4202-b947-5cb58e32682f"))
        assertThat(driver).isNotNull
        val ex = assertThrows<NotFoundException> {
            sut.getDriver(UUID.fromString("381eb40e-4d02-4c19-b300-cb419879d870"))
        }.message
        assertThat(ex).isEqualTo("Driver with ID: '381eb40e-4d02-4c19-b300-cb419879d870' hasn't been found in database")
    }

    @Test
    fun assignCar() {

    }
}