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

package backend.repositories

import com.freenow.jdbc.tables.DriverGeolocation.*
import com.freenow.jdbc.tables.records.DriverGeolocationRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Repository
class GeolocationRepository(private val jooq: DSLContext) {

    fun saveOrUpdateGeolocation(id: UUID, latitude: BigDecimal, longitude: BigDecimal) {
        jooq.insertInto(
            DRIVER_GEOLOCATION,
            DRIVER_GEOLOCATION.DRIVER_ID,
            DRIVER_GEOLOCATION.LATITUDE,
            DRIVER_GEOLOCATION.LONGITUDE,
            DRIVER_GEOLOCATION.DATE_COORDINATE_UPDATED
        )
            .values(id, latitude, longitude, OffsetDateTime.now())
            .onDuplicateKeyUpdate()
            .set(DRIVER_GEOLOCATION.LATITUDE, latitude)
            .set(DRIVER_GEOLOCATION.LONGITUDE, longitude)
            .set(DRIVER_GEOLOCATION.DATE_COORDINATE_UPDATED, OffsetDateTime.now())
            .execute()
    }

    fun findById(id: UUID): DriverGeolocationRecord? {
        return jooq.selectFrom(DRIVER_GEOLOCATION)
            .where(DRIVER_GEOLOCATION.DRIVER_ID.eq(id))
            .singleOrNull()

    }
}