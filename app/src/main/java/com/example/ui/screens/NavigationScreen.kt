package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AirportFacility
import com.example.data.model.NavigationStep
import com.example.data.model.PassengerLocation
import com.example.ui.components.AirportCanvasMap
import com.example.ui.theme.*

@Composable
fun NavigationScreen(
    currentGate: String,
    passengerLocation: PassengerLocation,
    facilities: List<AirportFacility>,
    steps: List<NavigationStep>,
    currentStepIndex: Int,
    isNavigating: Boolean,
    onStartNavigation: () -> Unit,
    onStopNavigation: () -> Unit,
    onNextStep: () -> Unit,
    onPrevStep: () -> Unit,
    onOpenBoardingPass: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedViewMode by remember { mutableStateOf(0) } // 0: Turn-by-Turn, 1: Terminal Map
    var isSimulatingMovement by remember { mutableStateOf(false) }

    // Simulated passenger movement progress along path (0f to 1f)
    val movementProgress = remember { Animatable(0f) }

    LaunchedEffect(isNavigating, isSimulatingMovement) {
        if (isNavigating && isSimulatingMovement) {
            movementProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 18000, easing = LinearEasing)
            )
        } else if (!isNavigating) {
            movementProgress.snapTo(0f)
        }
    }

    // Remaining dynamic distance and walking time based on simulation progress
    val baseDistance = if (currentGate == "31") 850 else 650
    val baseMinutes = if (currentGate == "31") 11 else 8
    val dynamicDistance = remember(movementProgress.value, currentGate) {
        (baseDistance * (1f - movementProgress.value)).toInt().coerceAtLeast(0)
    }
    val dynamicMinutes = remember(movementProgress.value, currentGate) {
        (baseMinutes * (1f - movementProgress.value) + 0.5f).toInt().coerceAtLeast(0)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Segmented Control (Turn-by-Turn vs Terminal Map)
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(modifier = Modifier.padding(3.dp)) {
                    // Turn-by-Turn Mode
                    Surface(
                        onClick = { selectedViewMode = 0 },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedViewMode == 0) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_mode_steps")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DirectionsWalk,
                                contentDescription = null,
                                tint = if (selectedViewMode == 0) AeroBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Turn-by-Turn",
                                color = if (selectedViewMode == 0) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedViewMode == 0) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Terminal Map Mode
                    Surface(
                        onClick = { selectedViewMode = 1 },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedViewMode == 1) MaterialTheme.colorScheme.surface else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("nav_mode_map")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Map,
                                contentDescription = null,
                                tint = if (selectedViewMode == 1) AeroBlue else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Terminal Map",
                                color = if (selectedViewMode == 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (selectedViewMode == 1) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Destination Summary Card (Matches Reference 3)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("destination_summary_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AeroSkyBlue))
                                Text(
                                    text = "YOUR DESTINATION",
                                    color = AeroSkyBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Gate $currentGate",
                                color = if (currentGate == "31") AeroWarning else MaterialTheme.colorScheme.onSurface,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = (-0.5).sp
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Terminal T1",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Text("•", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                Text(
                                    text = "From ${passengerLocation.currentZone}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Right: Distance & Flight Pill
                        Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = AeroBlue, modifier = Modifier.size(18.dp))
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$dynamicDistance m",
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "$dynamicMinutes min walk",
                                        color = AeroSkyBlue,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            // Flight AI 1234 Pill (Clicking opens Boarding Pass)
                            Surface(
                                onClick = onOpenBoardingPass,
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.testTag("nav_boarding_pass_pill")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier.size(16.dp).clip(CircleShape).background(Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_air_india_logo),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    Text("AI 1234 · Pass", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Interactive Programmatically Rendered Airport Terminal Map
        item {
            AirportCanvasMap(
                facilities = facilities,
                passengerLocation = passengerLocation,
                targetGate = currentGate,
                isNavigating = isNavigating,
                navProgress = movementProgress.value,
                onNavigateClick = onStartNavigation
            )
        }

        // 4. Navigation Status Banner with Simulation Controls (Matches Reference 3)
        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = AeroNavyDark,
                border = androidx.compose.foundation.BorderStroke(1.dp, AeroBlue.copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(AeroBlue.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.AutoMirrored.Filled.DirectionsWalk, contentDescription = null, tint = AeroSkyBlue, modifier = Modifier.size(18.dp))
                        }
                        Column {
                            Text(
                                text = "Follow the blue route to Gate $currentGate",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isNavigating) "Simulating indoor walking progress..." else "Route updates automatically if gate changes.",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 10.sp
                            )
                        }
                    }

                    if (isNavigating) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            // Pause / Resume simulation
                            IconButton(
                                onClick = { isSimulatingMovement = !isSimulatingMovement },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSimulatingMovement) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Pause/Resume",
                                    tint = AeroSkyBlue
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    isSimulatingMovement = false
                                    onStopNavigation()
                                },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                            ) {
                                Text("✕ Exit", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 5. Route Overview Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Default.Route, contentDescription = null, tint = AeroBlue, modifier = Modifier.size(18.dp))
                    Text(
                        text = "Route Overview",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = "$dynamicDistance m • $dynamicMinutes min",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // 6. Turn-by-Turn Steps List (Matches Reference 3)
        itemsIndexed(steps) { index, step ->
            val isCurrent = index == currentStepIndex
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isCurrent && isNavigating) AeroBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Step Number Circle
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(if (isCurrent && isNavigating) AeroBlue else MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${index + 1}",
                            color = if (isCurrent && isNavigating) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = step.instruction,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${step.distanceMeters} m",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = step.detail,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 7. Sticky Bottom Primary CTA: Start Navigation
        item {
            Button(
                onClick = {
                    if (isNavigating) {
                        isSimulatingMovement = !isSimulatingMovement
                    } else {
                        onStartNavigation()
                        isSimulatingMovement = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("nav_start_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AeroBlue,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (isNavigating && isSimulatingMovement) Icons.Default.Pause else Icons.Default.NearMe,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isNavigating) {
                            if (isSimulatingMovement) "Pause Simulated Navigation" else "Resume Simulated Navigation"
                        } else {
                            "Start Navigation to Gate $currentGate"
                        },
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
