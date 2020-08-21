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

package backend.services

import backend.api.CarApiService
import backend.api.DriverApiService
import backend.exceptions.CarAlreadyInUseException
import backend.exceptions.NotFoundException
import backend.exceptions.ValidationException
import backend.jdbc.tables.records.DriverRecord
import backend.model.*
import backend.repositories.DriverRepository
import backend.repositories.GeolocationRepository
import backend.utils.map
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*
import java.util.stream.Collectors

@Service
class DriverApiServiceImpl(
    private val driverRepository: DriverRepository,
    private val geolocationRepository: GeolocationRepository,
    private val carService: CarApiService
) : DriverApiService {

    private val logger = KotlinLogging.logger {}

    private val randomUUID = UUID::randomUUID
    private val now = { OffsetDateTime.now() }

    @Transactional
    override fun assignCar(id: UUID, carId: UUID) {
        logger.debug { "Attempt to assign Car ID: $carId to Driver with ID: $id" }
        getDriver(id)
            .takeIf { it.onlineStatus == OnlineStatus.ONLINE }
            ?.let { driver ->
                carService.carInfo(carId)
                    .let {
                        driverRepository.findByCarId(carId)
                            ?.takeIf { it.id != driver.id }
                            ?.let { drv ->
                                if (drv.onlineStatus != OnlineStatus.ONLINE) {
                                    unassignCar(drv.id, carId)
                                } else {
                                    throw CarAlreadyInUseException("Cannot assign Car, assigned to another Driver")
                                }
                            }
                        driverRepository.assignCar(driver.id, carId)
                    }
            } ?: throw ValidationException("Cannot assign Car, Driver is not ONLINE")
    }

    override fun createDriver(createDriver: CreateDriver): Driver {
        logger.debug { "Attempt to create Driver $createDriver" }
        return driverRepository.create(
            map(createDriver, randomUUID, now, { OnlineStatus.OFFLINE }) { null }
        ).let { map(it, { null }, { null }) }
    }

    override fun deleteDriver(id: UUID) {
        logger.debug { "Attempt to delete Driver By ID: $id" }
        if (!driverRepository.deleteById(id))
            throw NotFoundException("Driver with ID: '$id' hasn't been found in database")
    }

    override fun findDrivers(driversQuery: DriversQuery?): DriverList {
        val carIds = driversQuery?.let { map(it) }
            ?.takeIf { hasCarParameters(it) }
            ?.let { carService.findCars(it) }?.cars
            ?.mapNotNull { it.id }
            ?.takeIf { it.isNotEmpty() }
        val driverList = driverRepository.findByParameters(
            username = driversQuery?.username,
            onlineStatus = driversQuery?.onlineStatus,
            deleted = driversQuery?.deleted,
            passwordExpired = driversQuery?.passwordExpired,
            carIds = carIds
        ).map { mergeValues(it) }
            .collect(Collectors.toList())

        return DriverList(driverList)
    }

    override fun getDriver(id: UUID): Driver {
        return driverRepository.findById(id)
            ?.let { mergeValues(it) }
            ?: throw NotFoundException("Driver with ID: '$id' hasn't been found in database")
    }

    override fun mergeDriver(id: UUID, updateDriver: UpdateDriver): Driver {
        return driverRepository.findById(id)?.let {
            it.onlineStatus = updateDriver.onlineStatus
            it
        }?.let { driverRepository.update(it) }
            ?.let { mergeValues(it) }
            ?: throw NotFoundException("Driver with ID: '$id' hasn't been found in database")
    }

    override fun unassignCar(id: UUID, carId: UUID) {
        logger.debug { "Attempt to unassign car from Driver with ID: $id and Car with ID: $carId" }
        if(!driverRepository.unassignCar(id, carId))
            throw ValidationException("Failed to unassigh Car from Driver")
    }

    override fun updateLocation(id: UUID, geoLocation: GeoLocation) {
        logger.debug { "Attempt to update Geolocation $geoLocation for Driver with ID: $id" }
        geolocationRepository.saveOrUpdateGeolocation(
            id,
            geoLocation.latitude.toBigDecimal(),
            geoLocation.longitude.toBigDecimal()
        )
    }

    private fun hasCarParameters(q: CarsQuery): Boolean {
        return q.engineType
                ?: q.licensePlate
                ?: q.manufacturer
                ?: q.model
                ?: q.ratingHighBound
                ?: q.ratingLowBound
                ?: q.seatCount
                ?: q.vin != null
    }

    private fun mergeValues(record: DriverRecord): Driver {
        return map(record, { driverId ->
            geolocationRepository.findById(driverId)
                ?.let { location ->
                    GeoLocation(
                        longitude = location.longitude.toDouble(),
                        latitude = location.latitude.toDouble()
                    )
                }
        }) { carId ->
            carId?.let { carService.carInfo(it) }
        }
    }


}