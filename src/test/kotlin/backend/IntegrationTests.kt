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

import backend.api.CarApiService
import backend.api.DriverApiService
import backend.exceptions.CarAlreadyInUseException
import backend.exceptions.NotFoundException
import backend.model.*
import backend.repositories.CarRepository
import backend.repositories.DriverRepository
import backend.repositories.GeolocationRepository
import backend.repositories.ManufacturerRepository
import backend.services.CarApiServiceImpl
import backend.services.DriverApiServiceImpl
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@SpringBootTest
@ActiveProfiles("demo")
internal class IntegrationTests {

    @Autowired
    lateinit var driverRepository: DriverRepository

    @Autowired
    lateinit var carService: CarApiService

    @Autowired
    lateinit var geolocationRepository: GeolocationRepository

    @Autowired
    lateinit var carRepository: CarRepository

    @Autowired
    lateinit var manufacturerRepository: ManufacturerRepository

    lateinit var driverApiService: DriverApiService
    lateinit var carApiService: CarApiService

    @BeforeEach
    fun setUp() {
        driverApiService = DriverApiServiceImpl(
            driverRepository,
            geolocationRepository,
            carService
        )
        carApiService = CarApiServiceImpl(
            carRepository, manufacturerRepository
        )
    }

    @Test
    fun `availability of test data for Driver`() {
        val driver = driverApiService.getDriver(UUID.fromString("be598ce2-cf29-4202-b947-5cb58e32682f"))
        assertThat(driver).isNotNull
        val ex = assertThrows<NotFoundException> {
            driverApiService.getDriver(UUID.fromString("381eb40e-4d02-4c19-b300-cb419879d870"))
        }.message
        assertThat(ex)
            .isEqualTo("Driver with ID: '381eb40e-4d02-4c19-b300-cb419879d870' hasn't been found in database")
    }

    @Test
    @Transactional(readOnly = true)
    fun `Driver inserted into database`() {
        val username = UUID.randomUUID().toString()
        val password = UUID.randomUUID().toString()
        val create = CreateDriver(
            username = username,
            password = password
        )
        val driver = driverApiService.createDriver(create)
        assertThat(driver.id).isNotNull()
        assertThat(driver.username).isEqualTo(username)
        assertThat(driver.onlineStatus).isEqualTo(OnlineStatus.OFFLINE)
        assertThat(driver.dateCreated).isNotNull()
    }

    @Test
    @Transactional(readOnly = true)
    fun `check that Driver has been deleted from Database`() {
        val uuid = UUID.fromString("7030970c-a7de-471c-b64b-21348ef6a76a")
        assertThat(driverApiService.getDriver(uuid)).isNotNull
        driverApiService.deleteDriver(uuid)
        assertThrows<NotFoundException> {
            driverApiService.getDriver(uuid)
        }
    }

    @Test
    @Transactional(readOnly = true)
    fun `attempt to assign car belongs to another Driver`() {
        val driverId = UUID.fromString("be598ce2-cf29-4202-b947-5cb58e32682f")
        val carId = UUID.fromString("c8ebf883-d39a-47bc-ac63-d450e46f1afd")
        val ex = assertThrows<CarAlreadyInUseException> {
            driverApiService.assignCar(driverId, carId)
        }.message
        assertThat(ex).isEqualTo("Cannot assign Car, assigned to another Driver")
    }

    @Test
    @Transactional(readOnly = true)
    fun `assign-unassing cases for Driver and Car`() {
        val driverUuid = UUID.fromString("90cb04b1-9b7d-43b3-8bab-0a7a982934d4")
        val carUuid = UUID.fromString("b8b7bb84-41d1-456b-9ef6-c8307b1cd122")
        val randomId = UUID.randomUUID()
        var msg = assertThrows<NotFoundException> {
            driverApiService.assignCar(
                driverUuid,
                randomId
            )
        }.message
        assertThat(msg).isEqualTo("Car with ID: '$randomId' hasn't been found in database")
        msg = assertThrows<NotFoundException> {
            driverApiService.assignCar(
                randomId,
                carUuid
            )
        }.message
        assertThat(msg).isEqualTo("Driver with ID: '$randomId' hasn't been found in database")
        driverApiService.assignCar(driverUuid, carUuid)
        var driver = driverApiService.getDriver(driverUuid)
        assertThat(driver).isNotNull
        assertThat(driver.car).isNotNull
        assertThat(driver.car?.id).isEqualTo(carUuid)

        driverApiService.unassignCar(driverUuid, carUuid)

        driver = driverApiService.getDriver(driverUuid)
        assertThat(driver).isNotNull
        assertThat(driver.car).isNull()
    }

    @Test
    @Transactional(readOnly = true)
    fun `update geolocation for a Driver`() {
        val driverUuid = UUID.fromString("7030970c-a7de-471c-b64b-21348ef6a76a")
        val geoLocation = GeoLocation(
            latitude = 9.1,
            longitude = -10.3
        )
        var driver = driverApiService.getDriver(driverUuid)
        assertThat(driver).isNotNull
        assertThat(driver.coordinate).isNull()
        driverApiService.updateLocation(driverUuid, geoLocation)
        driver = driverApiService.getDriver(driverUuid)
        assertThat(driver).isNotNull
        assertThat(driver.coordinate).isNotNull
        assertThat(driver.coordinate?.latitude).isEqualTo(geoLocation.latitude)
        assertThat(driver.coordinate?.longitude).isEqualTo(geoLocation.longitude)

        driverApiService.updateLocation(
            driverUuid, GeoLocation(
                11.1,
                10.3
            )
        )
        driver = driverApiService.getDriver(driverUuid)
        assertThat(driver).isNotNull
        assertThat(driver.coordinate).isNotNull
        assertThat(driver.coordinate?.latitude).isNotEqualTo(geoLocation.latitude)
    }

    @Test
    @Transactional(readOnly = true)
    fun `create new Car`() {
        val newCar = Car(
            vin = UUID.randomUUID().toString(),
            model = "X1",
            manufacturer = "NNN",
            seatCount = 2,
            licensePlate = "123SDA",
            convertible = true,
            engineType = Engine.OIL
        )
        val created = carApiService.addCar(newCar)
        assertThat(created).isNotNull
        assertThat(created.licensePlate).isEqualTo("123SDA")
        assertThat(created.model).isEqualTo("X1")
        assertThat(created.manufacturer).isEqualTo("NNN")
        assertThat(created.engineType).isEqualTo(Engine.OIL)
    }

    @Test
    @Transactional(readOnly = true)
    fun `modify car attempt`() {
        val uuid = UUID.fromString("844ea81e-607f-427b-b4d5-7da2d67a297c")
        val update = UpdateCar(
            manufacturer = "BMW",
            licensePlate = "GHDD112"
        )
        val car = carApiService.modifyCar(
            carId = uuid,
            updateCar = update
        )
        assertThat(car).isNotNull
        assertThat(car.licensePlate).isEqualTo("GHDD112")
        assertThat(car.manufacturer).isEqualTo("BMW")
        val randomId = UUID.randomUUID()
        assertThrows<NotFoundException> {
            carApiService.modifyCar(
                carId = randomId,
                updateCar = update
            )
        }.let { assertThat(it.message).isEqualTo("Car with ID: '$randomId' hasn't been found in database") }
    }

    @Test
    @Transactional(readOnly = true)
    fun `attempt to remove Car`() {
        val randomId = UUID.randomUUID()
        assertThrows<NotFoundException> {
            carApiService.removeCar(randomId)
        }.let { assertThat(it.message).isEqualTo("Car with ID: '$randomId' hasn't been found in database") }
        val carId = UUID.fromString("b8b7bb84-41d1-456b-9ef6-c8307b1cd122")
        assertThat(carApiService.carInfo(carId)).isNotNull
        carApiService.removeCar(carId)
        assertThrows<NotFoundException> {
            carApiService.carInfo(carId)
        }.let { assertThat(it.message).isEqualTo("Car with ID: '$carId' hasn't been found in database") }
    }

    @Test
    @Transactional(readOnly = true)
    fun `find Cars by parameters`() {
        CarsQuery().let {
            assertThat(carApiService.findCars(it).cars).hasSize(5)
        }
        CarsQuery(
            ratingLowBound = 4.0,
            ratingHighBound = 6.0
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(3)
        }
        CarsQuery(
            manufacturer = "BMW"
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(3)
        }
        CarsQuery(
            model = "Z"
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(3)
        }
        CarsQuery(
            engineType = Engine.ELECTRIC
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(2)
        }
        CarsQuery(
            licensePlate = "D"
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(4)
        }
        CarsQuery(
            vin = "JHKDS"
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(1)
        }
        CarsQuery(
            vin = "JHKDS"
        ).let {
            assertThat(carApiService.findCars(it).cars).hasSize(1)
        }
    }

    @Test
    @Transactional(readOnly = true)
    fun `find Drivers by parameters`() {
        DriversQuery(
            engineType = Engine.ELECTRIC,
            model = "X",
            manufacturer = "GM",
            ratingLowBound = 8.0
        ).let {
            assertThat(driverApiService.findDrivers(it).drivers).hasSize(1)
        }
        DriversQuery(
            username = "name.de"
        ).let {
            assertThat(driverApiService.findDrivers(it).drivers).hasSize(4)
        }
        DriversQuery(
            onlineStatus = OnlineStatus.ONLINE
        ).let {
            assertThat(driverApiService.findDrivers(it).drivers).hasSize(3)
        }
        DriversQuery(
            deleted = true
        ).let {
            assertThat(driverApiService.findDrivers(it).drivers).hasSize(1)
        }
        DriversQuery(
            passwordExpired = true
        ).let {
            assertThat(driverApiService.findDrivers(it).drivers).hasSize(1)
        }
    }

    @Test
    @Transactional(readOnly = true)
    fun `update Driver Online status`() {
        val uuid = UUID.fromString("7030970c-a7de-471c-b64b-21348ef6a76a")
        driverApiService.getDriver(uuid).let {
            assertThat(it.onlineStatus).isEqualTo(OnlineStatus.OFFLINE)
        }
        driverApiService.mergeDriver(
            uuid, UpdateDriver(
                onlineStatus = OnlineStatus.ONLINE
            )
        ).let {
            assertThat(it.onlineStatus).isEqualTo(OnlineStatus.ONLINE)
        }
        driverApiService.getDriver(uuid).let {
            assertThat(it.onlineStatus).isEqualTo(OnlineStatus.ONLINE)
        }
    }
}