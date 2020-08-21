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

package backend.services

import com.freenow.api.CarApiService
import com.freenow.api.DriverApiService
import com.freenow.jdbc.tables.records.DriverGeolocationRecord
import com.freenow.jdbc.tables.records.DriverRecord
import com.freenow.model.*
import backend.repositories.DriverRepository
import backend.repositories.GeolocationRepository
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.time.OffsetDateTime
import java.util.*


/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@ExtendWith(MockKExtension::class)
internal class DriverApiServiceImplTest {
    @MockK
    lateinit var mockDriverRepository: DriverRepository

    @MockK
    lateinit var mockGeolocationRepository: GeolocationRepository

    @MockK
    lateinit var mockCarApiService: CarApiService

    lateinit var record: DriverRecord
    lateinit var recordUUID: String
    lateinit var recordUsername: String
    lateinit var recordPassword: String
    lateinit var recordCreationDate: OffsetDateTime
    lateinit var recordOnlineStatus: OnlineStatus
    lateinit var vin: String
    lateinit var car: Car
    lateinit var carId: UUID

    lateinit var systemUnderTest: DriverApiService

    @BeforeEach
    fun setUp() {
        recordUUID = "e3e82769-d0da-4fa2-9184-3e4f5baa8ab6"
        recordUsername = "username12"
        recordPassword = "password00012"
        recordCreationDate = OffsetDateTime.now()
        recordOnlineStatus = OnlineStatus.ONLINE
        vin = UUID.randomUUID().toString()
        carId = UUID.randomUUID()
        val uuid = UUID.fromString(recordUUID)
        record = DriverRecord(
            uuid,
            recordUsername,
            recordPassword,
            recordCreationDate,
            false,
            false,
            recordOnlineStatus,
            null
        )
        car = Car(
            vin = vin,
            engineType = Engine.OIL,
            convertible = true,
            licensePlate = "RNDM",
            manufacturer = "BMW",
            seatCount = 4,
            model = "S500",
            id = carId,
            dateCreated = recordCreationDate,
            rating = 9.0
        )
        every { mockDriverRepository.create(ofType(DriverRecord::class)) }.returns(record)
        every { mockDriverRepository.findById(eq(uuid)) }.returns(record)
        every { mockDriverRepository.findByCarId(eq(carId)) }.returns(record)
        every { mockDriverRepository.assignCar(ofType(UUID::class), ofType(UUID::class)) }.returns(true)
        every { mockGeolocationRepository.findById(eq(uuid)) }.returns(
            DriverGeolocationRecord(
                uuid,
                10.5.toBigDecimal(), (-11.6).toBigDecimal(),
                recordCreationDate
            )
        )

        every { mockCarApiService.carInfo(eq(carId)) }.returns(car)
        every { mockCarApiService.modifyCar(eq(carId), ofType(UpdateCar::class)) }.returns(car)

        systemUnderTest = DriverApiServiceImpl(
            mockDriverRepository,
            mockGeolocationRepository,
            mockCarApiService
        )
    }

    @AfterEach
    fun tearDown() {
    }

    @Test
    fun createDriver() {
        val driver = systemUnderTest.createDriver(
            CreateDriver(
                username = recordUsername,
                password = recordPassword
            )
        )
        verify(exactly = 1) { mockDriverRepository.create(any()) }
        assertThat(driver).isNotNull
        assertThat(driver.coordinate).isNull()
        val uuid = UUID.fromString(recordUUID)
        assertThat(driver.id).isEqualTo(uuid)
    }

    @Test
    fun getDriver() {
        val uuid = UUID.fromString(recordUUID)
        val driver = systemUnderTest.getDriver(uuid)
        verify(exactly = 1) { mockDriverRepository.findById(eq(uuid)) }
        verify(exactly = 1) { mockGeolocationRepository.findById(eq(uuid)) }
        assertThat(driver).isNotNull
        assertThat(driver.coordinate).isNotNull
        assertThat(driver.id).isEqualTo(uuid)
    }

    @Test
    fun assignCar() {
        val uuid = UUID.fromString(recordUUID)
        systemUnderTest.assignCar(uuid, carId)
        verify(exactly = 1) {
            mockCarApiService.carInfo(eq(carId))
        }
        verify(exactly = 1) {
            mockDriverRepository.assignCar(eq(uuid), eq(carId))
        }
    }
}