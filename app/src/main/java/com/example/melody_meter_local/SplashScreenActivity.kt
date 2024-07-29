package com.example.melody_meter_local

import android.content.Intent
import android.os.Bundle
import android.transition.Fade
import android.view.animation.AccelerateInterpolator
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.melody_meter_local.databinding.ActivityMainBinding
import com.example.melody_meter_local.databinding.ActivitySplashScreenBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SplashScreenActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imageView = binding.splashImage

        imageView.alpha = 0f //helps with animation
        imageView.animate().setDuration(1500).alpha(1f).withEndAction{
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Set enter and exit animations
            window.enterTransition = Fade()
            window.exitTransition = Fade()

            // Optionally, set duration and interpolator
            window.enterTransition.duration = 200
            window.enterTransition.interpolator = AccelerateInterpolator()
            window.exitTransition.duration = 200
            window.exitTransition.interpolator = AccelerateInterpolator()
            finish()
        }
    }
}