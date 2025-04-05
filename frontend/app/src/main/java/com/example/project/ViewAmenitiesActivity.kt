package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.project.databinding.ActivityViewAmenitiesBinding
import com.example.project.models.RoomAmenitiesTable
import com.example.project.network.RetrofitClient
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textfield.TextInputEditText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewAmenitiesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityViewAmenitiesBinding
    private val amenityList = mutableListOf<RoomAmenitiesTable>()
    private lateinit var amenityAdapter: AmenityAdapter
    private var roomId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityViewAmenitiesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        roomId = intent.getIntExtra("roomId", -1)
        if (roomId == -1) {
            finish()
            return
        }

        binding.topAppBar.setNavigationOnClickListener { onBackPressed() }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_logout) {
                logout()
                true
            } else {
                false
            }
        }

        amenityAdapter = AmenityAdapter(amenityList) { amenity, isEnabled ->
            updateAmenityStatus(amenity, isEnabled)
        }
        binding.amenityRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.amenityRecyclerView.adapter = amenityAdapter

        loadAmenities()

        binding.fabAddAmenity.setOnClickListener {
            showAddAmenityDialog()
        }
    }

    private fun loadAmenities() {
        Log.d("ViewAmenities", "Loading amenities for room ID: $roomId")
        RetrofitClient.apiService.getRoomAmenitiesByRoomId(roomId).enqueue(object : Callback<List<RoomAmenitiesTable>> {
            override fun onResponse(call: Call<List<RoomAmenitiesTable>>, response: Response<List<RoomAmenitiesTable>>) {
                Log.d("API Response", "Code: ${response.code()}, Headers: ${response.headers()}")
                Log.d("API Response", "Body: ${response.body()}")
                if (response.isSuccessful) {
                    val amenities = response.body() ?: emptyList()
                    Log.d("ViewAmenities", "Fetched Amenities: $amenities")
                    amenityList.clear()
                    amenityList.addAll(amenities)
                    amenityAdapter.notifyDataSetChanged()
                    binding.progressBar.visibility = View.GONE
                    binding.amenityRecyclerView.visibility = View.VISIBLE
                } else {
                    Log.e("ViewAmenities", "Failed to fetch amenities: ${response.errorBody()?.string()}")
                    showError("Failed to load amenities. Please try again later.")
                }
            }

            override fun onFailure(call: Call<List<RoomAmenitiesTable>>, t: Throwable) {
                Log.e("ViewAmenities", "Failed to fetch amenities: ${t.message}")
                showError("Network error. Please check your connection.")
            }
        })
    }

    private fun showAddAmenityDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_amenity, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        val etAmenityName = dialogView.findViewById<TextInputEditText>(R.id.etAmenityName)
        val switchAmenityEnabled = dialogView.findViewById<SwitchMaterial>(R.id.switchAmenityEnabled)
        val btnCancel = dialogView.findViewById<Button>(R.id.btnCancel)
        val btnSave = dialogView.findViewById<Button>(R.id.btnSave)

        btnCancel.setOnClickListener { dialog.dismiss() }
        btnSave.setOnClickListener {
            val amenityName = etAmenityName.text.toString().trim()
            if (amenityName.isNotEmpty()) {
                val newAmenity = RoomAmenitiesTable(
                    amenityid = -1,
                    roomid = roomId,
                    amenityname = amenityName,
                    isenabled = switchAmenityEnabled.isChecked
                )
                addAmenity(newAmenity)
                dialog.dismiss()
            } else {
                etAmenityName.error = "Please enter a name"
            }
        }

        dialog.show()
    }

    private fun addAmenity(amenity: RoomAmenitiesTable) {
        RetrofitClient.apiService.createRoomAmenity(amenity).enqueue(object : Callback<RoomAmenitiesTable> {
            override fun onResponse(call: Call<RoomAmenitiesTable>, response: Response<RoomAmenitiesTable>) {
                if (response.isSuccessful) {
                    val addedAmenity = response.body()!!
                    amenityList.add(addedAmenity)
                    amenityAdapter.notifyDataSetChanged()
                    Snackbar.make(binding.root, "Amenity added successfully", Snackbar.LENGTH_SHORT).show()
                } else {
                    Log.e("ViewAmenities", "Failed to add amenity: ${response.errorBody()?.string()}")
                    Snackbar.make(binding.root, "Failed to add amenity", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<RoomAmenitiesTable>, t: Throwable) {
                Log.e("ViewAmenities", "Failed to add amenity: ${t.message}")
                Snackbar.make(binding.root, "Network error while adding amenity", Snackbar.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateAmenityStatus(amenity: RoomAmenitiesTable, isEnabled: Boolean) {
        val originalAmenity = amenity
        val updatedAmenity = RoomAmenitiesTable(
            amenityid = amenity.amenityid,
            roomid = amenity.roomid,
            amenityname = amenity.amenityname,
            isenabled = isEnabled
        )
        val index = amenityList.indexOfFirst { it.amenityid == amenity.amenityid }
        if (index != -1) {
            amenityList[index] = updatedAmenity
            // Defer notification
            binding.amenityRecyclerView.post {
                amenityAdapter.notifyItemChanged(index)
            }
        }

        val token = "Bearer ${getSharedPreferences("AppPrefs", MODE_PRIVATE).getString("authToken", "")}"
        Log.d("ViewAmenities", "Updating amenity: $updatedAmenity with token: $token")
        RetrofitClient.apiService.updateRoomAmenity(token, amenity.amenityid, updatedAmenity)
            .enqueue(object : Callback<RoomAmenitiesTable> {
                override fun onResponse(call: Call<RoomAmenitiesTable>, response: Response<RoomAmenitiesTable>) {
                    if (response.isSuccessful) {
                        val serverAmenity = response.body()!!
                        val serverIndex = amenityList.indexOfFirst { it.amenityid == serverAmenity.amenityid }
                        if (serverIndex != -1) {
                            amenityList[serverIndex] = serverAmenity
                            binding.amenityRecyclerView.post {
                                amenityAdapter.notifyItemChanged(serverIndex)
                            }
                        }
                        Log.d("ViewAmenities", "Amenity status updated successfully: $serverAmenity")
                    } else {
                        if (index != -1) {
                            amenityList[index] = originalAmenity // Revert to original
                            binding.amenityRecyclerView.post {
                                amenityAdapter.notifyItemChanged(index)
                            }
                        }
                        val errorBody = response.errorBody()?.string() ?: "No error body"
                        Log.e("ViewAmenities", "Failed to update amenity status: Code=${response.code()}, Error=$errorBody")
                        Snackbar.make(binding.root, "Failed to update amenity: ${response.code()}", Snackbar.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<RoomAmenitiesTable>, t: Throwable) {
                    if (index != -1) {
                        amenityList[index] = originalAmenity // Revert to original
                        binding.amenityRecyclerView.post {
                            amenityAdapter.notifyItemChanged(index)
                        }
                    }
                    Log.e("ViewAmenities", "Failed to update amenity status: ${t.message}", t)
                    Snackbar.make(binding.root, "Network error while updating amenity", Snackbar.LENGTH_SHORT).show()
                }
            })
    }

    private fun showError(message: String) {
        binding.progressBar.visibility = View.GONE
        binding.amenityRecyclerView.visibility = View.GONE
        binding.errorText.visibility = View.VISIBLE
        binding.errorText.text = message
    }

    private fun logout() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    inner class AmenityAdapter(
        private val amenities: List<RoomAmenitiesTable>,
        private val onToggleChange: (RoomAmenitiesTable, Boolean) -> Unit
    ) : RecyclerView.Adapter<AmenityAdapter.AmenityViewHolder>() {

        inner class AmenityViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val amenityName: TextView = itemView.findViewById(R.id.amenityName)
            val amenityToggle: SwitchMaterial = itemView.findViewById(R.id.amenityToggle)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AmenityViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_amenity, parent, false)
            return AmenityViewHolder(view)
        }

        override fun onBindViewHolder(holder: AmenityViewHolder, position: Int) {
            val amenity = amenities[position]
            holder.amenityName.text = amenity.amenityname
            // Prevent listener from firing during binding
            holder.amenityToggle.setOnCheckedChangeListener(null)
            holder.amenityToggle.isChecked = amenity.isenabled
            holder.amenityToggle.setOnCheckedChangeListener { _, isChecked ->
                onToggleChange(amenity, isChecked)
            }
        }

        override fun getItemCount(): Int = amenities.size
    }
}