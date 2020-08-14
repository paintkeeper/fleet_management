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
import com.freenow.exceptions.NotFoundException
import com.freenow.jdbc.tables.records.ManufacturerRecord
import com.freenow.model.Car
import com.freenow.model.Manufacturer
import com.freenow.model.UpdateCar
import com.freenow.repositories.CarRepository
import com.freenow.repositories.ManufacturerRepository
import com.freenow.utils.map
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.util.*

@Service
class CarApiServiceImpl(
    private val carRepository: CarRepository,
    private val manufacturerRepository: ManufacturerRepository
) : CarApiService {

    private val randomUUID = UUID::randomUUID
    private val now = { OffsetDateTime.now() }

    override fun addCar(car: Car): Car {
        val manufacturer = getOrCreateManufacturer(car.manufacturer)
        return map(car,
            randomUUID,
            now,
            { manufacturer.id },
            { false })
            .let { carRepository.create(it) }
            .let {
                map(it) {
                    map(manufacturer)
                }
            }
    }

    override fun carInfo(carId: UUID): Car {
        return carRepository.findById(carId)?.let {
            map(it) { manufacturerId ->
                map(manufacturerRepository.findById(manufacturerId))
            }
        } ?: throw NotFoundException("Car with ID: '$carId' hasn't been found in database")
    }

    @Transactional
    override fun modifyCar(carId: UUID, updateCar: UpdateCar): Car {
        val manufacturer = updateCar.manufacturer
            ?.let { getOrCreateManufacturer(it) }
            ?.let { map(it) }
        return carRepository.findById(carId)
            ?.let { car ->
                carRepository.updateCarById(
                    car.id,
                    car.vin,
                    model = updateCar.model,
                    rating = updateCar.rating?.toDouble(),
                    seatCount = updateCar.seatCount,
                    manufacturer = manufacturer?.id,
                    licensePlate = updateCar.licensePlate,
                    convertible = updateCar.convertible,
                    engineType = updateCar.engineType
                )
                carRepository.findById(car.id)
            }
            ?.let { car ->
                map(car) {
                    map(manufacturerRepository.findById(it))
                }
            } ?: throw NotFoundException("Car with ID: '$carId' hasn't been found in database")
    }

    override fun removeCar(carId: UUID) {
        if (!carRepository.deleteById(carId))
            throw NotFoundException("Car with ID: '$carId' hasn't been found in database")
    }

    private fun getOrCreateManufacturer(manufacturer: Manufacturer): ManufacturerRecord {
        return manufacturer.id?.let { manufacturerRepository.findById(it) }
            ?: manufacturerRepository.findByName(manufacturer.name)
            ?: manufacturerRepository.create(
                map(
                    manufacturer,
                    randomUUID,
                    now
                )
            )
    }

}