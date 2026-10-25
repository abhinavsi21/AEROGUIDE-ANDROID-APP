package com.example.ui.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AeroSkyBlue,
    onPrimary = AeroNavyDark,
    primaryContainer = AeroNavyMedium,
    onPrimaryContainer = Color.White,
    secondary = AeroBlue,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF16325C),
    onSecondaryContainer = AeroSkyBlue,
    background = AeroNavyDark,
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF0B1F38),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF102A4C),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF1E3A5F),
    outlineVariant = Color(0xFF152C4A),
    error = Color(0xFFEF4444),
    onError = Color.White
)

// Master Light Theme Color Scheme per Section 6 Spec
private val LightColorScheme = lightColorScheme(
    primary = AeroBlue,                         // #2878E8
    onPrimary = Color.White,
    primaryContainer = AeroLightElevatedBlue,    // #E1EEFF
    onPrimaryContainer = AeroLightPrimaryText,   // #13233B
    secondary = AeroSkyBlue,                    // #19A9E8
    onSecondary = Color.White,
    secondaryContainer = AeroLightSecondarySurface, // #EAF1FB
    onSecondaryContainer = AeroLightPrimaryText,
    tertiary = Color(0xFF06B6D4),
    onTertiary = Color.White,
    background = AeroLightBackground,           // #F3F6FC
    onBackground = AeroLightPrimaryText,        // #13233B
    surface = AeroLightSurface,                 // #FFFFFF
    onSurface = AeroLightPrimaryText,           // #13233B
    surfaceVariant = AeroLightSecondarySurface, // #EAF1FB
    onSurfaceVariant = AeroLightSecondaryText,  // #64748B
    outline = AeroLightBorder,                  // #D9E3F0
    outlineVariant = Color(0xFFE2E8F0),
    error = AeroCritical,                       // #B42318
    onError = Color.White
)

@Composable
fun AeroGuideTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
