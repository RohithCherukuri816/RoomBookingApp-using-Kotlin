package com.example.project

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.data.AppDatabase
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityManageRoomsBinding
import com.example.project.network.RetrofitClient
import com.example.project.network.WebSocketClient
import com.example.project.repository.RoomRepository
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class ManageRoomsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityManageRoomsBinding
    private val roomList = mutableListOf<RoomEntity>()
    private val filteredRoomList = mutableListOf<RoomEntity>()
    private lateinit var roomAdapter: RoomAdapter
    private lateinit var repository: RoomRepository
    private lateinit var webSocketClient: WebSocketClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManageRoomsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope, this)

        // Get credentials from SharedPreferences
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val email = prefs.getString("EMAIL", "") ?: ""
        val password = prefs.getString("PASSWORD", "") ?: ""

        Log.d("ManageRoomsActivity", "Using credentials: email='$email', password='$password'")
        RetrofitClient.setCredentials(email, password)
        RetrofitClient.initialize(this)

        webSocketClient = WebSocketClient(
            onMessageReceived = { message ->
                Log.d("ManageRoomsActivity", "Received WebSocket message: $message")
                showNotification(message)
            },
            username = email,
            password = password
        )
        webSocketClient.connect()

        setupTopAppBar()
        setupRecyclerView()
        setupFilterSpinner()
        loadRooms()

        binding.addRoomFab.setOnClickListener {
            val intent = Intent(this, AddRoomActivity::class.java)
            intent.putExtra("MODE", "ADD")
            startActivity(intent)
        }

        setSupportActionBar(binding.topAppBar)
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
        roomAdapter = RoomAdapter(filteredRoomList) { room ->
            val intent = Intent(this, AddRoomActivity::class.java).apply {
                putExtra("MODE", "UPDATE")
                putExtra("ROOM_ID", room.id)
                putExtra("ROOM_NUMBER", room.roomnumber)
                putExtra("BLOCK", room.block)
                putExtra("FLOOR", room.floor)
                putExtra("CAPACITY", room.capacity)
                putExtra("STATUS", room.status)
                putExtra("CATEGORY", room.category)
                putExtra("SUB_CATEGORY", room.subCategory)
            }
            startActivity(intent)
        }
        binding.roomRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.roomRecyclerView.adapter = roomAdapter
    }

    private fun setupFilterSpinner() {
        val categories = listOf("All", "Classroom", "Lab", "Activity Room")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.filterCategorySpinner.adapter = adapter

        binding.filterCategorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = categories[position]
                filterRooms(selectedCategory)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun filterRooms(category: String) {
        filteredRoomList.clear()
        if (category == "All") {
            filteredRoomList.addAll(roomList)
        } else {
            filteredRoomList.addAll(roomList.filter { it.category == category })
        }
        roomAdapter.notifyDataSetChanged()
    }

    private fun loadRooms() {
        lifecycleScope.launch {
            val result = repository.syncAdminRooms()
            when (result) {
                is RoomRepository.Result.Success -> {
                    roomList.clear()
                    roomList.addAll(result.data)
                    filterRooms(binding.filterCategorySpinner.selectedItem.toString())
                }
                is RoomRepository.Result.Error -> {
                    Log.e("ManageRoomsActivity", "Failed to load rooms: ${result.exception.message}")
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

    private fun showNotification(message: String) {
        val channelId = "room_updates"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Room Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { setShowBadge(true) }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        val intent = Intent(this, AdminActionsActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Room Update")
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        webSocketClient.disconnect()
    }

    inner class RoomAdapter(
        private val rooms: MutableList<RoomEntity>,
        private val onUpdateClick: (RoomEntity) -> Unit
    ) : RecyclerView.Adapter<RoomAdapter.RoomViewHolder>() {

        inner class RoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val roomName: TextView = itemView.findViewById(R.id.roomName)
            val roomCapacity: TextView = itemView.findViewById(R.id.roomCapacity)
            val updateButton: com.google.android.material.button.MaterialButton =
                itemView.findViewById(R.id.updateButton)
            val deleteButton: com.google.android.material.button.MaterialButton =
                itemView.findViewById(R.id.deleteButton)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RoomViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_room, parent, false)
            return RoomViewHolder(view)
        }

        override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
            val room = rooms[position]
            holder.roomName.text = room.roomnumber
            holder.roomCapacity.text = "Capacity: ${room.capacity}"

            holder.updateButton.setOnClickListener {
                onUpdateClick(room)
            }

            holder.deleteButton.setOnClickListener {
                MaterialAlertDialogBuilder(this@ManageRoomsActivity)
                    .setTitle("Delete Room")
                    .setMessage("Are you sure you want to delete ${room.roomnumber}?")
                    .setPositiveButton("Delete") { _, _ ->
                        lifecycleScope.launch {
                            val result = repository.deleteRoom(room)
                            when (result) {
                                is RoomRepository.Result.Success -> {
                                    rooms.removeAt(position)
                                    notifyItemRemoved(position)
                                    showSnackbar("Room deleted!")
                                }
                                is RoomRepository.Result.Error -> {
                                    Log.e("ManageRoomsActivity", "Failed to delete room: ${result.exception.message}")
                                    showSnackbar("Failed to delete room: ${result.exception.message}")
                                }
                            }
                        }
                    }
                    .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                    .show()
            }
        }

        override fun getItemCount(): Int = rooms.size
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}