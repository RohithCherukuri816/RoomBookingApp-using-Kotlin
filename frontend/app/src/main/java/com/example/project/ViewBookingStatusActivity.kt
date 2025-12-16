package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.data.AppDatabase
import com.example.project.data.Booking
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityViewBookingStatusBinding
import com.example.project.network.RetrofitClient
import com.example.project.network.WebSocketClient
import com.example.project.repository.RoomRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class ViewBookingStatusActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBookingStatusBinding
    private val bookingList = mutableListOf<Booking>()
    private val roomMap = mutableMapOf<Int, RoomEntity>()
    private lateinit var bookingAdapter: BookingStatusAdapter
    private lateinit var repository: RoomRepository
    private lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBookingStatusBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository (not needed for hardcoded testing, but kept for structure)
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope, this)

        // Get credentials from SharedPreferences (optional for testing)
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val username = prefs.getString("username", "test_user") ?: "test_user"
        val password = prefs.getString("password", "test_pass") ?: "test_pass"

        // Set credentials in RetrofitClient (optional for testing)
        RetrofitClient.setCredentials(username, password)

        // Initialize WebSocketClient (optional for testing, can be bypassed)
        webSocketClient = WebSocketClient(
            onMessageReceived = { _ -> loadBookings() },
            username = username,
            password = password
        )
        webSocketClient.connect()

        // Set up the top app bar
        setupTopAppBar()

        // Set up RecyclerView
        setupRecyclerView()

        // Load hardcoded bookings
        loadBookings()

        // Set up refresh FAB
        setupRefreshFab()
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

    private fun setupRecyclerView() {
        bookingAdapter = BookingStatusAdapter(bookingList, roomMap)
        binding.bookingStatusRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.bookingStatusRecyclerView.adapter = bookingAdapter
    }

    private fun setupRefreshFab() {
        binding.refreshFab.setOnClickListener {
            val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 300 }
            val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 300 }
            binding.bookingStatusRecyclerView.startAnimation(fadeOut)
            binding.bookingStatusRecyclerView.visibility = View.GONE
            binding.loadingPlaceholder.startAnimation(fadeIn)
            binding.loadingPlaceholder.visibility = View.VISIBLE
            loadBookings()
        }
    }

    private fun loadBookings() {
        lifecycleScope.launch {
            // Hardcoded room data
            val hardcodedRooms = listOf(
                RoomEntity(
                    id = 1,
                    roomnumber = "A - 01",
                    block = "A",
                    floor = 1,
                    capacity = 4,
                    status = "Available",
                    category = "Meeting Room",
                    subCategory = "Small",
                    createdAt = Calendar.getInstance(),
                    updatedAt = Calendar.getInstance()
                ),

            )

            // Populate roomMap with hardcoded rooms
            roomMap.clear()
            hardcodedRooms.forEach { roomMap[it.id] = it }

            // Hardcoded booking data matching Booking.kt
            val hardcodedBookings = listOf(
                Booking(
                    bookingid = 1,
                    roomid = 1,
                    userid = 1,
                    bookingdate = "2025-04-02",
                    starttime = "09:00", // Provide a value or null
                    endtime = "10:00",   // Provide a value or null
                    purpose = "Team Meeting", // Provide a value or null
                    status = "Confirmed",
                    attendees = 4 // Provide a value or null
                ),
                Booking(
                    bookingid = 2,
                    roomid = 2,
                    userid = 1,
                    bookingdate = "2025-04-03",
                    starttime = "14:00", // Provide a value or null
                    endtime = "16:00",   // Provide a value or null
                    purpose = "Client Presentation", // Provide a value or null
                    status = "Pending",
                    attendees = 8 // Provide a value or null
                )
            )

            // Populate bookingList with hardcoded bookings
            bookingList.clear()
            bookingList.addAll(hardcodedBookings)
            bookingAdapter.notifyDataSetChanged()

            // Transition to content view
            transitionToContent()
        }
    }

    private fun transitionToContent() {
        val fadeOut = AlphaAnimation(1f, 0f).apply { duration = 300 }
        val fadeIn = AlphaAnimation(0f, 1f).apply { duration = 300 }
        binding.loadingPlaceholder.startAnimation(fadeOut)
        binding.loadingPlaceholder.visibility = View.GONE
        binding.bookingStatusRecyclerView.startAnimation(fadeIn)
        binding.bookingStatusRecyclerView.visibility = View.VISIBLE
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // RecyclerView Adapter
    inner class BookingStatusAdapter(
        private val bookings: List<Booking>,
        private val roomMap: Map<Int, RoomEntity>
    ) : RecyclerView.Adapter<BookingStatusAdapter.BookingViewHolder>() {

        inner class BookingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val roomNameText: TextView = itemView.findViewById(R.id.roomNameText)
            val bookingDateText: TextView = itemView.findViewById(R.id.bookingDateText)
            val statusText: TextView = itemView.findViewById(R.id.statusText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booking_status, parent, false)
            return BookingViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
            val booking = bookings[position]
            val room = roomMap[booking.roomid]
            holder.roomNameText.text = room?.roomnumber ?: "C - 18"
            holder.bookingDateText.text = "Date: ${booking.bookingdate}"
            holder.statusText.text = booking.status ?: "Pending"
            holder.statusText.setTextColor(
                when (holder.statusText.text.toString()) {
                    "Confirmed" -> resources.getColor(R.color.teal_200, theme)
                    "Pending" -> resources.getColor(R.color.purple_200, theme)
                    "Cancelled" -> resources.getColor(R.color.red, theme)
                    else -> resources.getColor(R.color.white, theme)
                }
            )
        }

        override fun getItemCount(): Int = bookings.size
    }
}