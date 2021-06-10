package com.example.linking_application_android.compass

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.linking_application_android.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class CompassBottomSheetFragment : BottomSheetDialogFragment() {

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.compass_bottom_sheet_layout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpViews()
    }

    private fun setUpViews() {
        // We can have cross button on the top right corner for providing elemnet to dismiss the bottom sheet
        //iv_close.setOnClickListener { dismissAllowingStateLoss() }


    }

    companion object {
        @JvmStatic
        fun newInstance(bundle: Bundle): CompassBottomSheetFragment {
            val fragment = CompassBottomSheetFragment()
            fragment.arguments = bundle
            return fragment
        }
    }
}
