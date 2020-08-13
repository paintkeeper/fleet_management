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

import com.freenow.api.DriverApiService
import com.freenow.api.NotFoundException
import com.freenow.model.*
import com.freenow.repositories.DriverRepository
import com.freenow.utils.map
import org.springframework.stereotype.Service
import java.util.*

@Service

class DriverApiServiceImpl(
    private val driverRepository: DriverRepository
) : DriverApiService {

    override fun assignCar(id: UUID, vin: String) {
        TODO("Not yet implemented")
    }

    override fun createDriver(createDriver: CreateDriver): Driver {
        TODO("Not yet implemented")
    }

    override fun deleteDriver(id: UUID) {
        TODO("Not yet implemented")
    }

    override fun findDrivers(status: OnlineStatus, nextId: UUID?): DriverList {
        TODO("Not yet implemented")
    }

    override fun getDriver(id: UUID): Driver {
        return driverRepository.findById(id)?.let { map(it) }
            ?: throw NotFoundException("Driver with ID: '$id' hasn't been found in database")
    }

    override fun unassignCar(id: UUID, vin: String) {
        TODO("Not yet implemented")
    }

    override fun updateLocation(id: UUID, geoLocation: GeoLocation) {
        TODO("Not yet implemented")
    }

}