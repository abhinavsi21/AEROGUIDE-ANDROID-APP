package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Flight
import com.example.data.model.SimulationStage
import com.example.ui.theme.*

data class PassengerJourneyMilestone(
    val stage: SimulationStage,
    val title: String,
    val location: String,
    val scheduledTime: String,
    val details: String,
    val isCompleted: Boolean,
    val isCurrent: Boolean
)

@Composable
fun JourneyTimelineScreen(
    flight: Flight,
    currentStage: SimulationStage,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val milestones = listOf(
        PassengerJourneyMilestone(
            stage = SimulationStage.SCHEDULED,
            title = "Booking Confirmed",
            location = "Air India Direct · AG7X92",
            scheduledTime = "10:30",
            details = "E-Ticket issued for Seat ${flight.seat}, Economy Extra",
            isCompleted = currentStage.ordinal >= SimulationStage.SCHEDULED.ordinal,
            isCurrent = currentStage == SimulationStage.SCHEDULED
        ),
        PassengerJourneyMilestone(
            stage = SimulationStage.CHECK_IN_OPEN,
            title = "Terminal Check-in & Bag Drop",
            location = "Terminal T1 · Counter 14",
            scheduledTime = "13:00",
            details = "Boarding pass issued for Group B, 1 checked bag (23kg)",
            isCompleted = currentStage.ordinal >= SimulationStage.CHECK_IN_OPEN.ordinal,
            isCurrent = currentStage == SimulationStage.CHECK_IN_OPEN
        ),
        PassengerJourneyMilestone(
            stage = SimulationStage.CHECK_IN_OPEN,
            title = "Security Screening Cleared",
            location = "Central Security Checkpoint A",
            scheduledTime = "14:05",
            details = "Fast-track biometric security line cleared in 6 min",
            isCompleted = currentStage.ordinal >= SimulationStage.BOARDING.ordinal,
            isCurrent = false
        ),
        PassengerJourneyMilestone(
            stage = SimulationStage.BOARDING,
            title = "Proceed to Departure Gate",
            location = "Concourse North · Gate ${flight.gate}",
            scheduledTime = "14:25",
            details = if (flight.gate == "31") "Gate relocated to Gate 31 (850m · 11min walk)" else "Gate 24 (650m · 8min walk from Food Court)",
            isCompleted = currentStage.ordinal >= SimulationStage.BOARDING.ordinal,
            isCurrent = currentStage == SimulationStage.BOARDING || currentStage == SimulationStage.GATE_CHANGE
        ),
        PassengerJourneyMilestone(
            stage = SimulationStage.FINAL_CALL,
            title = "Passenger Boarding",
            location = "Gate ${flight.gate} Podium",
            scheduledTime = "14:40",
            details = "Boarding pass scan & jet bridge embarkation",
            isCompleted = currentStage.ordinal >= SimulationStage.FINAL_CALL.ordinal,
            isCurrent = currentStage == SimulationStage.FINAL_CALL
        ),
        PassengerJourneyMilestone(
            stage = SimulationStage.DEPARTED,
            title = "Departed on Schedule",
            location = "Runway 27R · Airborne to GOI",
            scheduledTime = "15:10",
            details = "Flight AI 1234 airborne. Estimated arrival Goa at 16:25",
            isCompleted = currentStage == SimulationStage.DEPARTED,
            isCurrent = currentStage == SimulationStage.DEPARTED
        )
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .testTag("journey_back_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = AeroNavyDark)
                }

                Column {
                    Text(
                        text = "My Passenger Journey",
                        color = AeroNavyDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${flight.flightNumber} · Mumbai to Goa",
                        color = AeroTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = AeroNavyDark)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("ACTIVE MILESTONE", color = AeroSkyBlue, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(currentStage.stageName.substringAfter(". "), color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        Text(currentStage.description, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AeroBlue)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(currentStage.timeLabel, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        itemsIndexed(milestones) { index, m ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Milestone vertical line & circle
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    m.isCurrent -> AeroBlue
                                    m.isCompleted -> AeroSuccess
                                    else -> Color(0xFFE2E8F0)
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (m.isCompleted && !m.isCurrent) {
                            Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = (index + 1).toString(),
                                color = if (m.isCurrent) Color.White else AeroTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    if (index < milestones.size - 1) {
                        Box(
                            modifier = Modifier
                                .width(2.dp)
                                .height(56.dp)
                                .background(if (m.isCompleted) AeroSuccess.copy(alpha = 0.5f) else Color(0xFFE2E8F0))
                        )
                    }
                }

                // Milestone Card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = if (m.isCurrent) Color(0xFFEBF4FF) else Color.White),
                    border = CardDefaults.outlinedCardBorder().copy(
                        brush = androidx.compose.ui.graphics.SolidColor(
                            if (m.isCurrent) AeroBlue else AeroBorderLight
                        )
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = m.title,
                                color = AeroNavyDark,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = m.scheduledTime,
                                color = AeroTextSecondary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Text(
                            text = m.location,
                            color = AeroBlue,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        Text(
                            text = m.details,
                            color = AeroTextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }
    }
}
