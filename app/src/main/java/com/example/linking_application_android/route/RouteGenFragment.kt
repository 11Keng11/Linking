package com.example.linking_application_android.route

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.example.linking_application_android.R
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import com.example.linking_application_android.MapsActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*


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
//    private var themeSpin: Spinner? = null

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
        closeFab = view.findViewById(R.id.closeFab)
        nextFab = view.findViewById(R.id.nextFab)
        rerunFab = view.findViewById(R.id.rerunfab)
        startBut = view.findViewById(R.id.startBut)
        endBut = view.findViewById(R.id.endBut)
        dstBut = view.findViewById(R.id.dstBut)
        startText = view.findViewById(R.id.startText)
        endText = view.findViewById(R.id.endText)
        dstText = view.findViewById(R.id.dstText)
        routeText = view.findViewById(R.id.routeText)
//        themeSpin = view.findViewById(R.id.themeSpin)
//
//        val themes = resources.getStringArray(R.array.Themes)
//
//        if (themeSpin != null) {
//            val adapter = ArrayAdapter(this,
//                android.R.layout.simple_spinner_item, themes)
//            themeSpin.adapter = adapter
//
//            themeSpin.onItemSelectedListener = object :
//                AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(parent: AdapterView<*>,
//                                            view: View, position: Int, id: Long) {
//                    Toast.makeText(this,
//                        "Selected Option: " + themes[position], Toast.LENGTH_SHORT).show()
//                }
//
//                override fun onNothingSelected(parent: AdapterView<*>) {
//                    // write code to perform some action
//                }
//            }
//        }

        var selectedRoute // ArrayList of landmarks
                : ArrayList<String>? = null
        var allRoutes // ArrayList of routes
                : ArrayList<ArrayList<String>> = getRoutes()
        var index  = 0 // Index of route

//        val themeContent = arrayOf("Nature","Play", "Exercise").toCollection(ArrayList())

        // Initializing an ArrayAdapter
//        if (themeSpin != null) {
//            val adapter = ArrayAdapter(
//                this,
//                R.layout.spinner_item, themeContent
//            )
//            themeSpin.adapter = adapter
//        }


        closeFab.setOnClickListener(View.OnClickListener {
            dismiss()
        })

        rerunFab.setOnClickListener(View.OnClickListener {
            var start = startText!!.text.toString().uppercase()
            var end = endText!!.text.toString().uppercase()
            var dist = dstText!!.text.toString().uppercase()
            if (start == "GE26" || end == "GE25" || dist == "1000") {
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
            startText!!.text = "GE26"
        })

        endBut.setOnClickListener(View.OnClickListener {
            endText!!.text = "GE25"
        })

        dstBut.setOnClickListener(View.OnClickListener {
            dstText!!.text = "100"
        })

    }
}