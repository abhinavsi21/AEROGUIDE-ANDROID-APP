package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FlightStatus
import com.example.ui.theme.*

@Composable
fun StatusBadge(
    status: FlightStatus,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, borderColor) = when (status) {
        FlightStatus.BOARDING -> Triple(Color(0xFFE8F9F1), AeroSuccess, Color(0xFF20C77A).copy(alpha = 0.4f))
        FlightStatus.GATE_CHANGE -> Triple(AeroWarningContainer, AeroWarning, AeroWarning.copy(alpha = 0.5f))
        FlightStatus.FINAL_CALL -> Triple(AeroCriticalContainer, AeroCritical, AeroCritical.copy(alpha = 0.6f))
        FlightStatus.CHECK_IN_OPEN -> Triple(Color(0xFFE0EDFD), AeroBlue, AeroBlue.copy(alpha = 0.4f))
        FlightStatus.SCHEDULED -> Triple(Color(0xFFF1F5F9), AeroTextSecondary, AeroBorderLight)
        FlightStatus.SECURITY -> Triple(Color(0xFFEDE9FE), Color(0xFF7C3AED), Color(0xFF7C3AED).copy(alpha = 0.4f))
        FlightStatus.DEPARTED -> Triple(Color(0xFFF1F5F9), AeroNavyDark, AeroBorderLight)
    }

    // Pulse animation for active urgent states
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alphaPulse"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        if (status == FlightStatus.BOARDING || status == FlightStatus.GATE_CHANGE || status == FlightStatus.FINAL_CALL) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(textColor.copy(alpha = alpha))
            )
        }
        Text(
            text = status.label.uppercase(),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.6.sp
        )
    }
}
