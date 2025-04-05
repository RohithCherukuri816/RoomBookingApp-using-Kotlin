package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityAdminActionsBinding

class AdminActionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAdminActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the top app bar with logout menu
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        // Set up click listeners for each card
        binding.manageRoomsCard.setOnClickListener {
            startActivity(Intent(this, ManageRoomsActivity::class.java))
        }

        binding.manageAmenitiesCard.setOnClickListener {
            startActivity(Intent(this, ManageAmenitiesActivity::class.java))
        }

        binding.assignRolesCard.setOnClickListener {
            startActivity(Intent(this, AssignRolesActivity::class.java))
        }

        binding.viewReportsCard.setOnClickListener {
            startActivity(Intent(this, ViewReportsActivity::class.java))
        }
    }

    // Inflate the menu for the top app bar
    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    // Logout function to clear session and return to login page
    private fun logout() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply() // Clear all session data
        startActivity(Intent(this, MainActivity::class.java))
        finish() // Close this activity
    }
}