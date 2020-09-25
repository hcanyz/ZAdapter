package com.hcanyz.zadapter.test

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun testZAdapterActivity(view: View) {
        startActivity(Intent(this, TestZAdapterActivity::class.java))
    }

    fun testZListAdapterActivity(view: View) {
        startActivity(Intent(this, TestZListAdapterActivity::class.java))
    }

    fun testZAdapterInOtherLayoutActivity(view: View) {
        startActivity(Intent(this, TestZAdapterInOtherLayoutActivity::class.java))
    }

    fun testZAdapterThrottleActivity(view: View) {
        startActivity(Intent(this, TestZAdapterThrottleActivity::class.java))
    }
}
