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

        // Initialize database and repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope,this)

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
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val userid = prefs.getInt("userid", 0)
            if (userid == 0) {
                Log.e("ViewBookingHistory", "User not logged in")
                showSnackbar("User not logged in. Please log in again.")
                return@launch
            }

            // Fetch all rooms
            val roomsResult = repository.loadAvailableRooms() // Use the correct method
            when (roomsResult) {
                is RoomRepository.Result.Success -> {
                    roomMap.clear()
                    roomsResult.data.forEach { roomMap[it.id] = it }
                }
                is RoomRepository.Result.Error -> {
                    Log.e("ViewBookingHistory", "Failed to load rooms: ${roomsResult.exception.message}")
                    showSnackbar("Failed to load rooms: ${roomsResult.exception.message}")
                }
            }

            // Fetch bookings based on filter
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val bookingsResult = when (filter) {
                "Weekly" -> {
                    calendar.add(Calendar.WEEK_OF_YEAR, -1)
                    repository.getBookingsByUserAndDate(userid, sdf.format(calendar.time))
                }
                "Monthly" -> {
                    calendar.add(Calendar.MONTH, -1)
                    repository.getBookingsByUserAndDate(userid, sdf.format(calendar.time))
                }
                "Yearly" -> {
                    calendar.add(Calendar.YEAR, -1)
                    repository.getBookingsByUserAndDate(userid, sdf.format(calendar.time))
                }
                else -> repository.getBookingsByUserAndDate(userid, "") // All bookings
            }

            when (bookingsResult) {
                is RoomRepository.Result.Success -> {
                    bookingList.clear()
                    bookingList.addAll(bookingsResult.data)
                    bookingAdapter.notifyDataSetChanged()
                }
                is RoomRepository.Result.Error -> {
                    bookingList.clear()
                    bookingAdapter.notifyDataSetChanged()
                    Log.e("ViewBookingHistory", "Failed to load bookings: ${bookingsResult.exception.message}")
                    showSnackbar("Failed to load bookings: ${bookingsResult.exception.message}")
                }
            }
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
            holder.roomNameText.text = room?.roomnumber ?: "Unknown Room"
            holder.bookingDateText.text = "Date: ${booking.bookingdate}"

        }

        override fun getItemCount(): Int = bookings.size
    }
}