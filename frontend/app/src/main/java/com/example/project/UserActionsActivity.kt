package com.example.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityUserActionsBinding

class UserActionsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserActionsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserActionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set up the top app bar with a back navigation action
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }

        // Logout button action
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.menu_logout) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            } else {
                false
            }
        }

        // Set up click listeners for each card
        binding.bookRoomCard.setOnClickListener {
            startActivity(Intent(this, BookRoomActivity::class.java))
        }

        binding.viewBookingStatusCard.setOnClickListener {
            startActivity(Intent(this, ViewBookingStatusActivity::class.java))
        }

        binding.viewBookingHistoryCard.setOnClickListener {
            startActivity(Intent(this, ViewBookingHistoryActivity::class.java))
        }
    }
}
