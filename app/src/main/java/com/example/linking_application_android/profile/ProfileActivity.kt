package com.example.linking_application_android.profile

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.linking_application_android.R
import com.example.linking_application_android.MapsActivity
import com.example.linking_application_android.changeVisibility
import com.google.android.material.floatingactionbutton.FloatingActionButton

class ProfileActivity : AppCompatActivity() {
    private lateinit var backFab // Test bluetooth scanning
            : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        backFab = findViewById(R.id.backFab)

        backFab.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, MapsActivity::class.java);
            startActivity(intent);
        })
    }
}