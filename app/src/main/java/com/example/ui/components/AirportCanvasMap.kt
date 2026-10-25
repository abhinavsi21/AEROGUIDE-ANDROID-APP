package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AirportFacility
import com.example.data.model.PassengerLocation
import com.example.ui.theme.*
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class Waypoint(val x: Float, val y: Float, val name: String)

@Composable
fun AirportCanvasMap(
    facilities: List<AirportFacility>,
    passengerLocation: PassengerLocation,
    targetGate: String,
    isNavigating: Boolean = false,
    navProgress: Float = 0f,
    onSelectGate: (String) -> Unit = {},
    onNavigateClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedLevel by remember { mutableStateOf("Level 2") }
    var zoomScale by remember { mutableFloatStateOf(1.0f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // Pulsing animation for passenger location marker
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseRadius by infiniteTransition.animateFloat(
        initialValue = 8f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseRadius"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseAlpha"
    )

    // Animated dash offset for route flow
    val dashPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "dashPhase"
    )

    val textMeasurer = rememberTextMeasurer()

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(380.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(Color(0xFF071426))
            .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(22.dp))
            .testTag("interactive_airport_canvas_map")
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        zoomScale = (zoomScale * zoom).coerceIn(0.7f, 2.8f)
                        panOffset = Offset(
                            x = (panOffset.x + pan.x).coerceIn(-400f, 400f),
                            y = (panOffset.y + pan.y).coerceIn(-400f, 400f)
                        )
                    }
                }
                .pointerInput(targetGate) {
                    detectTapGestures { tapOffset ->
                        // Detect taps near gates to dynamically switch destination
                        val w = size.width
                        val h = size.height
                        val cx = w / 2f + panOffset.x
                        val cy = h / 2f + panOffset.y

                        val gate24Pos = Offset(cx + (0.75f - 0.5f) * w * zoomScale, cy + (0.32f - 0.5f) * h * zoomScale)
                        val gate31Pos = Offset(cx + (0.86f - 0.5f) * w * zoomScale, cy + (0.65f - 0.5f) * h * zoomScale)

                        if (hypot(tapOffset.x - gate24Pos.x, tapOffset.y - gate24Pos.y) < 45f) {
                            onSelectGate("24")
                        } else if (hypot(tapOffset.x - gate31Pos.x, tapOffset.y - gate31Pos.y) < 45f) {
                            onSelectGate("31")
                        }
                    }
                }
        ) {
            val width = size.width
            val height = size.height
            val cx = width / 2f + panOffset.x
            val cy = height / 2f + panOffset.y

            fun mapX(relX: Float): Float = cx + (relX - 0.5f) * width * zoomScale
            fun mapY(relY: Float): Float = cy + (relY - 0.5f) * height * zoomScale

            // 1. TARMAC APRON BACKGROUND & TAXIWAY LINES
            drawRect(color = Color(0xFF06101D), size = size)

            // Apron taxiway guide lines
            drawLine(
                color = Color(0xFF0F2644),
                start = Offset(0f, mapY(0.18f)),
                end = Offset(width, mapY(0.18f)),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
            )
            drawLine(
                color = Color(0xFF0F2644),
                start = Offset(0f, mapY(0.85f)),
                end = Offset(width, mapY(0.85f)),
                strokeWidth = 3f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 16f), 0f)
            )

            // 2. ARCHITECTURAL TERMINAL CORRIDORS & CONCOURSES
            // Main Central Terminal Atrium Building
            val atriumL = mapX(0.12f)
            val atriumT = mapY(0.35f)
            val atriumW = 0.38f * width * zoomScale
            val atriumH = 0.45f * height * zoomScale

            drawRoundRect(
                color = Color(0xFF0E223D),
                topLeft = Offset(atriumL, atriumT),
                size = Size(atriumW, atriumH),
                cornerRadius = CornerRadius(16f * zoomScale, 16f * zoomScale)
            )
            drawRoundRect(
                color = Color(0xFF1E3A5F),
                topLeft = Offset(atriumL, atriumT),
                size = Size(atriumW, atriumH),
                cornerRadius = CornerRadius(16f * zoomScale, 16f * zoomScale),
                style = Stroke(width = 2f)
            )

            // Concourse Pier A (Gates 20 - 26 corridor leading North-East)
            val pierAL = mapX(0.42f)
            val pierAT = mapY(0.24f)
            val pierAW = 0.42f * width * zoomScale
            val pierAH = 0.16f * height * zoomScale

            drawRoundRect(
                color = Color(0xFF102747),
                topLeft = Offset(pierAL, pierAT),
                size = Size(pierAW, pierAH),
                cornerRadius = CornerRadius(12f * zoomScale, 12f * zoomScale)
            )
            drawRoundRect(
                color = Color(0xFF1E3A5F),
                topLeft = Offset(pierAL, pierAT),
                size = Size(pierAW, pierAH),
                cornerRadius = CornerRadius(12f * zoomScale, 12f * zoomScale),
                style = Stroke(width = 2f)
            )

            // Concourse Pier B (Satellite extension leading South-East to Gate 31)
            val pierBL = mapX(0.48f)
            val pierBT = mapY(0.56f)
            val pierBW = 0.44f * width * zoomScale
            val pierBH = 0.16f * height * zoomScale

            drawRoundRect(
                color = Color(0xFF102747),
                topLeft = Offset(pierBL, pierBT),
                size = Size(pierBW, pierBH),
                cornerRadius = CornerRadius(12f * zoomScale, 12f * zoomScale)
            )
            drawRoundRect(
                color = Color(0xFF1E3A5F),
                topLeft = Offset(pierBL, pierBT),
                size = Size(pierBW, pierBH),
                cornerRadius = CornerRadius(12f * zoomScale, 12f * zoomScale),
                style = Stroke(width = 2f)
            )

            // Central Connector Corridor
            val connL = mapX(0.38f)
            val connT = mapY(0.32f)
            val connW = 0.16f * width * zoomScale
            val connH = 0.35f * height * zoomScale
            drawRoundRect(
                color = Color(0xFF0F2644),
                topLeft = Offset(connL, connT),
                size = Size(connW, connH),
                cornerRadius = CornerRadius(8f * zoomScale, 8f * zoomScale)
            )

            // 3. ARCHITECTURAL ZONES & LANDMARKS
            // Security Check Area (Pre/Post Security Boundary)
            val secX = mapX(0.18f)
            val secY = mapY(0.42f)
            drawRoundRect(
                color = Color(0xFF143054),
                topLeft = Offset(secX, secY),
                size = Size(0.10f * width * zoomScale, 0.12f * height * zoomScale),
                cornerRadius = CornerRadius(6f, 6f)
            )
            val secText = textMeasurer.measure(
                text = AnnotatedString("Security\nCheck"),
                style = TextStyle(color = Color(0xFF94A3B8), fontSize = (8 * zoomScale).sp, fontWeight = FontWeight.Medium)
            )
            drawText(secText, topLeft = Offset(secX + 6f, secY + 6f))

            // Food Court Area (Starting Position Zone)
            val fcX = mapX(0.16f)
            val fcY = mapY(0.64f)
            drawRoundRect(
                color = Color(0xFF0F3224).copy(alpha = 0.6f),
                topLeft = Offset(fcX, fcY),
                size = Size(0.12f * width * zoomScale, 0.12f * height * zoomScale),
                cornerRadius = CornerRadius(8f, 8f)
            )
            drawRoundRect(
                color = AeroSuccess.copy(alpha = 0.4f),
                topLeft = Offset(fcX, fcY),
                size = Size(0.12f * width * zoomScale, 0.12f * height * zoomScale),
                cornerRadius = CornerRadius(8f, 8f),
                style = Stroke(width = 1f)
            )
            val fcText = textMeasurer.measure(
                text = AnnotatedString("Food Court\nLevel 2"),
                style = TextStyle(color = AeroSuccess, fontSize = (8 * zoomScale).sp, fontWeight = FontWeight.Bold)
            )
            drawText(fcText, topLeft = Offset(fcX + 6f, fcY + 6f))

            // Central Atrium & Retail Rotunda
            val atrCenterX = mapX(0.38f)
            val atrCenterY = mapY(0.50f)
            drawCircle(
                color = Color(0xFF16375E),
                radius = 28f * zoomScale,
                center = Offset(atrCenterX, atrCenterY)
            )
            drawCircle(
                color = Color(0xFF1E4878),
                radius = 28f * zoomScale,
                center = Offset(atrCenterX, atrCenterY),
                style = Stroke(width = 1.5f)
            )
            val atrText = textMeasurer.measure(
                text = AnnotatedString("Central\nAtrium"),
                style = TextStyle(color = Color.White, fontSize = (8 * zoomScale).sp, fontWeight = FontWeight.Bold)
            )
            drawText(atrText, topLeft = Offset(atrCenterX - 18f * zoomScale, atrCenterY - 14f * zoomScale))

            // Duty Free & Shopping Zone
            val dfX = mapX(0.30f)
            val dfY = mapY(0.40f)
            val dfText = textMeasurer.measure(
                text = AnnotatedString("Shops & Duty Free"),
                style = TextStyle(color = Color(0xFF94A3B8), fontSize = (8 * zoomScale).sp, fontWeight = FontWeight.Medium)
            )
            drawText(dfText, topLeft = Offset(dfX, dfY))

            // 4. DRAW ALL GATES (Gates 20 - 26 and Gate 31)
            val gatePositions = listOf(
                "20" to Offset(mapX(0.50f), mapY(0.22f)),
                "21" to Offset(mapX(0.56f), mapY(0.38f)),
                "22" to Offset(mapX(0.62f), mapY(0.22f)),
                "23" to Offset(mapX(0.68f), mapY(0.38f)),
                "24" to Offset(mapX(0.75f), mapY(0.22f)),
                "25" to Offset(mapX(0.79f), mapY(0.38f)),
                "26" to Offset(mapX(0.83f), mapY(0.22f)),
                "31" to Offset(mapX(0.86f), mapY(0.65f))
            )

            gatePositions.forEach { (gateNum, pos) ->
                val isTarget = gateNum == targetGate
                val gateColor = when {
                    isTarget && gateNum == "31" -> AeroWarning
                    isTarget -> AeroBlue
                    else -> Color(0xFF64748B)
                }

                // Gate Bay Notch & Jet Bridge Line
                val bridgeEnd = if (pos.y < mapY(0.30f)) Offset(pos.x, pos.y - 18f * zoomScale) else Offset(pos.x, pos.y + 18f * zoomScale)
                drawLine(
                    color = gateColor.copy(alpha = 0.6f),
                    start = pos,
                    end = bridgeEnd,
                    strokeWidth = 3f * zoomScale
                )

                // Gate Node Capsule
                val nodeR = if (isTarget) 14f * zoomScale else 10f * zoomScale
                drawCircle(color = gateColor, radius = nodeR, center = pos)
                drawCircle(color = Color.White, radius = nodeR * 0.45f, center = pos)

                if (isTarget) {
                    drawCircle(
                        color = gateColor.copy(alpha = 0.4f),
                        radius = nodeR * 1.8f,
                        center = pos,
                        style = Stroke(width = 2f)
                    )
                }

                // Gate Label Tag
                val labelText = "Gate $gateNum"
                val labelLayout = textMeasurer.measure(
                    text = AnnotatedString(labelText),
                    style = TextStyle(
                        color = if (isTarget) Color.White else Color(0xFFCBD5E1),
                        fontSize = (9 * zoomScale).sp,
                        fontWeight = if (isTarget) FontWeight.Bold else FontWeight.Medium
                    )
                )
                val tagW = labelLayout.size.width + 12f
                val tagH = labelLayout.size.height + 6f
                val tagY = if (pos.y < mapY(0.30f)) pos.y - 38f * zoomScale else pos.y + 20f * zoomScale

                drawRoundRect(
                    color = if (isTarget) gateColor else Color(0xFF071426).copy(alpha = 0.85f),
                    topLeft = Offset(pos.x - tagW / 2, tagY),
                    size = Size(tagW, tagH),
                    cornerRadius = CornerRadius(4f, 4f)
                )
                drawText(labelLayout, topLeft = Offset(pos.x - tagW / 2 + 6f, tagY + 3f))
            }

            // 5. PROGRAMMATIC ROUTE GRAPH CALCULATION & RENDERING
            // Food Court -> Concourse junction -> Target Gate (24 or 31)
            val pStart = Offset(mapX(0.20f), mapY(0.68f))
            val pAtrium = Offset(mapX(0.38f), mapY(0.50f))
            val pJunction = Offset(mapX(0.48f), mapY(0.32f))
            val pGate24 = Offset(mapX(0.75f), mapY(0.22f))
            val pGate31 = Offset(mapX(0.86f), mapY(0.65f))

            val routeWaypoints = if (targetGate == "31") {
                listOf(
                    pStart,
                    Offset(mapX(0.28f), mapY(0.60f)),
                    pAtrium,
                    Offset(mapX(0.48f), mapY(0.56f)),
                    Offset(mapX(0.68f), mapY(0.64f)),
                    pGate31
                )
            } else {
                listOf(
                    pStart,
                    Offset(mapX(0.28f), mapY(0.60f)),
                    pAtrium,
                    pJunction,
                    Offset(mapX(0.62f), mapY(0.26f)),
                    pGate24
                )
            }

            val routePath = Path().apply {
                moveTo(routeWaypoints.first().x, routeWaypoints.first().y)
                for (i in 1 until routeWaypoints.size) {
                    lineTo(routeWaypoints[i].x, routeWaypoints[i].y)
                }
            }

            // Soft glowing route shadow underlay
            drawPath(
                path = routePath,
                color = AeroSkyBlue.copy(alpha = 0.35f),
                style = Stroke(width = 14f * zoomScale, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )

            // Animated directional route line
            drawPath(
                path = routePath,
                color = AeroBlue,
                style = Stroke(
                    width = 5f * zoomScale,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), dashPhase)
                )
            )

            // 6. SIMULATED PASSENGER MOVEMENT ALONG THE ROUTE
            val currentPassengerPos = if (isNavigating && navProgress > 0f) {
                // Interpolate along route waypoints based on navProgress (0.0 to 1.0)
                val totalSegments = routeWaypoints.size - 1
                val scaledProgress = navProgress * totalSegments
                val segIndex = scaledProgress.toInt().coerceIn(0, totalSegments - 1)
                val segT = scaledProgress - segIndex
                val pA = routeWaypoints[segIndex]
                val pB = routeWaypoints[segIndex + 1]
                Offset(
                    x = pA.x + (pB.x - pA.x) * segT,
                    y = pA.y + (pB.y - pA.y) * segT
                )
            } else {
                pStart
            }

            // Passenger Pulsing Halo
            drawCircle(
                color = AeroSuccess.copy(alpha = pulseAlpha),
                radius = pulseRadius * zoomScale,
                center = currentPassengerPos,
                style = Stroke(width = 3f)
            )
            drawCircle(color = Color.White, radius = 9f * zoomScale, center = currentPassengerPos)
            drawCircle(color = AeroSuccess, radius = 6f * zoomScale, center = currentPassengerPos)

            // "You are here" / "Navigating" Callout Tag
            val passengerLabel = if (isNavigating) "Navigating to Gate $targetGate" else "You are here\nFood Court (Level 2)"
            val passTextLayout = textMeasurer.measure(
                text = AnnotatedString(passengerLabel),
                style = TextStyle(color = Color.White, fontSize = (9 * zoomScale).sp, fontWeight = FontWeight.Bold)
            )
            val pTagW = passTextLayout.size.width + 16f
            val pTagH = passTextLayout.size.height + 8f
            drawRoundRect(
                color = Color(0xFF071426).copy(alpha = 0.92f),
                topLeft = Offset(currentPassengerPos.x - pTagW / 2, currentPassengerPos.y - pTagH - 12f * zoomScale),
                size = Size(pTagW, pTagH),
                cornerRadius = CornerRadius(6f, 6f)
            )
            drawRoundRect(
                color = AeroSuccess,
                topLeft = Offset(currentPassengerPos.x - pTagW / 2, currentPassengerPos.y - pTagH - 12f * zoomScale),
                size = Size(pTagW, pTagH),
                cornerRadius = CornerRadius(6f, 6f),
                style = Stroke(width = 1.2f)
            )
            drawText(
                textLayoutResult = passTextLayout,
                topLeft = Offset(currentPassengerPos.x - pTagW / 2 + 8f, currentPassengerPos.y - pTagH - 8f * zoomScale)
            )
        }

        // Top Left: Floor Level Selector (Level 3, Level 2, Level 1, Ground)
        Column(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf("Level 3", "Level 2", "Level 1", "Ground").forEach { level ->
                val isSelected = selectedLevel == level
                Surface(
                    onClick = { selectedLevel = level },
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) AeroBlue else Color(0xFF071426).copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) AeroBlue else Color(0xFF1E3A5F))
                ) {
                    Text(
                        text = level,
                        color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 10.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Top Right: Controls (Compass, Recenter, Zoom In, Zoom Out)
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            horizontalAlignment = Alignment.End
        ) {
            // Compass
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF071426).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF1E3A5F), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Navigation, contentDescription = "North", tint = AeroSkyBlue, modifier = Modifier.size(16.dp))
            }

            // Zoom In
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF071426).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF1E3A5F), CircleShape)
                    .clickable { zoomScale = (zoomScale + 0.25f).coerceAtMost(2.8f) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.White, modifier = Modifier.size(18.dp))
            }

            // Zoom Out
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF071426).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF1E3A5F), CircleShape)
                    .clickable { zoomScale = (zoomScale - 0.25f).coerceAtLeast(0.7f) },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.White, modifier = Modifier.size(18.dp))
            }

            // Recenter
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF071426).copy(alpha = 0.85f))
                    .border(1.dp, Color(0xFF1E3A5F), CircleShape)
                    .clickable {
                        zoomScale = 1.0f
                        panOffset = Offset.Zero
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Recenter", tint = AeroSuccess, modifier = Modifier.size(16.dp))
            }
        }

        // Bottom Left: Active Terminal Label & Mode Indicator
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(12.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF071426).copy(alpha = 0.90f))
                .border(1.dp, Color(0xFF1E3A5F), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(AeroSuccess))
            Text(
                text = "Simulated Indoor Navigation · Terminal T1 ($selectedLevel)",
                color = Color.White,
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
