package com.example.linking_application_android.route

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.linking_application_android.MapsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*
import android.graphics.Color;
import android.widget.TextView
import com.example.linking_application_android.R
import androidx.appcompat.widget.AppCompatButton
import android.util.Log
import android.widget.Toast
import com.google.android.gms.maps.model.Marker


class RouteGenFragment : DialogFragment() {
    private lateinit var closeFab // Close dialog
            : FloatingActionButton
    private lateinit var rerunFab // Get new route fab
            : FloatingActionButton
    private lateinit var nextFab // Go to route
            : FloatingActionButton
    private lateinit var startBut
            : AppCompatButton
    private lateinit var endBut
            : AppCompatButton
    private lateinit var dstBut
            : AppCompatButton

    private var startText: TextView? = null
    private var endText: TextView? = null
    private var dstText: TextView? = null
    private var routeText: TextView? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.fragment_route_gen, container, false)
        // Set transparent background
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeFab = view.findViewById(R.id.closefab)
        nextFab = view.findViewById(R.id.nextfab)
        rerunFab = view.findViewById(R.id.rerunfab)
        startBut = view.findViewById(R.id.startbut)
        endBut = view.findViewById(R.id.endbut)
        dstBut = view.findViewById(R.id.dstbut)
        startText = view.findViewById(R.id.startText)
        endText = view.findViewById(R.id.endText)
        dstText = view.findViewById(R.id.dstText)
        routeText = view.findViewById(R.id.routeText)

        var selectedRoute // ArrayList of landmarks
                : ArrayList<String>? = null
        var allRoutes // ArrayList of routes
                : ArrayList<ArrayList<String>> = getRoutes()
        var index : Int = 0 // Index of route

        closeFab.setOnClickListener(View.OnClickListener {
            dismiss()
        })

        rerunFab.setOnClickListener(View.OnClickListener {
            var start = startText!!.text.toString().uppercase()
            var end = endText!!.text.toString().uppercase()
            var dist = dstText!!.text.toString().uppercase()
            if (start == "NA20" && end == "NA12" && dist == "500") {
                if (selectedRoute == null) {
                    rerunFab.setImageDrawable(getResources().getDrawable(R.drawable.fab_rerun))
                }
                selectedRoute = allRoutes.get(index)
                if (index == allRoutes.size -1) {
                    index = 0
                } else {
                    index++
                }
                routeText!!.text = displayRoute(selectedRoute!!)
            }

        })

        nextFab.setOnClickListener(View.OnClickListener {
            if (selectedRoute != null) {
                var newRoute = selectedRoute!!
                (activity as MapsActivity?)!!.setRoute(newRoute)
                dismiss()
            } else {
                Toast.makeText(this.context, "Please set a route first", Toast.LENGTH_LONG)
            }
        })

        startBut.setOnClickListener(View.OnClickListener {
            startText!!.text = "NA20"
        })

        endBut.setOnClickListener(View.OnClickListener {
            endText!!.text = "NA12"
        })

        dstBut.setOnClickListener(View.OnClickListener {
            dstText!!.text = "500"
        })

    }
}