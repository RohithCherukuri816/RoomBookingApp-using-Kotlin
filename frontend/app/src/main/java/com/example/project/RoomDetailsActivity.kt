package com.example.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.project.data.AppDatabase
import com.example.project.data.Booking
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityRoomDetailsBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class RoomDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRoomDetailsBinding
    private lateinit var selectedRoom: RoomEntity
    private lateinit var selectedDate: String
    private lateinit var repository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRoomDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the selected room and date from the intent
        selectedRoom = intent.getParcelableExtra<RoomEntity>("SELECTED_ROOM")
            ?: throw IllegalStateException("Selected room not found in intent")
        selectedDate = intent.getStringExtra("SELECTED_DATE")
            ?: throw IllegalStateException("Selected date not found in intent")

        // Initialize repository
        repository = RoomRepository(AppDatabase.getDatabase(this), lifecycleScope,this)

        // Set up the UI
        setupUI()
        setupToolbar()
        setupAmenities()
        setupBookingButton()
    }

    private fun setupUI() {
        // Set room details
        binding.roomName.text = selectedRoom.roomnumber
        binding.roomType.text = selectedRoom.category
        binding.capacityValue.text = "Capacity: ${selectedRoom.capacity}"

        // Set background based on room type
        binding.roomCard.background = ContextCompat.getDrawable(this, when (selectedRoom.category) {
            "Classroom" -> R.drawable.classroom_bg
            "Lab" -> R.drawable.lab_bg
            else -> R.drawable.activity_room_bg
        })
    }

    private fun setupToolbar() {
        // Set up the toolbar with back navigation and logout
        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.toolbar.setOnMenuItemClickListener {
            if (it.itemId == R.id.action_logout) {
                logoutUser()
                true
            } else {
                false
            }
        }
    }

    private fun setupAmenities() {
        // Display amenities
        val amenities = selectedRoom.subCategory ?: "No amenities listed"
        binding.amenitiesList.text = "Amenities: $amenities"
    }

    private fun setupBookingButton() {
        // Set up the "Book Now" button
        binding.btnBookNow.setOnClickListener {
            if (validateInputs()) {
                showConfirmationDialog()
            }
        }
    }

    private fun validateInputs(): Boolean {
        // Validate the audience count input
        val audience = binding.audienceCount.editText?.text.toString()
        if (audience.isEmpty() || audience.toInt() < 1) {
            Toast.makeText(this, "Please enter a valid audience count", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun showConfirmationDialog() {
        // Show a confirmation dialog with booking details
        val audience = binding.audienceCount.editText?.text.toString().toInt()
        val purpose = binding.purpose.editText?.text.toString()

        // Recommend a room for activity rooms
        val recommendedRoom = if (selectedRoom.category == "Activity Room") {
            getRecommendedActivityRoom(audience)
        } else {
            selectedRoom.roomnumber
        }

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Booking")
            .setMessage(
                "Recommended Room: $recommendedRoom\n\n" +
                        "Purpose: $purpose\n" +
                        "Audience: $audience\n" +
                        "Date: $selectedDate"
            )
            .setPositiveButton("Confirm") { _, _ ->
                completeBooking(purpose, audience)
            }
            .setNegativeButton("Cancel", null)
            .show()
        }

    private fun completeBooking(purpose: String, audience: Int) {
        // 1. Use proper date parsing/formatting
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

        try {
            // 2. Parse selectedDate and convert to server format
            val parsedDate = inputFormat.parse(selectedDate)
            val serverFormattedDate = outputFormat.format(parsedDate)

            // 3. Create booking with proper date format
            val booking = Booking(
                bookingid = 0,
                roomid = selectedRoom.id,
                userid = getUserid(),
                bookingdate = serverFormattedDate,  // Use formatted date
                starttime = "09:00:00",
                endtime = "10:00:00",
                purpose = purpose,
                status = "Pending",  // Changed from "CONFIRMED"
                attendees = audience
            )

            // 4. Proceed with booking
            lifecycleScope.launch {
                when (val result = repository.createBooking(booking)) {
                    is RoomRepository.Result.Success -> {
                        Toast.makeText(this@RoomDetailsActivity,
                            "Booking request submitted!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    is RoomRepository.Result.Error -> {
                        Toast.makeText(this@RoomDetailsActivity,
                            "Error: ${result.exception.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(this,
                "Invalid date format: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun getUserid(): Int {
        // Retrieve user ID from SharedPreferences
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        return prefs.getInt("userId", 0)
    }

    private fun logoutUser() {
        // Clear user session and navigate to MainActivity
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finishAffinity()
    }

    private fun getRecommendedActivityRoom(audience: Int): String {
        // Recommend a room based on audience count for activity rooms
        return when {
            audience < 8 -> "GTIDS Board Room"
            audience in 8..24 -> "Old Board Room"
            audience in 25..80 -> "B-05"
            audience > 80 -> "Gallery Hall 2"
            else -> "Gallery Hall 1"
        }
    }
}