package com.example.linking_application_android.route

import android.util.Log
import java.util.*

//
//import android.util.Log
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.gms.maps.model.Marker
//import com.google.android.gms.maps.model.MarkerOptions
//import com.google.api.client.extensions.android.http.AndroidHttp
//import com.google.api.client.json.JsonFactory
//import com.google.api.client.json.jackson2.JacksonFactory
//import com.google.api.services.sheets.v4.Sheets
//import java.io.IOException
//import java.util.ArrayList
//
//// Google sheet keys.
//var google_api_key: String = "AIzaSyDqJlXlJFXnGGjVXJs8maiUP5rE9oKsOB4"
//var sheet_id: String = "1hMrCgWmaN3hDmQOaIBUBcuqSXWbX8pI6d6WElL7-lrU"
//var sheetsService: Sheets? = null
//
//// Initialise google sheets
//fun initialiseSheets() {
//    val transport = AndroidHttp.newCompatibleTransport()
//    val factory: JsonFactory = JacksonFactory.getDefaultInstance()
//    sheetsService = Sheets.Builder(transport, factory, null)
//        .setApplicationName("Linking")
//        .build()
//}
//
//fun readSheet(sheetRange: String?, sheetsService : Sheets?, google_api_key : String, sheet_id : String ): List<List<Any?>> {
//    var values: List<List<Any?>> = ArrayList()
//    try {
//        val data = sheetsService!!.spreadsheets().values()[sheet_id, sheetRange]
//            .setKey(google_api_key)
//            .execute()
//        values = data.getValues()
//        return values
//    } catch (e: IOException) {
//        Log.e("Sheets failed", e.localizedMessage)
//    }
//    return values
//}
//
//class Node (Name : String) {
//    var name = Name
//    val neighbours : HashMap<Node,Float> = null
//    val connections : Int = null
//
//    fun setNeighbours (Name : Node, Dist : Float) {
//        neighbours.put(Name,Dist)
//        connections = neighbours.size
//    }
//}
//
//fun setNeighbours(nodes : ArrayList<Node>, sheetName : String ) {
//    var data = com.example.linking_application_android.readSheet(
//        sheetName,
//        sheetsService,
//        google_api_key,
//        sheet_id
//    )
//    for (row in data!!) {
//        try {
//            continue
//        } catch (e : NumberFormatException) {
//            Log.e("Sheets Error", e.localizedMessage )
//        }
//    }
//}
//
//fun getRoutes(Start : String, End : String, Dist : Float) {
//    var nodes : ArrayList<Node>
//    for (i in 1..24) {
//        var nodeName = "G$i"
//        var newNode = Node(nodeName)
//        nodes.add(newNode)
//    }
//    for (j in 1..65) {
//        var nodeName = "NA$j"
//        var newNode = Node(nodeName)
//        nodes.add(newNode)
//    }
//
//
//
//}
//

fun getRoutes() : ArrayList<ArrayList<String>> {
    val routes = ArrayList<ArrayList<String>>()
    val route0 = arrayOf("NA20", "NA12").toCollection(ArrayList())
    val route1 = arrayOf("NA20", "GE2", "NA12").toCollection(ArrayList())
    val route2 = arrayOf("NA20", "GE2", "GE22", "NA12").toCollection(ArrayList())
    val route3 = arrayOf("NA20", "GE2", "NA19", "NA12").toCollection(ArrayList())
    val route4 = arrayOf("NA20", "GE2", "GE18", "NA12").toCollection(ArrayList())
    val route5 = arrayOf("NA20", "NA19", "NA12").toCollection(ArrayList())
    val route6 = arrayOf("NA20", "NA19", "GE2", "NA12").toCollection(ArrayList())
    val route7 = arrayOf("NA20", "NA19", "GE2", "GE22", "NA12").toCollection(ArrayList())
    val route8 = arrayOf("NA20", "NA19", "GE2", "GE18", "NA12").toCollection(ArrayList())
    routes.add(route0)
    routes.add(route1)
    routes.add(route2)
    routes.add(route3)
    routes.add(route4)
    routes.add(route5)
    routes.add(route6)
    routes.add(route7)
    routes.add(route8)
    return routes
}

fun displayRoute(route : ArrayList<String>) : String {
    var routeStr = ""
    var length : Int = route.size
    for (i in route.indices) {
        if (i == length-1) {
            routeStr = routeStr + route.get(i)
        } else {
            routeStr = routeStr + route.get(i) + " -> "
        }
    }
    return routeStr
}




