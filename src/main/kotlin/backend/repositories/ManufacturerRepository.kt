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

import backend.jdbc.tables.Manufacturer.*
import backend.jdbc.tables.records.ManufacturerRecord
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.util.*

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
@Repository
class ManufacturerRepository(private val jooq: DSLContext) {

    @Transactional
    fun create(record: ManufacturerRecord): ManufacturerRecord {
        jooq.insertInto(MANUFACTURER)
            .set(record)
            .onConflictDoNothing()
            .execute()
        return jooq.selectFrom(MANUFACTURER)
            .where(MANUFACTURER.NAME.eq(record.name))
            .fetchOne()
    }

    fun findById(id: UUID): ManufacturerRecord {
        return jooq.selectFrom(MANUFACTURER)
            .where(MANUFACTURER.ID.eq(id))
            .fetchOne()
    }

    fun findByName(name: String): ManufacturerRecord? {
        return jooq.selectFrom(MANUFACTURER)
            .where(MANUFACTURER.NAME.eq(name))
            .singleOrNull()
    }
}