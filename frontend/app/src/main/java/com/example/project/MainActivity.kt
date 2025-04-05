package com.example.project

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.project.databinding.ActivityMainBinding
import com.example.project.models.UsersTable
import com.example.project.network.RetrofitClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize RetrofitClient with application context
        RetrofitClient.initialize(applicationContext)

        // Clear session on every launch to ensure login page is shown
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().clear().apply()

        setupLoginButton()
        checkExistingSession()
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            email.isEmpty() -> {
                showError("Email cannot be empty")
                false
            }
            !isValidEmail(email) -> {
                showError("Invalid email format")
                false
            }
            password.isEmpty() -> {
                showError("Password cannot be empty")
                false
            }
            else -> true
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun performLogin(email: String, password: String) {
        if (RetrofitClient.apiService == null) {
            showError("RetrofitClient is not initialized. Please restart the app.")
            return
        }

        binding.progressBar.isVisible = true

        // Set credentials in RetrofitClient
        RetrofitClient.setCredentials(email, password)
        Log.d(TAG, "Credentials set for: $email")

        // Make API call to get user profile
        val profileCall = RetrofitClient.apiService.getUserProfile()
        profileCall.enqueue(object : Callback<UsersTable> {
            override fun onResponse(call: Call<UsersTable>, response: Response<UsersTable>) {
                binding.progressBar.isVisible = false
                if (response.isSuccessful) {
                    val user = response.body()
                    user?.let {
                        if (it.userid ==0) {
                            showError("Server error: No user ID returned")
                            return
                        }

                        // Save ALL user data including ID
                        saveSession(
                            userid = it.userid,
                            email = email,
                            password = password,
                            role = determineRoleFromEmail(email)
                        )
                        redirectToDashboard(determineRoleFromEmail(email))
                        binding.dataStatusTextView.text = "Logged in: ${it.username} (${it.email})"
                    } ?: run {
                        showError("User data not received from server")
                    }
                } else {
                    Log.e(TAG, "Login failed: ${response.code()} - ${response.message()}")
                    showError("Invalid credentials. Try:\nAdmin: admin@uni.com/adminpass\nManager: manager@uni.com/fmpass\nUser: user@uni.com/userpass")
                }
            }

            override fun onFailure(call: Call<UsersTable>, t: Throwable) {
                binding.progressBar.isVisible = false
                Log.e(TAG, "getUserProfile Failure: ${t.message}", t)
                showError("Login failed: ${t.message}")
            }
        })
    }

    private fun determineRoleFromEmail(email: String): Role {
        return when (email) {
            "admin@uni.com" -> Role.ADMIN
            "manager@uni.com" -> Role.FLOOR_MANAGER
            "user@uni.com" -> Role.USER
            else -> Role.USER // Default role
        }
    }

    private fun redirectToDashboard(role: Role) {
        val intent = when (role) {
            Role.ADMIN -> Intent(this, AdminActionsActivity::class.java)
            Role.FLOOR_MANAGER -> Intent(this, FloorManagerActionsActivity::class.java)
            Role.USER -> Intent(this, UserActionsActivity::class.java)
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // Clear all previous activities
        startActivity(intent)
        finish()
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    private fun saveSession(userid: Int, email: String, password: String, role: Role) {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        prefs.edit().apply {
            putInt("userid", userid)  // Store the actual user ID
            putString("EMAIL", email)
            putString("PASSWORD", password)
            putString("ROLE", role.name)
            apply()
        }
        Log.d(TAG, "Session saved for user ID: $userid")
    }

    private fun checkExistingSession() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        val email = prefs.getString("EMAIL", null)
        val roleName = prefs.getString("ROLE", null)
        if (email != null && roleName != null) {
            redirectToDashboard(Role.valueOf(roleName))
        } else {
            // If no valid session, ensure login UI is shown
            binding.dataStatusTextView.text = ""
        }
    }

    enum class Role {
        ADMIN, FLOOR_MANAGER, USER
    }
}