package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.components.AirportCanvasMap
import com.example.ui.theme.*

@Composable
fun AirportMapScreen(
    facilities: List<AirportFacility>,
    passengerLocation: PassengerLocation,
    currentGate: String,
    onNavigateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredFacilities = remember(selectedFilter, facilities) {
        when (selectedFilter) {
            "GATES" -> facilities.filter { it.type == FacilityType.GATE }
            "FOOD" -> facilities.filter { it.type == FacilityType.FOOD }
            "RESTROOMS" -> facilities.filter { it.type == FacilityType.RESTROOM }
            "SECURITY" -> facilities.filter { it.type == FacilityType.SECURITY }
            else -> facilities
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Airport Wayfinding Map",
                    color = AeroNavyDark,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Terminal T1 · Concourse Level 2 (Simulated)",
                    color = AeroTextSecondary,
                    fontSize = 12.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE0EDFD))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Level 2",
                    color = AeroBlue,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Facility Filter Chips
        val filters = listOf("ALL", "GATES", "FOOD", "SECURITY", "RESTROOMS")
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filters) { filter ->
                val isSelected = selectedFilter == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(if (isSelected) AeroNavyDark else Color.White)
                        .border(1.dp, if (isSelected) AeroNavyDark else AeroBorderLight, RoundedCornerShape(20.dp))
                        .clickable { selectedFilter = filter }
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter,
                        color = if (isSelected) Color.White else AeroTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        // Full Interactive Canvas Map
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .testTag("airport_canvas_map")
        ) {
            AirportCanvasMap(
                facilities = filteredFacilities,
                passengerLocation = passengerLocation,
                targetGate = currentGate,
                onNavigateClick = onNavigateClick,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Bottom Map Info & Legend
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White)
                .border(1.dp, AeroBorderLight, RoundedCornerShape(12.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            LegendItem(color = AeroSkyBlue, label = "You (Food Court)")
            LegendItem(color = if (currentGate == "31") AeroWarning else AeroBlue, label = "Gate $currentGate")
            LegendItem(color = Color(0xFF7C3AED), label = "Security")
            LegendItem(color = Color(0xFFA855F7), label = "Duty Free")
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            color = AeroTextSecondary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
