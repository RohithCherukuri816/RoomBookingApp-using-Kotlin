package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.data.AppDatabase
import com.example.project.data.Booking
import com.example.project.databinding.ActivityApproveRejectBinding
import com.example.project.repository.RoomRepository
import com.example.project.repository.RoomRepository.Result
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.launch
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ApproveRejectActivity : AppCompatActivity() {

    companion object {
        const val STATUS_APPROVED = "Approved"
        const val STATUS_REJECTED = "Rejected"
        const val STATUS_PENDING = "Pending"
        const val FLOOR_MANAGER_ROLE_ID = 2
        private const val TAG = "ApproveRejectActivity" // Log tag
    }

    private lateinit var binding: ActivityApproveRejectBinding
    private lateinit var repository: RoomRepository
    private lateinit var bookingAdapter: BookingRequestAdapter

    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityApproveRejectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "Activity started") // Log when activity starts

        if (!userHasFloorManagerRole()) {
            Log.e(TAG, "Access denied - User does not have Floor Manager role")
            showError("Access denied - insufficient permissions")
            finish()
            return
        }

        repository = RoomRepository(AppDatabase.getDatabase(this), lifecycleScope, this)

        setupUI()
        loadData()
    }

    private fun setupUI() {
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    Log.d(TAG, "Logout clicked") // Log logout action
                    logoutUser()
                    true
                }
                else -> false
            }
        }

        bookingAdapter = BookingRequestAdapter(mutableListOf()) { booking, action ->
            Log.d(TAG, "Booking action clicked: $action on Booking ID: ${booking.bookingid}") // Log click
            showConfirmationDialog(booking, action)
        }

        binding.requestsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ApproveRejectActivity)
            adapter = bookingAdapter
        }
    }

    private fun loadData() {
        Log.d(TAG, "Loading pending bookings...") // Log data loading start
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Error loading bookings: ${throwable.localizedMessage}", throwable)
            showError("Failed to load: ${throwable.localizedMessage}")
        }

        lifecycleScope.launch(exceptionHandler) {
            when (val result = repository.getPendingBookings()) {
                is Result.Success -> {
                    Log.d(TAG, "Successfully loaded ${result.data.size} bookings")
                    if (result.data.isEmpty()) {
                        Log.d(TAG, "No pending bookings found")
                        showSnackbar("No pending bookings")
                    } else {
                        bookingAdapter.updateRequests(result.data.toMutableList())
                    }
                }
                is Result.Error -> {
                    Log.e(TAG, "Error fetching bookings: ${result.exception.localizedMessage}")
                    showError("Server error")
                }
            }
        }
    }

    private fun showConfirmationDialog(booking: Booking, action: String) {
        Log.d(TAG, "Showing confirmation dialog for action: $action on Booking ID: ${booking.bookingid}")
        val message = """
            Booking #${booking.bookingid}
            User ID: ${booking.userid}
            Room ID: ${booking.roomid}
            Time: ${booking.starttime ?: "N/A"}
            
            Are you sure you want to $action this booking?
        """.trimIndent()

        MaterialAlertDialogBuilder(this)
            .setTitle("Confirm Action")
            .setMessage(message)
            .setPositiveButton(action) { _, _ ->
                Log.d(TAG, "User confirmed action: $action on Booking ID: ${booking.bookingid}")
                processBookingAction(booking, action)
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "User cancelled action: $action on Booking ID: ${booking.bookingid}")
            }
            .show()
    }

    private fun processBookingAction(booking: Booking, action: String) {
        lifecycleScope.launch {
            when (val result = repository.updateBooking(booking.copy(status = action))) {
                is Result.Success -> {
                    showSnackbar("Booking $action successfully!")
                    loadData()
                }
                is Result.Error -> {
                    val errorMsg = when {
                        result.exception.message?.contains("403") == true ->
                            "Permission denied. Contact administrator."
                        result.exception.message?.contains("401") == true ->
                            "Session expired. Please login again."
                        else -> "Failed to $action booking: ${result.exception.message}"
                    }
                    showError(errorMsg)
                }
            }
        }
    }

    private fun userHasFloorManagerRole(): Boolean {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val roleId = prefs.getInt("userRole", 2)
        Log.d(TAG, "User role check: Current role ID = $roleId, Required = $FLOOR_MANAGER_ROLE_ID")
        return roleId == FLOOR_MANAGER_ROLE_ID
    }

    private fun logoutUser() {
        Log.d(TAG, "Logging out user...")
        getSharedPreferences("AppPrefs", MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showSnackbar(message: String) {
        Log.d(TAG, "Showing Snackbar: $message")
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    private fun showError(message: String) {
        Log.e(TAG, "Error: $message")
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setBackgroundTint(resources.getColor(android.R.color.holo_red_light, theme))
            .setAction("Retry") { loadData() }
            .show()
    }
}