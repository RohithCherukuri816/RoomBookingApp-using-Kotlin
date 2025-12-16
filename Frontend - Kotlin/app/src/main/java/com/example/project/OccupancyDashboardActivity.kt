package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project.data.AppDatabase
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityOccupancyDashboardBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OccupancyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOccupancyDashboardBinding
    private lateinit var repository: RoomRepository
    private val availableRooms = mutableListOf<RoomEntity>()
    private val occupiedRooms = mutableListOf<RoomEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOccupancyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope,this)

        // Set up the top app bar
        setupTopAppBar()

        // Load occupancy data
        loadOccupancyData()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }
    }

    private fun logout() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun loadOccupancyData() {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val today = sdf.format(Calendar.getInstance().time)

            // Fetch all rooms
            val roomsResult = repository.loadAvailableRooms() // Use the correct method
            when (roomsResult) {
                is RoomRepository.Result.Success -> {
                    val allRooms = roomsResult.data

                    // Fetch bookings for today
                    val bookingsResult = repository.getBookingsByDate(today)
                    when (bookingsResult) {
                        is RoomRepository.Result.Success -> {
                            val bookedRoomIds = bookingsResult.data
                                //.filter { it.status == "Confirmed" || it.status == "Pending" }
                                .map { it.roomid }
                                .toSet()

                            availableRooms.clear()
                            occupiedRooms.clear()

                            allRooms.forEach { room ->
                                if (room.id in bookedRoomIds) {
                                    occupiedRooms.add(room)
                                } else {
                                    availableRooms.add(room)
                                }
                            }

                            updateDashboardUI()
                        }
                        is RoomRepository.Result.Error -> {
                            Log.e("OccupancyDashboard", "Failed to load bookings: ${bookingsResult.exception.message}")
                            showSnackbar("Failed to load bookings: ${bookingsResult.exception.message}")
                            updateDashboardUIWithRoomsOnly(allRooms)
                        }
                    }
                }
                is RoomRepository.Result.Error -> {
                    Log.e("OccupancyDashboard", "Failed to load rooms: ${roomsResult.exception.message}")
                    showSnackbar("Failed to load rooms: ${roomsResult.exception.message}")
                }
            }
        }
    }

    private fun updateDashboardUI() {
        // Update Available Rooms
        binding.availableRoomsCount.text = "Available Rooms: ${availableRooms.size}"
        val availableText = availableRooms.joinToString("\n") { it.roomnumber }
        binding.availableRoomsList.text = if (availableText.isNotEmpty()) availableText else "None"

        // Update Occupied Rooms
        binding.occupiedRoomsCount.text = "Occupied Rooms: ${occupiedRooms.size}"
        val occupiedText = occupiedRooms.joinToString("\n") { it.roomnumber }
        binding.occupiedRoomsList.text = if (occupiedText.isNotEmpty()) occupiedText else "None"
    }

    private fun updateDashboardUIWithRoomsOnly(rooms: List<RoomEntity>) {
        availableRooms.clear()
        availableRooms.addAll(rooms)
        occupiedRooms.clear()

        updateDashboardUI()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}