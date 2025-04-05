package com.example.project

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.appcompat.app.AppCompatActivity
import com.example.project.data.AppDatabase
import com.example.project.data.RoomEntity
import com.example.project.databinding.ActivityAddRoomBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AddRoomActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddRoomBinding
    private lateinit var repository: RoomRepository
    private var selectedCategory: String = ""
    private var selectedSubCategory: String? = null
    private var roomId: Int = 0
    private var mode: String = "ADD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddRoomBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, CoroutineScope(Dispatchers.Main), this)

        // Set up the spinner first to ensure adapter is initialized
        setupCategorySpinner()

        // Now check mode and pre-fill if updating
        mode = intent.getStringExtra("MODE") ?: "ADD"
        if (mode == "UPDATE") {
            roomId = intent.getIntExtra("ROOM_ID", 0)
            preFillForm()
        }

        setSupportActionBar(binding.topAppBar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.topAppBar.setNavigationOnClickListener {
            startActivity(Intent(this, ManageRoomsActivity::class.java))
            finish()
        }

        binding.saveButton.text = if (mode == "ADD") "Add Room" else "Update Room"
        binding.saveButton.setOnClickListener { saveRoom() }
    }

    private fun preFillForm() {
        binding.roomNumberEditText.setText(intent.getStringExtra("ROOM_NUMBER"))
        binding.blockEditText.setText(intent.getStringExtra("BLOCK"))
        binding.floorEditText.setText(intent.getIntExtra("FLOOR", 0).toString())
        binding.capacityEditText.setText(intent.getIntExtra("CAPACITY", 0).toString())
        binding.statusEditText.setText(intent.getStringExtra("STATUS"))

        val category = intent.getStringExtra("CATEGORY") ?: ""
        selectedCategory = category
        val subCategory = intent.getStringExtra("SUB_CATEGORY")
        selectedSubCategory = subCategory

        val categories = listOf("Classroom", "Lab", "Activity Room")
        val adapter = binding.categorySpinner.adapter as ArrayAdapter<String> // Safe now because adapter is set
        val categoryIndex = categories.indexOf(category)
        if (categoryIndex >= 0) {
            binding.categorySpinner.setSelection(categoryIndex)
        }

        if (category == "Activity Room" || category == "Lab") {
            binding.subCategoryRadioGroup.visibility = View.VISIBLE
            updateSubCategoryOptions(category)
            when (subCategory) {
                "GTIDS Board Room" -> binding.radioGtidsBoardRoom.isChecked = true
                "Old Board Room" -> binding.radioOldBoardRoom.isChecked = true
                "B-05" -> binding.radioB05.isChecked = true
                "Gallery Hall 2" -> binding.radioGalleryHall2.isChecked = true
                "Gallery Hall 1" -> binding.radioGalleryHall1.isChecked = true
                "ST Lab" -> binding.radioStLab.isChecked = true
                "CT Lab" -> binding.radioCtLab.isChecked = true
                "GTM-1" -> binding.radioGtm1.isChecked = true
                "GTM-2" -> binding.radioGtm2.isChecked = true
                "Networking Lab" -> binding.radioNetworkingLab.isChecked = true
            }
        }
    }

    private fun setupCategorySpinner() {
        val categories = listOf("Classroom", "Lab", "Activity Room")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.categorySpinner.adapter = adapter

        binding.categorySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCategory = categories[position]
                if (selectedCategory == "Activity Room" || selectedCategory == "Lab") {
                    binding.subCategoryRadioGroup.visibility = View.VISIBLE
                    updateSubCategoryOptions(selectedCategory)
                } else {
                    binding.subCategoryRadioGroup.visibility = View.GONE
                    selectedSubCategory = null
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.subCategoryRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            val radioButton: RadioButton = findViewById(checkedId)
            selectedSubCategory = radioButton.text.toString()
        }
    }

    private fun updateSubCategoryOptions(category: String) {
        binding.radioGtidsBoardRoom.visibility = View.GONE
        binding.radioOldBoardRoom.visibility = View.GONE
        binding.radioB05.visibility = View.GONE
        binding.radioGalleryHall2.visibility = View.GONE
        binding.radioGalleryHall1.visibility = View.GONE
        binding.radioStLab.visibility = View.GONE
        binding.radioCtLab.visibility = View.GONE
        binding.radioGtm1.visibility = View.GONE
        binding.radioGtm2.visibility = View.GONE
        binding.radioNetworkingLab.visibility = View.GONE

        when (category) {
            "Activity Room" -> {
                binding.radioGtidsBoardRoom.visibility = View.VISIBLE
                binding.radioOldBoardRoom.visibility = View.VISIBLE
                binding.radioB05.visibility = View.VISIBLE
                binding.radioGalleryHall2.visibility = View.VISIBLE
                binding.radioGalleryHall1.visibility = View.VISIBLE
            }
            "Lab" -> {
                binding.radioStLab.visibility = View.VISIBLE
                binding.radioCtLab.visibility = View.VISIBLE
                binding.radioGtm1.visibility = View.VISIBLE
                binding.radioGtm2.visibility = View.VISIBLE
                binding.radioNetworkingLab.visibility = View.VISIBLE
            }
        }
    }

    private fun saveRoom() {
        val roomnumber = binding.roomNumberEditText.text.toString().trim()
        val block = binding.blockEditText.text.toString().trim()
        val floor = binding.floorEditText.text.toString().trim()
        val capacity = binding.capacityEditText.text.toString().trim()
        val status = binding.statusEditText.text.toString().trim()

        if (roomnumber.isEmpty() || block.isEmpty() || floor.isEmpty() || capacity.isEmpty() || status.isEmpty()) {
            Snackbar.make(binding.root, "Please fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        }

        val room = RoomEntity(
            id = if (mode == "UPDATE") roomId else 0,
            roomnumber = roomnumber,
            block = block,
            floor = floor.toInt(),
            capacity = capacity.toInt(),
            status = status,
            category = selectedCategory,
            subCategory = if (selectedCategory == "Activity Room" || selectedCategory == "Lab") selectedSubCategory else null
        )

        CoroutineScope(Dispatchers.Main).launch {
            val result = if (mode == "ADD") repository.addRoom(room) else repository.updateRoom(room)
            when (result) {
                is RoomRepository.Result.Success -> {
                    val message = if (mode == "ADD") "Room added successfully!" else "Room updated successfully!"
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    startActivity(Intent(this@AddRoomActivity, ManageRoomsActivity::class.java))
                    finish()
                }
                is RoomRepository.Result.Error -> {
                    Snackbar.make(binding.root, "Failed to save room: ${result.exception.message}", Snackbar.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.top_app_bar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}