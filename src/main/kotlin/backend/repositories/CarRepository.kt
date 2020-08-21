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

import backend.jdbc.tables.Car.*
import backend.jdbc.tables.records.CarRecord
import backend.model.Engine
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*
import java.util.stream.Stream

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Repository
class CarRepository(private val jooq: DSLContext) {

    @Transactional
    fun create(record: CarRecord): CarRecord {
        jooq.insertInto(CAR)
            .set(record)
            .onConflictDoNothing()
            .execute()
        return jooq.selectFrom(CAR)
            .where(CAR.VIN.eq(record.vin))
            .fetchOne()
    }

    fun findById(id: UUID): CarRecord? {
        return jooq.selectFrom(CAR)
            .where(CAR.ID.eq(id))
            .singleOrNull()
    }

    fun deleteById(id: UUID): Boolean {
        return jooq.deleteFrom(CAR)
            .where(CAR.ID.eq(id))
            .execute() == 1
    }

    fun updateCarById(
        id: UUID,
        vin: String,
        model: String?,
        licensePlate: String?,
        seatCount: Int?,
        engineType: Engine?,
        convertible: Boolean?,
        rating: Double?,
        manufacturer: UUID?
    ): Boolean {
        var step = jooq.update(CAR).set(CAR.VIN, vin)
        step = model?.let { step.set(CAR.MODEL, it) } ?: step
        step = licensePlate?.let { step.set(CAR.LICENSE_PLATE, it) } ?: step
        step = seatCount?.let { step.set(CAR.SEAT_COUNT, it) } ?: step
        step = engineType?.let { step.set(CAR.ENGINE_TYPE, it) } ?: step
        step = convertible?.let { step.set(CAR.CONVERTIBLE, it) } ?: step
        step = rating?.let { step.set(CAR.RATING, it) } ?: step
        step = manufacturer?.let { step.set(CAR.MANUFACTURER_ID, it) } ?: step
        return step.where(CAR.ID.eq(id)).execute() == 1
    }

    fun findByParameters(
        licensePlate: String?,
        ratingLowBound: Double?,
        ratingHighBound: Double?,
        vin: String?,
        manufacturerId: UUID?,
        seatCount: Int?,
        model: String?,
        engineType: Engine?
    ): Stream<CarRecord> {
        var step = jooq.selectFrom(CAR).where("1=1")
        step = model?.let { step.and(CAR.MODEL.contains(it)) } ?: step
        step = licensePlate?.let { step.and(CAR.LICENSE_PLATE.contains(it)) } ?: step
        step = vin?.let { step.and(CAR.VIN.contains(it)) } ?: step
        step = seatCount?.let { step.and(CAR.SEAT_COUNT.eq(it)) } ?: step
        step = engineType?.let { step.and(CAR.ENGINE_TYPE.eq(it)) } ?: step
        step = ratingLowBound?.let { step.and(CAR.RATING.ge(it)) } ?: step
        step = ratingHighBound?.let { step.and(CAR.RATING.le(it)) } ?: step
        step = manufacturerId?.let { step.and(CAR.MANUFACTURER_ID.eq(it)) } ?: step
        return step.fetchStream()
    }

}