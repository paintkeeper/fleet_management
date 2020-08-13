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

import com.freenow.jdbc.enums.DriverOnlineStatus
import com.freenow.jdbc.tables.records.DriverRecord
import com.freenow.model.Driver
import com.freenow.model.OnlineStatus

/**
 * Andrei Alekseenko {engelier at gmx.de}
 */
fun map(record: DriverRecord?): Driver? {
    return record?.let {
        Driver(
            id = it.id,
            username = it.username,
            dateCreated = it.dateCreated,
            coordinate = null,
            onlineStatus = if (it.onlineStatus == DriverOnlineStatus.ONLINE) {
                OnlineStatus.ONLINE
            } else OnlineStatus.OFFLINE
        )
    }
}