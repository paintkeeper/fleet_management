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

package com.freenow.utils

import com.freenow.jdbc.tables.records.CarRecord
import com.freenow.jdbc.tables.records.DriverRecord
import com.freenow.jdbc.tables.records.ManufacturerRecord
import com.freenow.model.*
import java.time.OffsetDateTime
import java.util.*

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
fun map(record: DriverRecord, geolocation: (UUID) -> GeoLocation?, car: (UUID?) -> Car?): Driver {
    return record.let {
        Driver(
            id = it.id,
            username = it.username,
            dateCreated = it.dateCreated,
            coordinate = geolocation(it.id),
            onlineStatus = it.onlineStatus,
            car = car(it.carId)
        )
    }
}

fun map(
    createDriver: CreateDriver,
    id: () -> UUID,
    dateCreated: () -> OffsetDateTime?,
    onlineStatus: () -> OnlineStatus,
    carId: () -> UUID?
): DriverRecord {
    return DriverRecord(
        id(),
        createDriver.username,
        null,
        dateCreated(),
        false,
        true,
        onlineStatus(),
        carId()
    )
}

fun map(
    driver: Driver,
    password: String? = null,
    deleted: Boolean? = null,
    passwordExpired: Boolean? = null
): DriverRecord {
    return DriverRecord(
        driver.id,
        driver.username,
        password,
        null,
        deleted,
        passwordExpired,
        driver.onlineStatus,
        driver.car?.id
    )
}

fun map(
    car: Car,
    id: () -> UUID,
    dateCreated: () -> OffsetDateTime?,
    manufacturer: () -> UUID,
    deleted: () -> Boolean
): CarRecord {
    return CarRecord(
        id(),
        dateCreated(),
        car.vin,
        car.model,
        car.licensePlate,
        car.seatCount,
        car.engineType,
        car.convertible,
        car.rating,
        manufacturer(),
        deleted()
    )
}

fun map(record: CarRecord, manufacturer: (manufacturerId: UUID) -> Manufacturer): Car {
    return Car(
        vin = record.vin,
        model = record.model,
        engineType = record.engineType,
        convertible = record.convertible,
        licensePlate = record.licensePlate,
        manufacturer = manufacturer(record.manufacturerId).name,
        seatCount = record.seatCount,
        rating = record.rating,
        id = record.id,
        dateCreated = record.dateCreated
    )
}

fun map(record: ManufacturerRecord): Manufacturer {
    return Manufacturer(
        id = record.id,
        name = record.name,
        dateCreated = record.dateCreated
    )
}

fun map(driversQuery: DriversQuery): CarsQuery {
    return CarsQuery(
        licensePlate = driversQuery.licensePlate,
        model = driversQuery.licensePlate,
        seatCount = driversQuery.seatCount,
        engineType = driversQuery.engineType,
        manufacturer = driversQuery.manufacturer,
        vin = driversQuery.vin,
        ratingHighBound = driversQuery.ratingHighBound,
        ratingLowBound = driversQuery.ratingLowBound
    )
}
