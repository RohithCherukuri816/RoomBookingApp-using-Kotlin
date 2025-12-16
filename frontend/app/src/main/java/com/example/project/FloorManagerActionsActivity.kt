package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityFloorManagerActionsBinding

class FloorManagerActionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFloorManagerActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFloorManagerActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the top app bar with a back navigation action
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }


        // Set up click listeners for each card
        binding.approveRejectCard.setOnClickListener {
            startActivity(Intent(this, ApproveRejectActivity::class.java))
        }

        binding.occupancyDashboardCard.setOnClickListener {
            startActivity(Intent(this, OccupancyDashboardActivity::class.java))
        }

        binding.allocateRoomsCard.setOnClickListener {
            startActivity(Intent(this, AllocateRoomsActivity::class.java))
        }
    }
}