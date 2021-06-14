package com.example.linking_application_android.compass

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

}