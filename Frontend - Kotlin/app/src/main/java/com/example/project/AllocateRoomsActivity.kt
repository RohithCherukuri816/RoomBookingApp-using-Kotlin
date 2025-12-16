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
import com.example.project.databinding.ActivityAllocateRoomsBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.Date



class AllocateRoomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllocateRoomsBinding
    private val availableRooms = mutableListOf<RoomEntity>()
    private lateinit var roomAdapter: AllocateRoomAdapter
    private lateinit var repository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllocateRoomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope,this)

        // Top App Bar setup
        setupTopAppBar()

        // RecyclerView setup
        setupRecyclerView()

        // Load initial data
        loadAvailableRooms()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_logout) {
                logoutUser()
                true
            } else {
                false
            }
        }
    }

    private fun logoutUser() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun setupRecyclerView() {
        roomAdapter = AllocateRoomAdapter(availableRooms, this::allocateRoom)
        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomsRecyclerView.adapter = roomAdapter
    }

    private fun loadAvailableRooms() {
        lifecycleScope.launch {
            // Fetch available rooms from the repository
            val roomsResult = repository.loadAvailableRooms()
            when (roomsResult) {
                is RoomRepository.Result.Success -> {
                    // Get today's bookings
                    val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Calendar.getInstance().time)
                    val bookingsResult = repository.getBookingsByDate(today)
                    when (bookingsResult) {
                        is RoomRepository.Result.Success -> {
                            // Filter out booked rooms
                            val bookedRoomIds = bookingsResult.data.map { it.roomid }.toSet()
                            availableRooms.clear()
                            availableRooms.addAll(roomsResult.data.filter { it.id !in bookedRoomIds })
                            roomAdapter.notifyDataSetChanged()
                        }
                        is RoomRepository.Result.Error -> {
                            // If bookings fail to load, show all rooms as available
                            availableRooms.clear()
                            availableRooms.addAll(roomsResult.data)
                            roomAdapter.notifyDataSetChanged()
                            showSnackbar("Failed to load bookings. Showing all rooms as available.")
                        }
                    }
                }
                is RoomRepository.Result.Error -> {
                    showSnackbar("Failed to load rooms. Please try again.")
                    Log.e("AllocateRooms", "Failed to load rooms: ${roomsResult.exception.message}")
                }
            }
        }
    }

    private fun allocateRoom(room: RoomEntity) {
        lifecycleScope.launch {
            val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
            val userid = prefs.getInt("userid", 0)

            // Add debug logging
            Log.d("BookingDebug", "Attempting booking with user ID: $userid")

            if (userid == 0) {
                showSnackbar("Session expired. Please log in again.")
                startActivity(Intent(this@AllocateRoomsActivity, MainActivity::class.java))
                finish()
                return@launch
            }

            // Create a new booking
            val booking = Booking(
                bookingid = 0, // Server will generate this
                roomid = room.id,
                userid = userid,
                bookingdate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date()), // Use current date
                starttime = "09:00:00", // Default start time
                endtime = null, // No end time initially
                purpose = "Allocated by admin", // Default purpose
                status = "CONFIRMED",
                attendees = 1 // Default attendees
            )

            // Send the booking request
            val result = repository.createBooking(booking)
            when (result) {
                is RoomRepository.Result.Success -> {
                    showSnackbar("Room ${room.roomnumber} allocated successfully!")
                    loadAvailableRooms() // Refresh the list
                }
                is RoomRepository.Result.Error -> {
                    showSnackbar("Failed to allocate room: ${result.exception.message}")
                }
            }
        }
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // RecyclerView Adapter
    inner class AllocateRoomAdapter(
        private val rooms: List<RoomEntity>,
        private val onAllocateClick: (RoomEntity) -> Unit
    ) : androidx.recyclerview.widget.RecyclerView.Adapter<AllocateRoomAdapter.RoomViewHolder>() {

        inner class RoomViewHolder(itemView: View) : androidx.recyclerview.widget.RecyclerView.ViewHolder(itemView) {
            val roomName: android.widget.TextView = itemView.findViewById(R.id.roomName)
            val roomCapacity: android.widget.TextView = itemView.findViewById(R.id.roomCapacity)
            val allocateButton: com.google.android.material.button.MaterialButton = itemView.findViewById(R.id.allocateButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = android.view.LayoutInflater.from(parent.context)
                .inflate(R.layout.item_allocate_room, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val room = rooms[position]
            holder.roomName.text = room.roomnumber
            holder.roomCapacity.text = "Capacity: ${room.capacity}"
            holder.allocateButton.setOnClickListener { onAllocateClick(room) }
        }

        override fun getItemCount(): Int = rooms.size
    }
}