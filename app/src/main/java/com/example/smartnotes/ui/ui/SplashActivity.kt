package com.smartnotes.ui.ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.smartnotes.databinding.ActivitySplashBinding
import com.smartnotes.utils.smoothTransition

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ObjectAnimator.ofFloat(binding.tvAppName, "alpha", 0f, 1f).apply {
            duration = 1200
            start()
        }

        binding.tvAppName.postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            smoothTransition()
            finish()
        }, 1800)
    }
}
