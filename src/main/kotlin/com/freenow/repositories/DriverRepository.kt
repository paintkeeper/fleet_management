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

package com.freenow.repositories

import com.freenow.jdbc.tables.Driver.*
import com.freenow.jdbc.tables.records.DriverRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Repository
class DriverRepository(private val jooq: DSLContext) {

    fun findById(id: UUID): DriverRecord? {
        return jooq.selectFrom(DRIVER)
            .where(DRIVER.ID.eq(id))
            .singleOrNull()
    }

    fun findByCarId(carId: UUID): DriverRecord? {
        return jooq.selectFrom(DRIVER).where(DRIVER.CAR_ID.eq(carId)).singleOrNull()
    }

    fun deleteById(id: UUID): Boolean {
        return jooq.deleteFrom(DRIVER)
            .where(DRIVER.ID.eq(id))
            .execute() == 1
    }

    @Transactional
    fun create(record: DriverRecord): DriverRecord {
        jooq.insertInto(DRIVER)
            .set(record)
            .onConflictDoNothing()
            .execute()
        return jooq.selectFrom(DRIVER)
            .where(DRIVER.ID.eq(record.id))
            .fetchOne()
    }

    fun update(record: DriverRecord): DriverRecord {
        return jooq.update(DRIVER)
            .set(record)
            .returning()
            .fetchOne()
    }

    fun unassignCar(id: UUID): Boolean {
        return jooq.update(DRIVER)
            .setNull(DRIVER.CAR_ID)
            .where(DRIVER.ID.eq(id))
            .execute() == 1
    }

    fun assignCar(id: UUID, carId: UUID): Boolean {
        return jooq.update(DRIVER)
            .set(DRIVER.CAR_ID, carId)
            .where(DRIVER.ID.eq(id))
            .execute() == 1
    }
}