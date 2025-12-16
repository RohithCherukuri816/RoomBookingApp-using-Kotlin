package com.example.project

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.project.data.AppDatabase
import com.example.project.databinding.ActivityOccupancyDashboardBinding
import com.example.project.repository.RoomRepository
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class OccupancyDashboardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOccupancyDashboardBinding
    private lateinit var repository: RoomRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOccupancyDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize database and repository
        val db = AppDatabase.getDatabase(this)
        repository = RoomRepository(db, lifecycleScope, this)

        // Set up the top app bar
        setupTopAppBar()

        // Enable JavaScript for WebView
        binding.dashboardWebView.settings.javaScriptEnabled = true

        // Load data (hardcoded + database)
        loadRequestData()
        loadDashboardWebView()
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

    private fun loadRequestData() {
        lifecycleScope.launch {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val today = sdf.format(Calendar.getInstance().time)

            val bookingsResult = repository.getBookingsByDate(today)
            when (bookingsResult) {
                is RoomRepository.Result.Success -> {
                    val bookings = bookingsResult.data
                    val receivedCount = bookings.size
                    val acceptedCount = bookings.count { it.status == "Confirmed" }
                    val rejectedCount = bookings.count { it.status == "Rejected" }
                    updateDashboardUI(receivedCount, acceptedCount, rejectedCount)
                }
                is RoomRepository.Result.Error -> {
                    //Log.e("OccupancyDashboard", "Failed to load bookings: ${bookingsResult.exception.message}")
                    //showSnackbar("Failed to load request data: ${bookingsResult.exception.message}")
                    // Fallback to hardcoded data if database fails
                    updateDashboardUI(3, 0, 1)
                }
            }
        }
    }

    private fun updateDashboardUI(receivedCount: Int, acceptedCount: Int, rejectedCount: Int) {
        binding.requestsReceivedCount.text = "Requests Received: $receivedCount"
        binding.requestsAcceptedCount.text = "Requests Accepted: $acceptedCount"
        binding.requestsRejectedCount.text = "Requests Rejected: $rejectedCount"
    }

    private fun loadDashboardWebView() {
        // Hardcoded data for the WebView chart
        val received = 3
        val accepted = 0
        val rejected = 0

        val htmlContent = """
            <!DOCTYPE html>
            <html>
            <head>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { margin: 0; padding: 10px; background: #f5f5f5; }
                    canvas { max-width: 100%; height: auto; }
                </style>
            </head>
            <body>
                <canvas id="requestsChart"></canvas>
                <script>
                    const ctx = document.getElementById('requestsChart').getContext('2d');
                    new Chart(ctx, {
                        type: 'bar',
                        data: {
                            labels: ['Received', 'Accepted', 'Rejected'],
                            datasets: [{
                                label: 'Request Statistics',
                                data: [$received, $accepted, $rejected],
                                backgroundColor: ['#26A69A', '#4CAF50', '#EF5350'],
                                borderColor: ['#26A69A', '#4CAF50', '#EF5350'],
                                borderWidth: 1
                            }]
                        },
                        options: {
                            scales: {
                                y: { beginAtZero: true }
                            },
                            plugins: {
                                legend: { display: false }
                            }
                        }
                    });
                </script>
            </body>
            </html>
        """.trimIndent()

        binding.dashboardWebView.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }
}