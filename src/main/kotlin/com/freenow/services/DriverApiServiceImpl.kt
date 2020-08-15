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

import com.freenow.api.CarApiService
import com.freenow.api.DriverApiService
import com.freenow.exceptions.CarAlreadyInUseException
import com.freenow.exceptions.NotFoundException
import com.freenow.jdbc.tables.records.DriverRecord
import com.freenow.model.*
import com.freenow.repositories.DriverRepository
import com.freenow.repositories.GeolocationRepository
import com.freenow.utils.map
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
    private val randomUUID = UUID::randomUUID
    private val now = { OffsetDateTime.now() }

    @Transactional
    override fun assignCar(id: UUID, carId: UUID) {
        getDriver(id)
            .takeIf { it.onlineStatus == OnlineStatus.ONLINE }
            ?.let { driver ->
                carService.carInfo(carId)
                    .let {
                        driverRepository.findByCarId(carId)
                            ?.takeIf { it.id != driver.id }
                            ?.let { drv ->
                                if (drv.onlineStatus != OnlineStatus.ONLINE) {
                                    unassignCar(drv.id)
                                } else {
                                    throw CarAlreadyInUseException("Cannot assign Car, assigned to another Driver")
                                }
                            }
                        driverRepository.assignCar(driver.id, carId)
                    }
            }
    }

    override fun createDriver(createDriver: CreateDriver): Driver {
        return driverRepository.create(
            map(createDriver, randomUUID, now, { OnlineStatus.OFFLINE }) { null }
        ).let { map(it, { null }, { null }) }
    }

    override fun deleteDriver(id: UUID) {
        if (!driverRepository.deleteById(id))
            throw NotFoundException("Driver with ID: '$id' hasn't been found in database")
    }

    override fun findDrivers(driversQuery: DriversQuery): DriverList {
        val carIds = map(driversQuery).takeIf { hasCarParameters(it) }
            ?.let { carService.findCars(it) }?.cars
            ?.mapNotNull { it.id }
            ?.takeIf { it.isNotEmpty() }
        val driverList = driverRepository.findByParameters(
            username = driversQuery.username,
            onlineStatus = driversQuery.onlineStatus,
            deleted = driversQuery.deleted,
            passwordExpired = driversQuery.passwordExpired,
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

    override fun unassignCar(id: UUID) {
        driverRepository.unassignCar(id)
    }

    override fun updateLocation(id: UUID, geoLocation: GeoLocation) {
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