package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.data.AppDatabase
import com.example.project.data.Booking
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityViewBookingHistoryBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ViewBookingHistoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewBookingHistoryBinding
    private val bookingList = mutableListOf<Booking>()
    private val roomMap = mutableMapOf<Int, RoomEntity>()
    private lateinit var bookingAdapter: BookingHistoryAdapter
    private lateinit var repository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewBookingHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository (kept for structure, not used in hardcoded mode)
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope, this)

        // Set up the top app bar
        setupTopAppBar()

        // Set up RecyclerView
        setupRecyclerView()

        // Set up filters
        setupFilters()

        // Load initial data (all bookings)
        loadBookingHistory(null)
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
        bookingAdapter = BookingHistoryAdapter(bookingList, roomMap)
        binding.historyRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.historyRecyclerView.adapter = bookingAdapter
    }

    private fun setupFilters() {
        val filters = listOf("Weekly", "Monthly", "Yearly")
        filters.forEach { filter ->
            val chip = Chip(this).apply {
                text = filter
                isCheckable = true
                setChipBackgroundColorResource(R.color.purple_200)
                setTextColor(resources.getColor(R.color.white, theme))
                setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) {
                        loadBookingHistory(filter)
                        binding.filterChipGroup.clearCheck()
                        this.isChecked = true
                    }
                }
            }
            binding.filterChipGroup.addView(chip)
        }
    }

    private fun loadBookingHistory(filter: String?) {
        lifecycleScope.launch {
            // Hardcoded room data
            val hardcodedRooms = listOf(
                RoomEntity(
                    id = 1,
                    roomnumber = "101",
                    block = "A",
                    floor = 1,
                    capacity = 4,
                    status = "Available",
                    category = "Meeting Room",
                    subCategory = "Small",
                    createdAt = Calendar.getInstance(),
                    updatedAt = Calendar.getInstance()
                ),
                RoomEntity(
                    id = 2,
                    roomnumber = "202",
                    block = "B",
                    floor = 2,
                    capacity = 10,
                    status = "Booked",
                    category = "Conference Room",
                    subCategory = "Large",
                    createdAt = Calendar.getInstance(),
                    updatedAt = Calendar.getInstance()
                )
            )

            // Populate roomMap with hardcoded rooms
            roomMap.clear()
            hardcodedRooms.forEach { roomMap[it.id] = it }

            // Hardcoded booking data
            val allBookings = listOf(
                Booking(
                    bookingid = 1,
                    roomid = 1,
                    userid = 1,
                    bookingdate = "25/03/2025", // Within the last week (assuming today is April 1, 2025)
                    starttime = "09:00",
                    endtime = "10:00",
                    purpose = "Team Meeting",
                    status = "Confirmed",
                    attendees = 4
                ),
                Booking(
                    bookingid = 2,
                    roomid = 2,
                    userid = 1,
                    bookingdate = "15/03/2025", // Within the last month
                    starttime = "14:00",
                    endtime = "16:00",
                    purpose = "Client Presentation",
                    status = "Pending",
                    attendees = 8
                ),
                Booking(
                    bookingid = 3,
                    roomid = 1,
                    userid = 1,
                    bookingdate = "10/06/2024", // Within the last year
                    starttime = "10:00",
                    endtime = "11:00",
                    purpose = "Planning Session",
                    status = "Cancelled",
                    attendees = 6
                ),
                Booking(
                    bookingid = 4,
                    roomid = 2,
                    userid = 1,
                    bookingdate = "01/01/2024", // Older than a year
                    starttime = "13:00",
                    endtime = "15:00",
                    purpose = "Workshop",
                    status = "Confirmed",
                    attendees = 10
                )
            )

            // Filter bookings based on the selected filter
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val filteredBookings = when (filter) {
                "Weekly" -> {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    val weekAgo = sdf.format(calendar.time)
                    allBookings.filter { sdf.parse(it.bookingdate)!! >= sdf.parse(weekAgo)!! }
                }
                "Monthly" -> {
                    calendar.add(Calendar.MONTH, -1)
                    val monthAgo = sdf.format(calendar.time)
                    allBookings.filter { sdf.parse(it.bookingdate)!! >= sdf.parse(monthAgo)!! }
                }
                "Yearly" -> {
                    calendar.add(Calendar.YEAR, -1)
                    val yearAgo = sdf.format(calendar.time)
                    allBookings.filter { sdf.parse(it.bookingdate)!! >= sdf.parse(yearAgo)!! }
                }
                else -> allBookings // All bookings
            }

            // Populate bookingList with filtered bookings
            bookingList.clear()
            bookingList.addAll(filteredBookings)
            bookingAdapter.notifyDataSetChanged()
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // RecyclerView Adapter
    inner class BookingHistoryAdapter(
        private val bookings: List<Booking>,
        private val roomMap: Map<Int, RoomEntity>
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<BookingHistoryAdapter.BookingViewHolder>() {

        inner class BookingViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val roomNameText: android.widget.TextView = itemView.findViewById(R.id.roomNameText)
            val bookingDateText: android.widget.TextView = itemView.findViewById(R.id.bookingDateText)
            val statusText: android.widget.TextView = itemView.findViewById(R.id.statusText)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_booking_history, parent, false)
            return BookingViewHolder(view)
        }

        override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
            val booking = bookings[position]
            val room = roomMap[booking.roomid]
            holder.roomNameText.text = room?.roomnumber ?: "C - 18"
            holder.bookingDateText.text = "Date: ${booking.bookingdate}"
            holder.statusText.text = "Status: ${booking.status}" // Added status display
        }

        override fun getItemCount(): Int = bookings.size
    }
}