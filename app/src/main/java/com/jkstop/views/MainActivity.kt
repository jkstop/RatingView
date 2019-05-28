package com.jkstop.views

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.vRating

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vRating.currentRate = 3
        vRating.setOnRateChangeListener {
            Toast.makeText(this, "Rate is $it", Toast.LENGTH_LONG).show()
        }
    }
}