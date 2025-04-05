package com.example.project

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.data.AppDatabase
import com.example.project.data.Booking
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityBookRoomBinding
import com.example.project.network.RetrofitClient
import com.example.project.repository.RoomRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class BookRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBookRoomBinding
    private val availableRooms = mutableListOf<RoomEntity>()
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var selectedDate: String
    private lateinit var repository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope,this)

        // Set up the top app bar
        setupTopAppBar()

        // Initialize the date picker
        setupDatePicker()

        // Set up the RecyclerView
        setupRecyclerView()

        // Load available rooms
        loadAvailableRooms()
    }

    private fun setupTopAppBar() {
        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_logout) {
                logout()
                true
            } else {
                false
            }
        }
    }

    private fun setupDatePicker() {
        val calendar = Calendar.getInstance()
        selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
        binding.datePickerText.text = "Selected Date: $selectedDate"

        binding.datePickerContainer.setOnClickListener {
            val datePicker = DatePickerDialog(
                this,
                { _, year, month, day ->
                    calendar.set(year, month, day)
                    selectedDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                    binding.datePickerText.text = "Selected Date: $selectedDate"
                    loadAvailableRooms()
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )
            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }
    }

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(availableRooms) { room ->
            // Redirect to RoomDetailsActivity when a room is clicked
            val intent = Intent(this, RoomDetailsActivity::class.java).apply {
                putExtra("SELECTED_ROOM", room)
                putExtra("SELECTED_DATE", selectedDate)
            }
            startActivity(intent)
        }
        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomsRecyclerView.adapter = roomAdapter
    }

    private fun loadAvailableRooms() {
        lifecycleScope.launch {
            when (val result = repository.loadAvailableRooms()) {
                is RoomRepository.Result.Success -> {
                    availableRooms.clear()
                    availableRooms.addAll(result.data)
                    roomAdapter.notifyDataSetChanged()

                    if (result.data.isEmpty()) {
                        Snackbar.make(binding.root, "No rooms available", Snackbar.LENGTH_LONG).show()
                    }
                }
                is RoomRepository.Result.Error -> {
                    Snackbar.make(binding.root, "Error: ${result.exception.message}", Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun logout() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val email = prefs.getString("EMAIL", "") ?: ""
        val password = prefs.getString("PASSWORD", "") ?: ""
        RetrofitClient.setCredentials(email, password) // Reinitialize RetrofitClient
        prefs.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: android.view.Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    // RoomAdapter for RecyclerView
    inner class RoomAdapter(
        private val rooms: List<RoomEntity>,
        private val onRoomClick: (RoomEntity) -> Unit
    ) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

        inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val roomName: TextView = itemView.findViewById(R.id.roomName)
            val roomCapacity: TextView = itemView.findViewById(R.id.roomCapacity)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_booking, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val room = rooms[position]
            holder.roomName.text = room.roomnumber
            holder.roomCapacity.text = "Capacity: ${room.capacity}"
            holder.itemView.setOnClickListener { onRoomClick(room) }
        }

        override fun getItemCount(): Int = rooms.size
    }
}