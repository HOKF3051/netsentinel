package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val NetDarkColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    secondary = CyberEmerald,
    onSecondary = Color.Black,
    tertiary = CyberSapphire,
    background = NetDarkBackground,
    surface = NetDarkSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = NetDarkBorder,
    onSurfaceVariant = TextGray
)

private val NetAmoledColorScheme = darkColorScheme(
    primary = CyberCyan,
    onPrimary = Color.Black,
    secondary = CyberEmerald,
    onSecondary = Color.Black,
    tertiary = CyberSapphire,
    background = NetAmoledBackground,
    surface = NetAmoledSurface,
    onBackground = Color.White,
    onSurface = Color.White,
    surfaceVariant = NetAmoledBorder,
    onSurfaceVariant = TextGray
)

private val NetLightColorScheme = lightColorScheme(
    primary = CyberSapphire,
    onPrimary = Color.White,
    secondary = CyberEmerald,
    onSecondary = Color.White,
    tertiary = CyberCyan,
    background = NetLightBackground,
    surface = NetLightSurface,
    onBackground = Color(0xFF0F172A),
    onSurface = Color(0xFF0F172A),
    surfaceVariant = NetLightBorder,
    onSurfaceVariant = Color(0xFF475569)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    amoledMode: Boolean = false,
    accentColorName: String = "Indigo",
    content: @Composable () -> Unit,
) {
    val baseScheme = when {
        amoledMode -> NetAmoledColorScheme
        darkTheme -> NetDarkColorScheme
        else -> NetLightColorScheme
    }

    // Resolve accent overrides dynamically
    val resolvedPrimary = when (accentColorName) {
        "Indigo" -> CyberIndigo
        "Cyan" -> CyberCyan
        "Emerald" -> CyberEmerald
        "Sapphire" -> CyberSapphire
        "Coral" -> CyberCoral
        else -> CyberIndigo
    }

    val finalColorScheme = baseScheme.copy(
        primary = resolvedPrimary,
        surface = when {
            amoledMode -> NetAmoledSurface.copy(alpha = 0.35f)
            darkTheme -> Color(0xFF1E293B).copy(alpha = 0.45f) // Slate glass surface
            else -> Color.White.copy(alpha = 0.55f) // Light glass surface
        },
        surfaceVariant = when {
            amoledMode -> NetAmoledBorder.copy(alpha = 0.50f)
            darkTheme -> Color(0xFF334155).copy(alpha = 0.50f)
            else -> Color(0xFFE2E8F0).copy(alpha = 0.50f)
        },
        outline = when {
            amoledMode -> Color.White.copy(alpha = 0.15f)
            darkTheme -> Color.White.copy(alpha = 0.12f) // Clean glass border
            else -> Color.Black.copy(alpha = 0.08f)
        }
    )

    MaterialTheme(
        colorScheme = finalColorScheme,
        typography = Typography,
        content = content
    )
}

