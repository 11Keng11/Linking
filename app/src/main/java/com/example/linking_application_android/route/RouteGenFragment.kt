package com.example.linking_application_android.route

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.linking_application_android.R
import com.example.linking_application_android.changeVisibility
import com.google.android.material.floatingactionbutton.FloatingActionButton

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
    }
}
