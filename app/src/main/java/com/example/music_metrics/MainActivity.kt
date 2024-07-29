package com.example.music_metrics

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.music_metrics.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup navigation based on orientation
        if (binding.bottomNavBar != null) {
            // Portrait mode
            binding.bottomNavBar?.setupWithNavController(navController)
        } else {
            // Landscape mode
            setupLandscapeNavigation()
        }

        // Add destination changed listener for debugging
        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("Navigation", "Navigated to ${destination.label}")
        }

    }

    private fun setupLandscapeNavigation() {
        binding.btnHome?.setOnClickListener {
            setActiveButton(it as ImageButton)
            navController.navigate(R.id.navigation_home)
        }
        binding.btnSearch?.setOnClickListener {
            setActiveButton(it as ImageButton)
            navController.navigate(R.id.navigation_search)
        }
        binding.btnProfile?.setOnClickListener {
            setActiveButton(it as ImageButton)
            navController.navigate(R.id.navigation_profile)
        }

        // Set initial active button
        setActiveButton(binding.btnHome as ImageButton)
    }

    private fun setActiveButton(activeButton: ImageButton) {
        // Reset all buttons to inactive state
        binding.btnHome?.background = ContextCompat.getDrawable(this, R.color.transparent)
        binding.btnSearch?.background = ContextCompat.getDrawable(this, R.color.transparent)
        binding.btnProfile?.background = ContextCompat.getDrawable(this, R.color.transparent)

        // Set active button color
        activeButton.background = ContextCompat.getDrawable(this, R.drawable.active_indicator)
    }
}