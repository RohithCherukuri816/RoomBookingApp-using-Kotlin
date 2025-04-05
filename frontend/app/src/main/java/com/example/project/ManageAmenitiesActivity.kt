package com.example.project

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
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityManageAmenitiesBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ManageAmenitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageAmenitiesBinding
    private val roomList = mutableListOf<RoomEntity>()
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var repository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageAmenitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope,this)

        // Set up the top app bar
        setupTopAppBar()

        // Set up RecyclerView
        setupRecyclerView()

        // Load rooms from the backend
        loadRooms()
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

    private fun setupRecyclerView() {
        roomAdapter = RoomAdapter(roomList) { room ->
            // Navigate to ViewAmenitiesActivity
            val intent = Intent(this, ViewAmenitiesActivity::class.java)
            intent.putExtra("roomId", room.id)
            startActivity(intent)
        }
        binding.roomRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomRecyclerView.adapter = roomAdapter
    }

    private fun loadRooms() {
        lifecycleScope.launch {
            val result = repository.loadAvailableRooms() // Use the correct method
            when (result) {
                is RoomRepository.Result.Success -> {
                    roomList.clear()
                    roomList.addAll(result.data)
                    roomAdapter.notifyDataSetChanged()
                }
                is RoomRepository.Result.Error -> {
                    Log.e("ManageAmenities", "Failed to load rooms: ${result.exception.message}")
                    showSnackbar("Failed to load rooms: ${result.exception.message}")
                }
            }
        }
    }

    private fun logout() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    // Room Adapter
    inner class RoomAdapter(
        private val rooms: List<RoomEntity>,
        private val onViewClick: (RoomEntity) -> Unit
    ) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

        inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val roomName: TextView = itemView.findViewById(R.id.roomName)
            val viewButton: MaterialButton = itemView.findViewById(R.id.viewButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room_amenity, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val room = rooms[position]
            holder.roomName.text = room.roomnumber
            holder.viewButton.setOnClickListener { onViewClick(room) }
        }

        override fun getItemCount(): Int = rooms.size
    }
}