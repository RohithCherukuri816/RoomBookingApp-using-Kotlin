package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityAllocateRoomsBinding
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AllocateRoomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAllocateRoomsBinding
    private val availableRooms = mutableListOf<RoomEntity>()
    private lateinit var roomAdapter: AllocateRoomAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAllocateRoomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Top App Bar setup
        setupTopAppBar()

        // RecyclerView setup
        setupRecyclerView()

        // Load hardcoded rooms
        loadHardcodedRooms()
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
        roomAdapter = AllocateRoomAdapter(availableRooms, this::showAllocationDialog)
        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomsRecyclerView.adapter = roomAdapter
    }

    private fun loadHardcodedRooms() {
        availableRooms.clear()
        availableRooms.addAll(
            listOf(
                RoomEntity(1, "A - 01", "A", 0, 40, "AVAILABLE", null, null),
                RoomEntity(2, "A - 07", "A", 0, 30, "AVAILABLE", null, null),
                RoomEntity(3, "D - 01", "D", 0, 50, "AVAILABLE", null, null),
                RoomEntity(4, "C - 18", "C", 0, 66, "AVAILABLE", null, null)
            )
        )
        roomAdapter.notifyDataSetChanged()
    }

    private fun showAllocationDialog(room: RoomEntity) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_allocate_room, null)
        val textInputLayout = dialogView.findViewById<TextInputLayout>(R.id.textInputLayout)
        val editText = dialogView.findViewById<TextInputEditText>(R.id.editTextAssignee)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Allocate Room ${room.roomnumber}")
            .setView(dialogView)
            .setPositiveButton("Allocate") { _, _ ->
                val assignee = editText.text.toString().trim()
                if (assignee.isNotEmpty()) {
                    allocateRoom(room, assignee)
                } else {
                    showSnackbar("Please enter an assignee name")
                }
            }
            .setNegativeButton("Cancel", null)
            .create()

        dialog.show()
    }

    private fun allocateRoom(room: RoomEntity, assignee: String) {
        showSnackbar("Room ${room.roomnumber} allocated to $assignee")
        availableRooms.remove(room)
        roomAdapter.notifyDataSetChanged()
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
            val view = LayoutInflater.from(parent.context)
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