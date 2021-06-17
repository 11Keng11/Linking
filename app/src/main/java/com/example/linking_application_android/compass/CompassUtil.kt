package com.example.linking_application_android.compass

import com.google.android.gms.maps.model.LatLng

class CompassUtil {

    /**
     * Returns clockwise angle in degrees.
     */
    fun radianToDegrees(radian: Float): Int {
        var degrees: Int

        if (radian < 0) {
            degrees = (360 - Math.toDegrees(radian.toDouble() * -1)).toInt()
        } else {
            degrees = Math.toDegrees(radian.toDouble()).toInt()
        }

        return degrees
    }

    fun angleFromCoordinate(curLoc: LatLng, dstLoc: LatLng): Double {
        val lat1: Double = curLoc.latitude
        val long1: Double = curLoc.longitude
        val lat2: Double = dstLoc.latitude
        val long2: Double = dstLoc.longitude
        val dLon = long2 - long1
        val y = Math.sin(dLon) * Math.cos(lat2)
        val x = Math.cos(lat1) * Math.sin(lat2) - (Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon))
        var brng = Math.atan2(y, x)
        brng = Math.toDegrees(brng)
        brng = (brng + 360) % 360
        return brng
    }

    fun distance(curLoc: LatLng, dstLoc: LatLng): Double {
        val lat1: Double = curLoc.latitude
        val lon1: Double = curLoc.longitude
        val lat2: Double = dstLoc.latitude
        val lon2: Double = dstLoc.longitude
        val R = 6371 // Radius of the earth
        val latDistance = Math.toRadians(lat2 - lat1)
        val lonDistance = Math.toRadians(lon2 - lon1)
        val a = (Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + (Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2)))
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        var distance = R * c * 1000 // convert to meters
        return distance
    }
}