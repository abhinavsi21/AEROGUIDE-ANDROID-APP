package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import com.example.data.model.SimulationStage
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimulationSheet(
    currentStage: SimulationStage,
    onSelectStage: (SimulationStage) -> Unit,
    onNextStage: () -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = AeroNavyDark,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.White.copy(alpha = 0.4f))
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(AeroSkyBlue)
                        )
                        Text(
                            text = "AODB / FIDS SIMULATOR",
                            color = AeroSkyBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.5.sp
                        )
                    }
                    Text(
                        text = "Flight Operational Scenarios",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                TextButton(
                    onClick = onReset,
                    colors = ButtonDefaults.textButtonColors(contentColor = AeroWarning)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset Demo", fontWeight = FontWeight.SemiBold)
                }
            }

            Text(
                text = "Simulate real-time airport operational events to verify how AeroGuide's time intelligence, wayfinding, and alert engine adapt automatically.",
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 13.sp,
                lineHeight = 18.sp
            )

            // Stage cards
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SimulationStage.values().forEach { stage ->
                    val isSelected = stage == currentStage
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) Color(0xFF1E3A5F) else Color(0xFF0F1E33))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) AeroSkyBlue else Color(0xFF1B3150),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectStage(stage) }
                            .padding(horizontal = 14.dp, vertical = 12.dp)
                            .testTag("stage_${stage.name.lowercase()}"),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) AeroSkyBlue else Color(0xFF172B47)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = (stage.ordinal + 1).toString(),
                                    color = if (isSelected) AeroNavyDark else Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Column {
                                Text(
                                    text = stage.stageName.substringAfter(". "),
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Gate ${stage.gate} · ${stage.distanceMeters}m (${stage.walkingMinutes}m) · ${stage.timeLabel}",
                                    color = Color.White.copy(alpha = 0.6f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(AeroSkyBlue)
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "ACTIVE",
                                    color = AeroNavyDark,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Quick Advance Button
            Button(
                onClick = onNextStage,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("next_stage_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AeroSkyBlue,
                    contentColor = AeroNavyDark
                )
            ) {
                Icon(imageVector = Icons.Default.FastForward, contentDescription = "Next")
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Trigger Next Stage (${(currentStage.ordinal + 1) % SimulationStage.values().size + 1}/${SimulationStage.values().size})",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
