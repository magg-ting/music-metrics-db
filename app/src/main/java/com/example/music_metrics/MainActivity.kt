package com.example.music_metrics

import android.content.res.Configuration
import android.os.Bundle
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
        val orientation = resources.configuration.orientation
        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            binding.bottomNavBar?.setupWithNavController(navController)
        } else {
            val btnHome = binding.btnHome
            val btnSearch = binding.btnSearch
            val btnProfile = binding.btnProfile

            btnHome?.setOnClickListener {
                setActiveButton(btnHome)
                navController.navigate(R.id.navigation_home)
            }
            btnSearch?.setOnClickListener {
                setActiveButton(btnSearch)
                navController.navigate(R.id.navigation_search)
            }
            btnProfile?.setOnClickListener {
                setActiveButton(btnProfile)
                navController.navigate(R.id.navigation_profile)
            }

        }

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