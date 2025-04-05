package com.example.project

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.project.ui.theme.ProjectTheme

class ViewReportsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    ReportsDashboard()
                }
            }
        }
    }
}

@Composable
private fun ReportsDashboard() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Room Utilization Report",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            textAlign = TextAlign.Center
        )

        // Room Utilization Metrics
        RoomUtilizationMetrics()

        Spacer(modifier = Modifier.height(16.dp))

        // Filter options
        FilterChipGroup()
    }
}

@Composable
private fun RoomUtilizationMetrics() {
    // Hardcoded room data
    val totalRoomsCreated = 10 // Total rooms ever created
    val roomsAvailable = 4    // Rooms not booked today
    val roomsOccupied = 3     // Rooms booked today
    val roomsInactive = 2     // Rooms marked as "Inactive"
    val roomsDeactivated = 1  // Rooms marked as "Deactivated"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricItem(label = "Total Rooms Created", value = totalRoomsCreated.toString())
            MetricItem(label = "Rooms Available", value = roomsAvailable.toString())
            MetricItem(label = "Rooms Occupied", value = roomsOccupied.toString())
            MetricItem(label = "Rooms active", value = roomsInactive.toString())
            MetricItem(label = "Rooms inactive", value = roomsDeactivated.toString())
        }
    }
}

@Composable
private fun MetricItem(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FilterChipGroup() {
    val filters = listOf("Daily", "Weekly", "Monthly", "Custom")
    var selected by remember { mutableStateOf(filters[0]) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Filter by:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.wrapContentHeight()
        ) {
            filters.forEach { filter ->
                FilterChip(
                    selected = filter == selected,
                    onClick = { selected = filter },
                    label = { Text(filter) },
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp)),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}