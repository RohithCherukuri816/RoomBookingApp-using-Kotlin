package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.project.databinding.ActivityAssignRolesBinding
import com.example.project.models.CreateUserRequest
import com.example.project.models.UsersTable
import com.example.project.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AssignRolesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAssignRolesBinding
    private var selectedRoleId: Int = 1 // Default to Admin
    private val TAG = "AssignRolesActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAssignRolesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RetrofitClient
        RetrofitClient.initialize(applicationContext)
        Log.d(TAG, "RetrofitClient initialized")

        // Set up the top app bar
        setSupportActionBar(binding.topAppBar)
        binding.topAppBar.setNavigationOnClickListener {
            finish() // Go back to AdminActionsActivity
        }
        binding.topAppBar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_logout -> {
                    logout()
                    true
                }
                else -> false
            }
        }

        // Set up spinner
        val roles = arrayOf("Admin", "Floor Manager", "User")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerRole.adapter = adapter
        binding.spinnerRole.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedRoleId = when (position) {
                    0 -> 1 // Admin
                    1 -> 2 // Floor Manager
                    2 -> 3 // User
                    else -> 1
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                selectedRoleId = 1 // Default to Admin
            }
        }

        // Set up submit button
        binding.btnSubmit.setOnClickListener {
            createUser()
        }
    }

    private fun createUser() {
        val username = binding.etUsername.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        // Log input data
        Log.d(TAG, "Creating user with: username=$username, email=$email, password=$password, roleId=$selectedRoleId")

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            Log.w(TAG, "Validation failed: One or more fields are empty")
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val userRequest = CreateUserRequest(
            username = username,
            email = email,
            passwordHash = password, // In production, hash this
            roleId = selectedRoleId
        )

        // Log the request object
        Log.i(TAG, "Request body: $userRequest")

        // Use RetrofitClient's apiService
        val apiService = RetrofitClient.apiService
        val call = apiService.createUser(userRequest)

        // Log the API call initiation
        Log.d(TAG, "Initiating API call to create user")

        call.enqueue(object : Callback<UsersTable> {
            override fun onResponse(call: Call<UsersTable>, response: Response<UsersTable>) {
                if (response.isSuccessful) {
                    val createdUser = response.body()
                    Log.i(TAG, "User created successfully: $createdUser")
                    Toast.makeText(this@AssignRolesActivity, "User created successfully", Toast.LENGTH_SHORT).show()
                    finish()
                } else {
                    Log.e(TAG, "Failed to create user. Response code: ${response.code()}, Message: ${response.message()}")
                    Log.e(TAG, "Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(this@AssignRolesActivity, "Failed to create user: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<UsersTable>, t: Throwable) {
                Log.e(TAG, "API call failed: ${t.message}", t)
                Toast.makeText(this@AssignRolesActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun logout() {
        RetrofitClient.clearCredentials() // Clear auth token
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}