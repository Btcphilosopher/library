package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val StudyDarkColorScheme = darkColorScheme(
    primary = NaturalSageGreenDark,
    secondary = NaturalTextSecondaryDark,
    tertiary = NaturalTerracottaDark,
    background = NaturalBgDark,
    surface = NaturalSurfaceDark,
    surfaceVariant = NaturalDividerDark,
    onPrimary = NaturalBgDark,
    onSecondary = NaturalTextPrimaryDark,
    onBackground = NaturalTextPrimaryDark,
    onSurface = NaturalTextPrimaryDark,
    outline = NaturalDividerDark
)

private val PaperLightColorScheme = lightColorScheme(
    primary = NaturalSageGreen,
    secondary = NaturalTextSecondaryLight,
    tertiary = NaturalTerracotta,
    background = NaturalBgLight,
    surface = NaturalSurfaceLight,
    surfaceVariant = NaturalSurfaceVariantLight,
    onPrimary = NaturalSurfaceLight,
    onSecondary = NaturalTextPrimaryLight,
    onBackground = NaturalTextPrimaryLight,
    onSurface = NaturalTextPrimaryLight,
    outline = NaturalDividerLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set default to false to prioritize our gorgeous editorial color scheme!
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> StudyDarkColorScheme
        else -> PaperLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
