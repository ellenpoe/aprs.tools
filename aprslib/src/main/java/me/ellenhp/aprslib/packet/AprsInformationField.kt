/*
 * Copyright (c) 2019 Ellen Poe
 *
 * This file is part of APRSTools.
 *
 * APRSTools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * APRSTools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with APRSTools.  If not, see <https://www.gnu.org/licenses/>.
 */

package me.ellenhp.aprslib.packet

data class AprsInformationField(val dataType: Char, val aprsData: AprsData, val aprsDataExtension: AprsDataExtension?, val comment: String) {
    override fun toString(): String {
        return "%c%s%s%s".format(dataType, aprsData, aprsDataExtension ?: "", comment)
    }

    companion object {
        /** Convenience method for constructing a location update */
        fun locationUpdate(latitude: Double, longitude: Double): AprsInformationField {
            val latLng = AprsLatLng(latitude, longitude, AprsPositAmbiguity.NEAREST_19_METERS)
            val symbol = AprsSymbol('/', '$') // Phone symbol.
            val posit = AprsPosition(latLng, symbol)
            return AprsInformationField('=', AprsData(listOf(posit)), null, "Sent with APRSTools")
        }
    }
}

data class AprsData(val data: List<AprsDatum>) {
    inline fun <reified T> findDatumOfType(): T? {
        val instances = data.filterIsInstance<T>()
        if (instances.size == 0)
            return null
        else if (instances.size == 1)
            return instances[0]
        else
            throw DuplicateAprsDataException()
    }

    override fun toString(): String {
        return data.joinToString("")
    }
}

interface AprsDatum

interface AprsDataExtension

class DuplicateAprsDataException : Exception("You may only have not have duplicate AprsData. One per subclass only.")