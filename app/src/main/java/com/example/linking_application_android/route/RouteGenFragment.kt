package com.example.linking_application_android.route

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.linking_application_android.MapsActivity
import com.example.linking_application_android.R
import com.google.android.gms.maps.model.Marker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.ArrayList

class RouteGenFragment : DialogFragment() {
    private lateinit var closeFab // Close dialog
            : FloatingActionButton
    private lateinit var nextFab // Go to route
            : FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_route_gen, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        closeFab = view.findViewById(R.id.closefab)
        nextFab = view.findViewById(R.id.nextfab)

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

    }
}
