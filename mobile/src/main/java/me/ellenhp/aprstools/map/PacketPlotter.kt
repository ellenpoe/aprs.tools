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

package me.ellenhp.aprstools.map

import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.auto.factory.AutoFactory
import com.google.auto.factory.Provided
import com.google.common.collect.ImmutableList
import me.ellenhp.aprslib.packet.Ax25Address
import me.ellenhp.aprstools.Utils
import me.ellenhp.aprstools.history.PacketTrackHistory
import me.ellenhp.aprstools.history.TimestampedPacket
import java.time.Duration
import java.time.Instant
import javax.inject.Inject
import javax.inject.Provider

@AutoFactory(allowSubclasses = true)
class PacketPlotter constructor(@Provided val map: Provider<GoogleMap>,
                                @Provided val instant: Provider<Instant>,
                                val pruneDuration: Duration) {

    val markers = HashMap<Ax25Address, Marker>()
    val polylines = HashMap<Ax25Address, Polyline>()

    fun plot(history: PacketTrackHistory) {
        history.getStations().stream().forEach { updateStation(it, history.getTrack(it)!!) }
    }

    private fun updateStation(station: Ax25Address, track: ImmutableList<TimestampedPacket>) {
        val cutoffTime = instant.get().minus(Duration.ofHours(6))

        updateCreateOrPrunePolyline(station, track, cutoffTime)
        updateCreateOrPruneMarker(station, track, cutoffTime)
    }

    private fun updateCreateOrPrunePolyline(station: Ax25Address, track: ImmutableList<TimestampedPacket>, cutoffTime: Instant) {
        val currentPolyline = polylines[station]
        val points = trackToPointsAfterForPolyline(track, cutoffTime)
        if (currentPolyline == null) {
            // Create a new polyline if there are enough points.
            points?.let { polylines[station] = createPolyline(it) }
        }
        else {
            // Prune the polyline if there aren't enough points.
            if (points == null) {
                currentPolyline.remove()
                polylines.remove(station)
            } else {
                currentPolyline.points = points
            }
        }
    }

    private fun updateCreateOrPruneMarker(station: Ax25Address, track: ImmutableList<TimestampedPacket>, cutoffTime: Instant) {
        val currentMarker = markers[station]
        val points = trackToPointsAfter(track, cutoffTime)
        if (currentMarker == null) {
            // Create a new marker if there's at least one point.
            points?.let { markers[station] = createMarker(it.last(), station) }
        }
        else {
            // Prune the marker if there aren't any points left.
            if (points == null) {
                currentMarker.remove()
                markers.remove(station)
            } else {
                currentMarker.position = points.last()
            }
        }
    }

    private fun createPolyline(points: ImmutableList<LatLng>): Polyline {
        val polylineOptions = PolylineOptions()
        polylineOptions.addAll(points)
        return map.get().addPolyline(polylineOptions)
    }

    private fun createMarker(point: LatLng, station: Ax25Address): Marker {
        val markerOptions = MarkerOptions()
        markerOptions.position(point)
        markerOptions.title(station.toString())
        return map.get().addMarker(markerOptions)
    }

    private fun trackToPointsAfterForPolyline(track: ImmutableList<TimestampedPacket>, cutoffTime: Instant): ImmutableList<LatLng>? {
        val points = trackToPointsAfter(track, cutoffTime) ?: return null
        if (points.count() > 1) {
            return points
        }
        return null
    }

    private fun trackToPointsAfter(track: ImmutableList<TimestampedPacket>, cutoffTime: Instant): ImmutableList<LatLng>? {
        val points = track.stream().sorted()
                .filter { it.time.isAfter(cutoffTime) }
                .map { it.packet.location() }
                .filter { it != null }
                .map { LatLng(it!!.latitude, it.longitude) }
                .collect(Utils.toImmutableList())
        return if (points.isEmpty()) null else points
    }
}