package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.AeroNavyDark

@Composable
fun AeroGuideLogo(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    elevation: Dp = 4.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .shadow(
                elevation = elevation,
                shape = RoundedCornerShape(size * 0.22f),
                spotColor = Color(0x66071426)
            )
            .clip(RoundedCornerShape(size * 0.22f))
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF071426), Color(0xFF0B2347), Color(0xFF030C19))
                )
            )
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(Color(0xFF38BDF8).copy(alpha = 0.4f), Color(0xFF1E3A5F))
                ),
                RoundedCornerShape(size * 0.22f)
            )
            .testTag("aeroguide_brand_logo"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_aeroguide_logo),
            contentDescription = "AeroGuide Brand Logo",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}
