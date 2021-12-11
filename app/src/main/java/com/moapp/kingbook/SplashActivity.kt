package com.moapp.kingbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        startLoading()
    }
    private fun startLoading(){
        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({finish()}, 3000)
    }

}