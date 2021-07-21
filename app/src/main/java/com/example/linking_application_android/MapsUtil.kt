package com.example.linking_application_android

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.api.services.sheets.v4.Sheets
import com.nambimobile.widgets.efab.FabOption
import java.io.IOException
import java.util.ArrayList
import android.content.Context

// Change visibility of markers
fun changeVisibility(markers1: ArrayList<Marker>?, markers2: ArrayList<Marker>?, markers3: ArrayList<Marker>?, isVisible1: Boolean, isVisible2: Boolean, isVisible3: Boolean ) {
    for (m1 in markers1!!) {
        m1.isVisible = isVisible1
    }
    for (m2 in markers2!!) {
        m2.isVisible = isVisible2
    }
    for (m3 in markers3!!) {
        m3.isVisible = isVisible3
    }
}

// This method retrieves the correct icon for the respective markers. ie nature exercise and family
fun getIcon(item: String?, context: Context, packageName: String, width : Int , height : Int): BitmapDescriptor {
    val marker = BitmapFactory.decodeResource(context.getResources(),context.getResources().getIdentifier(item, "drawable", packageName))
    val sizedMarker = Bitmap.createScaledBitmap(marker, width, height, false)
    return BitmapDescriptorFactory.fromBitmap(sizedMarker)
}

fun readSheet(sheetRange: String?, sheetsService : Sheets?, google_api_key : String, sheet_id : String ): List<List<Any?>> {
    var values: List<List<Any?>> = ArrayList()
    try {
        val data = sheetsService!!.spreadsheets().values()[sheet_id, sheetRange]
            .setKey(google_api_key)
            .execute()
        values = data.getValues()
        return values
    } catch (e: IOException) {
        Log.e("Sheets failed", e.localizedMessage)
    }
    return values
}

// Set the markers on the map
fun setMarkers(values: List<List<Any?>>?, mapObj: GoogleMap?, markerIcon: BitmapDescriptor?): ArrayList<Marker> {
    val markers = ArrayList<Marker>()
    for (row in values!!) {
        try {
            val name = row[3].toString()
            val lat = row[2].toString().toFloat()
            val lon = row[1].toString().toFloat()
            val pos = LatLng(lat.toDouble(), lon.toDouble())
            val type = row[0].toString()
            val newMarker: Marker = mapObj!!.addMarker(
                MarkerOptions()
                    .position(pos)
                    .title(name)
                    .snippet(type)
                    .icon(markerIcon)
            )
            markers.add(newMarker)
        } catch (e : NumberFormatException) {
            Log.e("Sheets Error", e.localizedMessage )
        }
    }
    return markers
}


