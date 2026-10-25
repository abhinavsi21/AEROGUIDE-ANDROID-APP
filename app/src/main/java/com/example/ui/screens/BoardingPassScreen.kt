package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.Flight
import com.example.data.model.PassengerProfile
import com.example.ui.theme.*
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BoardingPassScreen(
    profile: PassengerProfile,
    flight: Flight,
    onBack: () -> Unit,
    onNavigateToGate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var isExporting by remember { mutableStateOf(false) }
    var exportSuccessMessage by remember { mutableStateOf<String?>(null) }

    fun exportBoardingPassPdf(share: Boolean = false) {
        try {
            isExporting = true
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 standard
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            val paint = Paint().apply { isAntiAlias = true }

            // Header Background
            paint.color = android.graphics.Color.parseColor("#071426")
            canvas.drawRect(0f, 0f, 595f, 160f, paint)

            // Accent Line
            paint.color = android.graphics.Color.parseColor("#2878E8")
            canvas.drawRect(0f, 160f, 595f, 166f, paint)

            // Title
            paint.color = android.graphics.Color.WHITE
            paint.textSize = 24f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("AEROGUIDE DIGITAL BOARDING PASS", 40f, 65f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = android.graphics.Color.parseColor("#39BDF8")
            canvas.drawText("Manohar International Airport (GOX / Mopa) · Cloud Crew", 40f, 95f, paint)

            // Demo disclaimer badge
            paint.color = android.graphics.Color.parseColor("#FF9F43")
            paint.textSize = 11f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DEMO — NOT VALID FOR TRAVEL", 40f, 130f, paint)

            // Main Card Background
            paint.color = android.graphics.Color.parseColor("#F3F6FC")
            canvas.drawRect(0f, 166f, 595f, 842f, paint)

            // White Pass Tile
            paint.color = android.graphics.Color.WHITE
            canvas.drawRoundRect(40f, 190f, 555f, 750f, 16f, 16f, paint)

            paint.color = android.graphics.Color.parseColor("#D9E3F0")
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 2f
            canvas.drawRoundRect(40f, 190f, 555f, 750f, 16f, 16f, paint)
            paint.style = Paint.Style.FILL

            // Airline & Flight Number
            paint.color = android.graphics.Color.parseColor("#13233B")
            paint.textSize = 20f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("Air India  ·  ${flight.flightNumber}", 65f, 235f, paint)

            paint.textSize = 12f
            paint.color = android.graphics.Color.parseColor("#64748B")
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText("Electronic Passenger Coupon", 65f, 255f, paint)

            // Route Large
            paint.textSize = 34f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = android.graphics.Color.parseColor("#13233B")
            canvas.drawText(flight.originCode, 65f, 310f, paint)
            canvas.drawText(flight.destinationCode, 430f, 310f, paint)

            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = android.graphics.Color.parseColor("#64748B")
            canvas.drawText(flight.originCity, 65f, 330f, paint)
            canvas.drawText(flight.destinationCity, 430f, 330f, paint)

            // Flight details grid
            fun drawField(label: String, value: String, x: Float, y: Float, isAccent: Boolean = false) {
                paint.color = android.graphics.Color.parseColor("#64748B")
                paint.textSize = 10f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(label.uppercase(), x, y, paint)

                paint.color = if (isAccent) android.graphics.Color.parseColor("#2878E8") else android.graphics.Color.parseColor("#13233B")
                paint.textSize = 18f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText(value, x, y + 24f, paint)
            }

            drawField("Passenger Name", profile.fullName, 65f, 375f)
            drawField("Booking Ref", "AG7X92", 360f, 375f)

            drawField("Gate", "Gate ${flight.gate}", 65f, 440f, isAccent = true)
            drawField("Terminal", "T1 Domestic", 220f, 440f)
            drawField("Seat", flight.seat, 360f, 440f)

            drawField("Boarding Time", flight.boardingTime, 65f, 510f)
            drawField("Departure", flight.departureTime, 220f, 510f)
            drawField("Gate Closes", flight.gateCloseTime, 360f, 510f)

            drawField("Boarding Group", "Group B", 65f, 580f)
            drawField("Class", "Economy Extra", 220f, 580f)
            drawField("Status", "BOARDING", 360f, 580f, isAccent = true)

            // Dotted Separator
            paint.color = android.graphics.Color.parseColor("#D9E3F0")
            paint.strokeWidth = 2f
            var dotX = 65f
            while (dotX < 530f) {
                canvas.drawLine(dotX, 640f, dotX + 6f, 640f, paint)
                dotX += 12f
            }

            // Barcode Simulation
            paint.color = android.graphics.Color.parseColor("#13233B")
            var barX = 70f
            val bars = listOf(3f, 6f, 2f, 8f, 3f, 5f, 2f, 7f, 4f, 6f, 2f, 4f, 8f, 2f, 5f, 3f, 7f, 2f, 6f, 4f, 2f, 8f, 3f, 5f, 2f, 7f, 3f, 6f, 2f, 5f, 8f, 3f, 4f, 6f)
            bars.forEach { w ->
                canvas.drawRect(barX, 665f, barX + w, 715f, paint)
                barX += w + 6f
            }

            paint.textSize = 10f
            paint.color = android.graphics.Color.parseColor("#64748B")
            canvas.drawText("* AI1234 - BOM - GOX - SEAT 14C - GATE ${flight.gate} *", 130f, 735f, paint)

            pdfDoc.finishPage(page)

            // Write to file
            val file = File(context.cacheDir, "AeroGuide_BoardingPass_AI1234.pdf")
            val outputStream = FileOutputStream(file)
            pdfDoc.writeTo(outputStream)
            outputStream.close()
            pdfDoc.close()

            isExporting = false
            exportSuccessMessage = "Saved PDF to cache: AeroGuide_BoardingPass_AI1234.pdf"
            Toast.makeText(context, "Boarding pass generated successfully!", Toast.LENGTH_SHORT).show()

            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            if (share) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "AeroGuide Boarding Pass - AI 1234")
                    putExtra(Intent.EXTRA_TEXT, "Here is your AeroGuide digital boarding pass for flight AI 1234 (Gate ${flight.gate}).")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Boarding Pass PDF"))
            } else {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                try {
                    context.startActivity(viewIntent)
                } catch (_: Exception) {
                    // Fallback to share intent if no PDF viewer installed
                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(shareIntent, "Save or Open Boarding Pass"))
                }
            }
        } catch (e: Exception) {
            isExporting = false
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Screen Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
                            .testTag("boarding_pass_back_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                    Text(
                        text = "Digital Boarding Pass",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                // Official DEMO disclaimer badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = AeroWarning.copy(alpha = 0.15f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AeroWarning.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = "DEMO",
                        color = AeroWarning,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // 2. High-Fidelity Aviation Boarding Pass Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("digital_boarding_pass_card"),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {

                    // Airline Banner Header
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(AeroNavyDark)
                            .padding(18.dp)
                    ) {
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
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color.White)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Image(
                                        painter = painterResource(id = R.drawable.ic_air_india_logo),
                                        contentDescription = "Air India",
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                                Column {
                                    Text("Air India", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                    Text(flight.flightNumber, color = AeroSkyBlue, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }

                            // Gate Updated Pill (If simulation triggered Gate 31)
                            if (flight.gate == "31") {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AeroWarning,
                                    modifier = Modifier.testTag("boarding_pass_updated_gate_badge")
                                ) {
                                    Text(
                                        text = "UPDATED GATE 31",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            } else {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = AeroSuccess.copy(alpha = 0.2f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, AeroSuccess.copy(alpha = 0.5f))
                                ) {
                                    Text(
                                        text = "BOARDING",
                                        color = AeroSuccess,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Pass Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Route Display: BOM -> GOX
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(flight.originCode, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(flight.originCity, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FlightTakeoff, contentDescription = null, tint = AeroBlue, modifier = Modifier.size(24.dp))
                                Text("1h 15m Non-stop", fontSize = 10.sp, color = AeroSkyBlue, fontWeight = FontWeight.SemiBold)
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(flight.destinationCode, fontSize = 34.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                                Text(flight.destinationCity, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))

                        // Passenger & Booking Reference Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("PASSENGER", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(profile.fullName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("BOOKING REF", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("AG7X92", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = AeroBlue)
                            }
                        }

                        // Flight Parameters Grid
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("GATE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(flight.gate, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = if (flight.gate == "31") AeroWarning else AeroBlue)
                            }
                            Column {
                                Text("SEAT", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(flight.seat, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column {
                                Text("GROUP", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("B", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("TERMINAL", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("T1", fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Time Schedule Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("BOARDING", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(flight.boardingTime, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AeroSkyBlue)
                            }
                            Column {
                                Text("GATE CLOSES", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(flight.gateCloseTime, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AeroWarning)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("DEPARTURE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(flight.departureTime, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                            }
                        }

                        // Decorative Tear-off Line with Notches
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Canvas(modifier = Modifier.fillMaxWidth()) {
                                drawLine(
                                    color = Color(0xFFD9E3F0),
                                    start = Offset(0f, size.height / 2),
                                    end = Offset(size.width, size.height / 2),
                                    strokeWidth = 2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 10f), 0f)
                                )
                            }
                        }

                        // High-Fidelity Demo Barcode Element
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(58.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 8.dp)) {
                                    val barColor = if (flight.gate == "31") Color(0xFF13233B) else Color(0xFF13233B)
                                    val bars = listOf(3f, 7f, 2f, 9f, 4f, 6f, 3f, 8f, 2f, 5f, 7f, 3f, 4f, 9f, 2f, 6f, 3f, 8f, 2f, 7f, 4f, 5f, 9f, 3f, 6f, 2f, 8f, 4f, 5f, 3f)
                                    var x = 0f
                                    bars.forEach { w ->
                                        drawRect(color = barColor, topLeft = Offset(x, 0f), size = androidx.compose.ui.geometry.Size(w, size.height))
                                        x += w + 6f
                                    }
                                }
                            }

                            Text(
                                text = "DEMO — NOT VALID FOR TRAVEL",
                                color = AeroCritical,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }

        // 3. Operational Action Buttons (Download PDF, Share, Navigate)
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Primary Action: Download PDF
                Button(
                    onClick = { exportBoardingPassPdf(share = false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("download_boarding_pass_btn"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AeroBlue, contentColor = Color.White)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isExporting) "Generating PDF..." else "Download Boarding Pass (PDF)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Share Button
                    OutlinedButton(
                        onClick = { exportBoardingPassPdf(share = true) },
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("share_boarding_pass_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                            Text("Share Pass", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    // Navigate to Gate Button
                    OutlinedButton(
                        onClick = onNavigateToGate,
                        modifier = Modifier
                            .weight(1f)
                            .height(46.dp)
                            .testTag("navigate_from_pass_btn"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(Icons.Default.NearMe, contentDescription = null, tint = AeroBlue, modifier = Modifier.size(16.dp))
                            Text("Go to Gate ${flight.gate}", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
