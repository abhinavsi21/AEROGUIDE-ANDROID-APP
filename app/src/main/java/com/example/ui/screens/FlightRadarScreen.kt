package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.RadarAircraft
import com.example.data.repository.AeroGuideRepository
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightRadarScreen(
    repository: AeroGuideRepository,
    onBack: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedAircraft by repository.selectedRadarAircraft.collectAsState()
    var selectedFilter by remember { mutableStateOf("ALL") }
    var searchQuery by remember { mutableStateOf("") }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }

    val filteredAircraft = remember(selectedFilter, searchQuery) {
        repository.getFilteredRadarAircraft(selectedFilter, searchQuery)
    }

    // Smooth Rotating Radar Sweep Animation
    val infiniteTransition = rememberInfiniteTransition(label = "radarSweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweepAngle"
    )

    val textMeasurer = rememberTextMeasurer()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 10.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Flight Radar Header (Matches Reference 2)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AeroBlue.copy(alpha = 0.15f))
                            .border(1.dp, AeroSkyBlue.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = "Radar",
                            tint = AeroSkyBlue,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Flight Radar",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            // SIMULATED DATA pill
                            Surface(
                                color = AeroSuccess.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(8.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AeroSuccess.copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AutoAwesome,
                                        contentDescription = null,
                                        tint = AeroSuccess,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "SIMULATED DATA",
                                        color = AeroSuccess,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                        Text(
                            text = "Live air traffic around GOX Mopa • 16 aircraft tracked",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                // Airport Selection Dropdown Pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text("GOX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(16.dp))
                        }
                        Text("Manohar Int'l", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // 2. Search Input Field
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search flight, airline, or route (e.g. AI 1234, BOM, GOX)...",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("radar_search_input"),
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AeroBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        // 3. Airline Filter Chips Row (Matches Reference 2)
        item {
            val filters = listOf(
                "ALL" to "All (16)",
                "AIR_INDIA" to "Air India",
                "INDIGO" to "IndiGo",
                "VISTARA" to "Vistara",
                "SPICEJET" to "SpiceJet",
                "INTERNATIONAL" to "International"
            )
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { (id, label) ->
                    val isSelected = selectedFilter == id
                    Surface(
                        onClick = { selectedFilter = id },
                        shape = RoundedCornerShape(10.dp),
                        color = if (isSelected) AeroBlue else MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) AeroBlue else MaterialTheme.colorScheme.outlineVariant
                        )
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }
            }
        }

        // 4. Interactive Radar Scope Viewport (Matches Reference 2)
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(340.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF040D1A))
                    .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(20.dp))
                    .testTag("radar_canvas_container")
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(filteredAircraft) {
                            detectTapGestures { offset ->
                                val width = size.width.toFloat()
                                val height = size.height.toFloat()
                                var closest: RadarAircraft? = null
                                var minDistance = Float.MAX_VALUE
                                filteredAircraft.forEach { a ->
                                    val ax = a.xRatio * width
                                    val ay = a.yRatio * height
                                    val dist = kotlin.math.hypot(offset.x - ax, offset.y - ay)
                                    if (dist < 80f && dist < minDistance) {
                                        minDistance = dist
                                        closest = a
                                    }
                                }
                                closest?.let { repository.selectRadarAircraft(it.callsign) }
                            }
                        }
                ) {
                    val cx = size.width / 2f
                    val cy = size.height / 2f
                    val maxR = minOf(cx, cy) * 0.90f * zoomScale

                    // Concentric Range Rings (20km, 40km, 60km, 80km)
                    val rings = listOf(0.25f, 0.5f, 0.75f, 1.0f)
                    rings.forEach { ratio ->
                        drawCircle(
                            color = Color(0xFF1A365D).copy(alpha = 0.5f),
                            radius = maxR * ratio,
                            center = Offset(cx, cy),
                            style = Stroke(
                                width = 1.2f,
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f), 0f)
                            )
                        )
                    }

                    // Radar Bearing Crosshairs
                    drawLine(
                        color = Color(0xFF1A365D).copy(alpha = 0.45f),
                        start = Offset(cx, cy - maxR),
                        end = Offset(cx, cy + maxR),
                        strokeWidth = 1f
                    )
                    drawLine(
                        color = Color(0xFF1A365D).copy(alpha = 0.45f),
                        start = Offset(cx - maxR, cy),
                        end = Offset(cx + maxR, cy),
                        strokeWidth = 1f
                    )

                    // Sweeping Beam Animation
                    val sweepRad = Math.toRadians(sweepAngle.toDouble())
                    val sweepX = cx + (maxR * sin(sweepRad)).toFloat()
                    val sweepY = cy - (maxR * cos(sweepRad)).toFloat()
                    drawLine(
                        color = AeroSkyBlue.copy(alpha = 0.55f),
                        start = Offset(cx, cy),
                        end = Offset(sweepX, sweepY),
                        strokeWidth = 2f
                    )

                    // Airport Center: GOX Mopa (Green Hub)
                    drawCircle(color = AeroSuccess, radius = 6f, center = Offset(cx, cy))
                    drawCircle(color = AeroSuccess.copy(alpha = 0.3f), radius = 14f, center = Offset(cx, cy), style = Stroke(width = 1.5f))

                    // Draw 16 Aircraft Targets with Callouts
                    filteredAircraft.forEach { a ->
                        val ax = cx + (a.xRatio - 0.5f) * size.width * zoomScale
                        val ay = cy + (a.yRatio - 0.5f) * size.height * zoomScale
                        val isSelected = selectedAircraft.callsign == a.callsign
                        val isDemo = a.isDemo

                        val planeColor = when {
                            isDemo -> AeroSkyBlue
                            a.airline.contains("IndiGo", ignoreCase = true) -> AeroSuccess
                            a.airline.contains("Vistara", ignoreCase = true) -> Color(0xFFE2B93B)
                            else -> Color(0xFF94A3B8)
                        }

                        // Heading Line
                        val rad = Math.toRadians(a.headingDeg.toDouble())
                        val hx = ax + (18f * sin(rad)).toFloat()
                        val hy = ay - (18f * cos(rad)).toFloat()
                        drawLine(
                            color = planeColor,
                            start = Offset(ax, ay),
                            end = Offset(hx, hy),
                            strokeWidth = if (isSelected) 3f else 1.5f
                        )

                        // Target Core Dot
                        drawCircle(
                            color = planeColor,
                            radius = if (isSelected) 7f else if (isDemo) 5.5f else 4.5f,
                            center = Offset(ax, ay)
                        )

                        // Pulsing selection halo
                        if (isSelected) {
                            drawCircle(
                                color = AeroSkyBlue.copy(alpha = 0.4f),
                                radius = 18f,
                                center = Offset(ax, ay),
                                style = Stroke(width = 2f)
                            )
                        }

                        // Aircraft Telemetry Label directly on Radar
                        val calloutText = "${a.code}\n${a.altitudeFt / 1000}k ft · ${a.speedKnots}kt"
                        val textLayout = textMeasurer.measure(
                            text = AnnotatedString(calloutText),
                            style = TextStyle(
                                color = if (isSelected) AeroSkyBlue else Color.White.copy(alpha = 0.85f),
                                fontSize = 8.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        )
                        val boxW = textLayout.size.width + 10f
                        val boxH = textLayout.size.height + 6f
                        val labelX = ax + 10f
                        val labelY = ay - boxH / 2

                        drawRoundRect(
                            color = Color(0xFF071426).copy(alpha = 0.85f),
                            topLeft = Offset(labelX, labelY),
                            size = Size(boxW, boxH),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawText(textLayoutResult = textLayout, topLeft = Offset(labelX + 5f, labelY + 3f))
                    }
                }

                // Top Left: Compass North Circle
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF071426).copy(alpha = 0.85f))
                        .border(1.dp, Color(0xFF1E3A5F), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Navigation, contentDescription = null, tint = AeroSkyBlue, modifier = Modifier.size(12.dp))
                        Text("N", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }

                // Top Right: Vertical Radar Controls (Layers, Target, Zoom +, Zoom -)
                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RadarControlBtn(icon = Icons.Default.Layers, onClick = {})
                    RadarControlBtn(icon = Icons.Default.MyLocation, onClick = { repository.selectRadarAircraft("AI1234") })
                    RadarControlBtn(icon = Icons.Default.Add, onClick = { zoomScale = (zoomScale + 0.2f).coerceAtMost(2.0f) })
                    RadarControlBtn(icon = Icons.Default.Remove, onClick = { zoomScale = (zoomScale - 0.2f).coerceAtLeast(0.6f) })
                }

                // Center Airport Label Badge: GOX / MOPA
                Surface(
                    color = Color(0xFF071426).copy(alpha = 0.92f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AeroSuccess.copy(alpha = 0.45f)),
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(modifier = Modifier.size(5.dp).clip(CircleShape).background(AeroSuccess))
                        Text("GOX / MOPA", color = AeroSuccess, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // Bottom Left: Distance Scale (20 km)
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(modifier = Modifier.width(36.dp).height(2.dp).background(Color.White.copy(alpha = 0.7f)))
                    Text("20 km", color = Color.White.copy(alpha = 0.7f), fontSize = 9.sp, fontWeight = FontWeight.Medium)
                }

                // Bottom Right: Simulation Disclaimer
                Text(
                    text = "Map data is simulated for demonstration.",
                    color = Color.White.copy(alpha = 0.45f),
                    fontSize = 8.sp,
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(12.dp)
                )
            }
        }

        // 5. Selected Aircraft Card (Matches Reference 2)
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("selected_aircraft_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Header line: Logo, Code, Badges, Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.White)
                                    .padding(2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_air_india_logo),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Column {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = selectedAircraft.code,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    if (selectedAircraft.isDemo) {
                                        Surface(
                                            color = AeroBlue.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "DEMO FLIGHT",
                                                color = AeroBlue,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${selectedAircraft.airline} · ${selectedAircraft.model} · ${selectedAircraft.route}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Approaching Status Badge
                        Surface(
                            color = AeroSuccess.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, AeroSuccess.copy(alpha = 0.35f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AeroSuccess))
                                Text(
                                    text = "Approaching GOX",
                                    color = AeroSuccess,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "${selectedAircraft.distanceKm} km away · ETA 5 min",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                    // 4 Telemetry Metrics Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        TelemetryColumn(icon = Icons.Default.Height, label = "Altitude", value = "${selectedAircraft.altitudeFt.toLocaleString()} ft")
                        TelemetryColumn(icon = Icons.Default.Speed, label = "Ground Speed", value = "${selectedAircraft.speedKnots} kts")
                        TelemetryColumn(icon = Icons.Default.Navigation, label = "Heading", value = "${selectedAircraft.headingDeg}°")
                        TelemetryColumn(icon = Icons.Default.NearMe, label = "Distance to GOX", value = "${selectedAircraft.distanceKm} km", valueColor = AeroSuccess)
                    }

                    // 3 Action Buttons (View Details, Track Flight, Share)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("View Details", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = { repository.selectRadarAircraft("AI1234") },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.MyLocation, contentDescription = null, modifier = Modifier.size(13.dp))
                                Text("Track Flight", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                        OutlinedButton(
                            onClick = {},
                            modifier = Modifier.weight(0.9f).height(38.dp),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(13.dp))
                                Text("Share", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }

        // 6. Nearby Flights List Section (Matches Reference 2)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Nearby Flights (${filteredAircraft.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "View all >",
                        color = AeroBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                filteredAircraft.take(4).forEach { aircraft ->
                    Surface(
                        onClick = { repository.selectRadarAircraft(aircraft.callsign) },
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (selectedAircraft.callsign == aircraft.callsign) AeroBlue else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(if (aircraft.airline.contains("IndiGo", ignoreCase = true)) Color(0xFF001B94) else Color.White)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (aircraft.airline.contains("IndiGo", ignoreCase = true)) {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_indigo_logo),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_air_india_logo),
                                            contentDescription = null,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                }
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        Text(aircraft.code, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                        if (aircraft.isDemo) {
                                            Surface(
                                                color = AeroBlue.copy(alpha = 0.15f),
                                                shape = RoundedCornerShape(4.dp)
                                            ) {
                                                Text("DEMO", color = AeroBlue, fontSize = 8.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp))
                                            }
                                        }
                                    }
                                    Text(aircraft.route, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }

                            Row(
                                modifier = Modifier.weight(1.5f),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${aircraft.altitudeFt.toLocaleString()} ft", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${aircraft.speedKnots} kts", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${aircraft.distanceKm} km", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = AeroSuccess)
                                    Text(aircraft.status, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryColumn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(11.dp))
            Text(label, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = valueColor)
    }
}

@Composable
private fun RadarControlBtn(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFF071426).copy(alpha = 0.85f))
            .border(1.dp, Color(0xFF1E3A5F), CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(imageVector = icon, contentDescription = null, tint = AeroSkyBlue, modifier = Modifier.size(16.dp))
    }
}

private fun Int.toLocaleString(): String {
    return String.format("%,d", this)
}
