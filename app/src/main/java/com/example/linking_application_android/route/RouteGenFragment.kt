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



class RouteGenFragment : DialogFragment() {
    private lateinit var closeFab // Close dialog
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
        startBut = view.findViewById(R.id.startbut)
        endBut = view.findViewById(R.id.endbut)
        dstBut = view.findViewById(R.id.dstbut)
        startText = view.findViewById(R.id.startText)
        endText = view.findViewById(R.id.endText)
        dstText = view.findViewById(R.id.dstText)

        closeFab.setOnClickListener(View.OnClickListener {
            dismiss()
        })

        nextFab.setOnClickListener(View.OnClickListener {
            val route = ArrayList<String>()
            route.add("NA20")
            route.add("NA19")
            route.add("G2")
            route.add("NA12")
            (activity as MapsActivity?)!!.setRoute(route)
            dismiss()
        })

        startBut.setOnClickListener(View.OnClickListener {
            startText!!.text = "NA20"
        })

        endBut.setOnClickListener(View.OnClickListener {
            endText!!.text = "NA12"
        })

        dstBut.setOnClickListener(View.OnClickListener {
            dstText!!.text = "1000m"
        })

    }
}